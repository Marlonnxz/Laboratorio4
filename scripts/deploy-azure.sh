#!/bin/bash
# ==============================================================================
# Script de Despliegue Automatizado en Microsoft Azure
# Sistemas Distribuidos — Laboratorio 4 (UPTC Sogamoso)
# Ejecutar en Azure Cloud Shell (Bash) desde portal.azure.com
# ==============================================================================

set -e

echo "=========================================================================="
echo "☁️  Iniciando Aprovisionamiento de Infraestructura en Microsoft Azure"
echo "=========================================================================="

# 1. Configuración de Variables Base
LOCATION="eastus"
RANDOM_SUFFIX=$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w 5 | head -n 1)

RESOURCE_GROUP="rg-distribuidos-lab4"
ACR_NAME="acrdislab4${RANDOM_SUFFIX}"
STORAGE_NAME="stgdislab4${RANDOM_SUFFIX}"
POSTGRES_SERVER="psql-distribuidos-${RANDOM_SUFFIX}"
REDIS_NAME="redis-distribuidos-${RANDOM_SUFFIX}"
CONTAINER_APP_ENV="cae-distribuidos-lab4"

DB_ADMIN_USER="dbadmin"
DB_ADMIN_PASSWORD="Lab4SecurePass2026!"
JWT_SECRET="uptc_sistemas_distribuidos_super_secret_jwt_key_2026_lab4!"

echo "📍 Región: $LOCATION"
echo "📦 Grupo de Recursos: $RESOURCE_GROUP"
echo "🏷️  Sufijo generado: $RANDOM_SUFFIX"
echo "--------------------------------------------------------------------------"

# 2. Registrar extensiones requeridas en Azure Cloud Shell
echo "🔧 Paso 1: Verificando extensiones necesarias en Azure CLI..."
az extension add --name containerapp --upgrade --yes 2>/dev/null || true
az provider register --namespace Microsoft.App --wait
az provider register --namespace Microsoft.OperationalInsights --wait
az provider register --namespace Microsoft.DBforPostgreSQL --wait

# 3. Crear Grupo de Recursos
echo "📁 Paso 2: Creando Grupo de Recursos ($RESOURCE_GROUP)..."
az group create --name $RESOURCE_GROUP --location $LOCATION -o table

# 4. Crear Azure Container Registry (ACR)
echo "🐳 Paso 3: Aprovisionando Azure Container Registry ($ACR_NAME)..."
az acr create \
  --resource-group $RESOURCE_GROUP \
  --name $ACR_NAME \
  --sku Basic \
  --admin-enabled true \
  -o table

ACR_LOGIN_SERVER=$(az acr show --name $ACR_NAME --query loginServer -o tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query "passwords[0].value" -o tsv)

# 5. Crear Azure Storage Account (Blob Storage) & Bucket Privado
echo "💾 Paso 4: Aprovisionando Azure Storage Account ($STORAGE_NAME)..."
az storage account create \
  --name $STORAGE_NAME \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION \
  --sku Standard_LRS \
  --kind StorageV2 \
  --allow-blob-public-access false \
  -o table

STORAGE_CONN_STRING=$(az storage account show-connection-string \
  --name $STORAGE_NAME \
  --resource-group $RESOURCE_GROUP \
  --query connectionString -o tsv)

echo "🔒 Creando contenedor privado 'academic-files'..."
az storage container create \
  --name "academic-files" \
  --account-name $STORAGE_NAME \
  --auth-mode key \
  --public-access off \
  -o table

# 6. Crear Azure Database for PostgreSQL Flexible Server
echo "🐘 Paso 5: Aprovisionando Azure Database for PostgreSQL Flexible Server ($POSTGRES_SERVER)..."
az postgres flexible-server create \
  --resource-group $RESOURCE_GROUP \
  --name $POSTGRES_SERVER \
  --location $LOCATION \
  --admin-user $DB_ADMIN_USER \
  --admin-password $DB_ADMIN_PASSWORD \
  --sku-name Standard_B1ms \
  --tier Burstable \
  --storage-size 32 \
  --backup-retention 7 \
  --yes -o table

echo "🛡️ Configurando regla de firewall para servicios internos de Azure..."
az postgres flexible-server firewall-rule create \
  --resource-group $RESOURCE_GROUP \
  --name $POSTGRES_SERVER \
  --rule-name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0 -o table

echo "🗄️ Creando las 4 bases de datos independientes requeridas..."
az postgres flexible-server db create --resource-group $RESOURCE_GROUP --server-name $POSTGRES_SERVER --database-name students_db -o table
az postgres flexible-server db create --resource-group $RESOURCE_GROUP --server-name $POSTGRES_SERVER --database-name subjects_db -o table
az postgres flexible-server db create --resource-group $RESOURCE_GROUP --server-name $POSTGRES_SERVER --database-name enrollments_db -o table
az postgres flexible-server db create --resource-group $RESOURCE_GROUP --server-name $POSTGRES_SERVER --database-name auth_db -o table

POSTGRES_HOST="${POSTGRES_SERVER}.postgres.database.azure.com"

# 7. Crear Azure Cache for Redis
echo "⚡ Paso 6: Aprovisionando Azure Cache for Redis ($REDIS_NAME)..."
az redis create \
  --resource-group $RESOURCE_GROUP \
  --name $REDIS_NAME \
  --location $LOCATION \
  --sku Basic \
  --vm-size c0 \
  --enable-non-ssl-port false -o table

REDIS_KEY=$(az redis list-keys --resource-group $RESOURCE_GROUP --name $REDIS_NAME --query primaryKey -o tsv)
REDIS_HOST="${REDIS_NAME}.redis.cache.windows.net"

# 8. Crear Azure Container Apps Environment
echo "🌐 Paso 7: Aprovisionando Azure Container Apps Environment ($CONTAINER_APP_ENV)..."
az containerapp env create \
  --name $CONTAINER_APP_ENV \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION -o table

# 9. Compilación de Imágenes Docker en la nube con ACR Tasks
echo "🔨 Paso 8: Compilando imágenes Docker en ACR (construcción remota en la nube)..."

echo "  -> Compilando auth-service..."
az acr build --registry $ACR_NAME --image auth-service:latest ./auth-service-main

echo "  -> Compilando students-service..."
az acr build --registry $ACR_NAME --image students-service:latest ./students-service-main

echo "  -> Compilando subjects-service..."
az acr build --registry $ACR_NAME --image subjects-service:latest ./subjects-service-main

echo "  -> Compilando enrollments-service..."
az acr build --registry $ACR_NAME --image enrollments-service:latest ./enrollments-service-main

echo "  -> Compilando api-gateway..."
az acr build --registry $ACR_NAME --image api-gateway:latest ./api-gateway-main

echo "  -> Compilando frontend..."
az acr build --registry $ACR_NAME --image frontend:latest ./frontend

# 10. Despliegue de Microservicios Internos (Sin acceso público)
echo "🚀 Paso 9: Desplegando microservicios en Azure Container Apps (Ingress Privado)..."

# auth-service
az containerapp create \
  --name auth-service \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINER_APP_ENV \
  --image "${ACR_LOGIN_SERVER}/auth-service:latest" \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_NAME \
  --registry-password $ACR_PASSWORD \
  --target-port 8084 \
  --ingress internal \
  --cpu 0.25 --memory 0.5Gi \
  --env-vars \
    SERVER_PORT=8084 \
    DB_HOST=$POSTGRES_HOST \
    DB_PORT=5432 \
    DB_NAME=auth_db \
    DB_USERNAME=$DB_ADMIN_USER \
    DB_PASSWORD=$DB_ADMIN_PASSWORD \
    JWT_SECRET=$JWT_SECRET -o table

# students-service
az containerapp create \
  --name students-service \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINER_APP_ENV \
  --image "${ACR_LOGIN_SERVER}/students-service:latest" \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_NAME \
  --registry-password $ACR_PASSWORD \
  --target-port 8081 \
  --ingress internal \
  --cpu 0.25 --memory 0.5Gi \
  --env-vars \
    SERVER_PORT=8081 \
    DB_HOST=$POSTGRES_HOST \
    DB_PORT=5432 \
    DB_NAME=students_db \
    DB_USERNAME=$DB_ADMIN_USER \
    DB_PASSWORD=$DB_ADMIN_PASSWORD \
    AZURE_STORAGE_CONNECTION_STRING="$STORAGE_CONN_STRING" \
    AZURE_STORAGE_CONTAINER_NAME=academic-files -o table

# subjects-service
az containerapp create \
  --name subjects-service \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINER_APP_ENV \
  --image "${ACR_LOGIN_SERVER}/subjects-service:latest" \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_NAME \
  --registry-password $ACR_PASSWORD \
  --target-port 8082 \
  --ingress internal \
  --cpu 0.25 --memory 0.5Gi \
  --env-vars \
    SERVER_PORT=8082 \
    DB_HOST=$POSTGRES_HOST \
    DB_PORT=5432 \
    DB_NAME=subjects_db \
    DB_USERNAME=$DB_ADMIN_USER \
    DB_PASSWORD=$DB_ADMIN_PASSWORD \
    AZURE_STORAGE_CONNECTION_STRING="$STORAGE_CONN_STRING" \
    AZURE_STORAGE_CONTAINER_NAME=academic-files -o table

# enrollments-service
az containerapp create \
  --name enrollments-service \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINER_APP_ENV \
  --image "${ACR_LOGIN_SERVER}/enrollments-service:latest" \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_NAME \
  --registry-password $ACR_PASSWORD \
  --target-port 8083 \
  --ingress internal \
  --cpu 0.25 --memory 0.5Gi \
  --env-vars \
    SERVER_PORT=8083 \
    DB_HOST=$POSTGRES_HOST \
    DB_PORT=5432 \
    DB_NAME=enrollments_db \
    DB_USERNAME=$DB_ADMIN_USER \
    DB_PASSWORD=$DB_ADMIN_PASSWORD -o table

# 11. Despliegue de API Gateway (Ingress Público con HTTPS)
echo "🚪 Paso 10: Desplegando API Gateway con Ingress Público (HTTPS)..."
az containerapp create \
  --name api-gateway \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINER_APP_ENV \
  --image "${ACR_LOGIN_SERVER}/api-gateway:latest" \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_NAME \
  --registry-password $ACR_PASSWORD \
  --target-port 8080 \
  --ingress external \
  --cpu 0.5 --memory 1.0Gi \
  --env-vars \
    SERVER_PORT=8080 \
    AUTH_SERVICE_URL="http://auth-service" \
    STUDENTS_SERVICE_URL="http://students-service" \
    SUBJECTS_SERVICE_URL="http://subjects-service" \
    ENROLLMENTS_SERVICE_URL="http://enrollments-service" \
    REDIS_HOST=$REDIS_HOST \
    REDIS_PORT=6380 \
    REDIS_PASSWORD=$REDIS_KEY \
    REDIS_SSL=true \
    JWT_SECRET=$JWT_SECRET \
    GATEWAY_SECURITY_ENABLED=true \
    GATEWAY_CACHE_ENABLED=true \
    GATEWAY_CACHE_TTL_SECONDS=180 -o table

GATEWAY_FQDN=$(az containerapp show --name api-gateway --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv)
GATEWAY_PUBLIC_URL="https://${GATEWAY_FQDN}"

# 12. Despliegue de Frontend (Ingress Público con HTTPS)
echo "💻 Paso 11: Desplegando Frontend con Ingress Público (HTTPS)..."
az containerapp create \
  --name frontend \
  --resource-group $RESOURCE_GROUP \
  --environment $CONTAINER_APP_ENV \
  --image "${ACR_LOGIN_SERVER}/frontend:latest" \
  --registry-server $ACR_LOGIN_SERVER \
  --registry-username $ACR_NAME \
  --registry-password $ACR_PASSWORD \
  --target-port 80 \
  --ingress external \
  --cpu 0.25 --memory 0.5Gi \
  --env-vars \
    VITE_API_GATEWAY_URL=$GATEWAY_PUBLIC_URL -o table

FRONTEND_FQDN=$(az containerapp show --name frontend --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv)
FRONTEND_PUBLIC_URL="https://${FRONTEND_FQDN}"

echo "=========================================================================="
echo "🎉 ¡DESPLIEGUE EN AZURE COMPLETADO CON ÉXITO!"
echo "=========================================================================="
echo "🌐 URL Pública del Frontend (HTTPS):  $FRONTEND_PUBLIC_URL"
echo "🚪 URL Pública del Gateway (HTTPS):   $GATEWAY_PUBLIC_URL"
echo "🩺 Gateway Health Check:              ${GATEWAY_PUBLIC_URL}/actuator/health"
echo "📖 Swagger UI del Gateway:            ${GATEWAY_PUBLIC_URL}/swagger-ui.html"
echo "=========================================================================="
echo "🔑 Credenciales de Prueba:"
echo "   • Admin:      admin@uptc.edu.co      / Admin123*"
echo "   • Estudiante: estudiante1@uptc.edu.co / Student123*"
echo "=========================================================================="

