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
LOCATION="${LOCATION:-northcentralus}"
RESOURCE_GROUP="${RESOURCE_GROUP:-rg-distribuidos-lab4-prod}"

# Detección inteligente de recursos ya creados en el grupo
if [ -z "$RANDOM_SUFFIX" ]; then
  EXISTING_ACR=$(az acr list --resource-group "$RESOURCE_GROUP" --query "[0].name" -o tsv 2>/dev/null || true)
  if [ -n "$EXISTING_ACR" ]; then
    RANDOM_SUFFIX=${EXISTING_ACR#acrdislab4}
    echo "♻️  Detectados recursos existentes con sufijo: $RANDOM_SUFFIX"
  else
    RANDOM_SUFFIX=$(cat /dev/urandom | tr -dc 'a-z0-9' | fold -w 5 | head -n 1)
  fi
fi

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
echo "🏷️  Sufijo: $RANDOM_SUFFIX"
echo "--------------------------------------------------------------------------"

# 2. Registrar extensiones requeridas en Azure Cloud Shell
echo "🔧 Paso 1: Verificando extensiones y proveedores en Azure CLI..."
az extension add --name containerapp --upgrade --yes 2>/dev/null || true
az provider register --namespace Microsoft.ContainerRegistry --wait
az provider register --namespace Microsoft.Cache --wait
az provider register --namespace Microsoft.Storage --wait
az provider register --namespace Microsoft.App --wait
az provider register --namespace Microsoft.OperationalInsights --wait
az provider register --namespace Microsoft.DBforPostgreSQL --wait

# 3. Crear Grupo de Recursos
echo "📁 Paso 2: Verificando Grupo de Recursos ($RESOURCE_GROUP)..."
az group show --name $RESOURCE_GROUP -o none 2>/dev/null || \
  az group create --name $RESOURCE_GROUP --location $LOCATION -o table

# 4. Crear Azure Container Registry (ACR)
echo "🐳 Paso 3: Verificando Azure Container Registry ($ACR_NAME)..."
az acr show --name $ACR_NAME --resource-group $RESOURCE_GROUP -o none 2>/dev/null || \
  az acr create \
    --resource-group $RESOURCE_GROUP \
    --name $ACR_NAME \
    --sku Basic \
    --admin-enabled true \
    -o table

ACR_LOGIN_SERVER=$(az acr show --name $ACR_NAME --query loginServer -o tsv)
ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query "passwords[0].value" -o tsv)

# 5. Crear Azure Storage Account (Blob Storage) & Bucket Privado
echo "💾 Paso 4: Verificando Azure Storage Account ($STORAGE_NAME)..."
az storage account show --name $STORAGE_NAME --resource-group $RESOURCE_GROUP -o none 2>/dev/null || \
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

echo "🔒 Verificando contenedor privado 'academic-files'..."
az storage container create \
  --name "academic-files" \
  --account-name $STORAGE_NAME \
  --auth-mode key \
  --public-access off \
  -o table 2>/dev/null || true

# 6. Crear Azure Database for PostgreSQL Flexible Server
echo "🐘 Paso 5: Verificando PostgreSQL Flexible Server ($POSTGRES_SERVER)..."
az postgres flexible-server show --name $POSTGRES_SERVER --resource-group $RESOURCE_GROUP -o none 2>/dev/null || \
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
    --public-access 0.0.0.0 \
    --yes

echo "🛡️ Configurando regla de firewall para servicios de Azure..."
az postgres flexible-server firewall-rule create \
  --resource-group $RESOURCE_GROUP \
  --server-name $POSTGRES_SERVER \
  --name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0 -o none 2>/dev/null || true

az postgres flexible-server firewall-rule create \
  --resource-group $RESOURCE_GROUP \
  --server-name $POSTGRES_SERVER \
  --name AllowAllTraffic \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 255.255.255.255 -o none 2>/dev/null || true

echo "⚙️ Ajustando parámetro require_secure_transport para máxima compatibilidad..."
az postgres flexible-server parameter set \
  --resource-group $RESOURCE_GROUP \
  --server-name $POSTGRES_SERVER \
  --name require_secure_transport \
  --value off -o none 2>/dev/null || true

echo "🗄️ Creando las 4 bases de datos independientes requeridas..."
az postgres flexible-server db create --resource-group $RESOURCE_GROUP --server-name $POSTGRES_SERVER --name students_db -o none 2>/dev/null || true
az postgres flexible-server db create --resource-group $RESOURCE_GROUP --server-name $POSTGRES_SERVER --name subjects_db -o none 2>/dev/null || true
az postgres flexible-server db create --resource-group $RESOURCE_GROUP --server-name $POSTGRES_SERVER --name enrollments_db -o none 2>/dev/null || true
az postgres flexible-server db create --resource-group $RESOURCE_GROUP --server-name $POSTGRES_SERVER --name auth_db -o none 2>/dev/null || true

POSTGRES_HOST="${POSTGRES_SERVER}.postgres.database.azure.com"

# 7. Crear Azure Managed Redis (Redis Enterprise)
echo "⚡ Paso 6: Aprovisionando Azure Managed Redis ($REDIS_NAME)..."
az extension add --name redisenterprise --upgrade --yes 2>/dev/null || true

EXISTING_REDIS=$(az redisenterprise list --resource-group "$RESOURCE_GROUP" --query "[0].name" -o tsv 2>/dev/null || true)
if [ -n "$EXISTING_REDIS" ]; then
  REDIS_NAME="$EXISTING_REDIS"
  echo "♻️  Reutilizando instancia de Azure Managed Redis existente: $REDIS_NAME"
else
  az redisenterprise create \
    --cluster-name "$REDIS_NAME" \
    --resource-group "$RESOURCE_GROUP" \
    --location "$LOCATION" \
    --sku "Balanced_B0" \
    --minimum-tls-version "1.2" \
    --public-network-access "Enabled" \
    --access-keys-auth "Enabled" \
    -o none
fi

REDIS_KEY=$(az redisenterprise database list-keys --cluster-name "$REDIS_NAME" --resource-group "$RESOURCE_GROUP" --name default --query primaryKey -o tsv 2>/dev/null || az redisenterprise database list-keys --cluster-name "$REDIS_NAME" --resource-group "$RESOURCE_GROUP" --query primaryKey -o tsv 2>/dev/null || true)
REDIS_HOST=$(az redisenterprise show --name "$REDIS_NAME" --resource-group "$RESOURCE_GROUP" --query hostName -o tsv)
REDIS_PORT=10000

# 8. Crear Azure Container Apps Environment
echo "🌐 Paso 7: Aprovisionando Azure Container Apps Environment ($CONTAINER_APP_ENV)..."
az containerapp env show --name $CONTAINER_APP_ENV --resource-group $RESOURCE_GROUP -o none 2>/dev/null || \
  az containerapp env create \
    --name $CONTAINER_APP_ENV \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION -o table

# 9. Verificación / Compilación de Imágenes Docker en ACR
echo "🔨 Paso 8: Verificando imágenes Docker en ACR..."

for img in auth-service students-service subjects-service enrollments-service api-gateway frontend; do
  if az acr repository show --name $ACR_NAME --repository "${img}" -o none 2>/dev/null; then
    echo "  -> ✅ Imagen ${img}:latest ya está presente en ACR."
  else
    echo "  -> Compilando ${img} en la nube (ACR Task)..."
    az acr build --registry $ACR_NAME --image "${img}:latest" "./${img}-main" 2>/dev/null || \
      echo "  -> (Nota: Si la suscripción restringe ACR Tasks, la imagen debe subirse vía docker push local)."
  fi
done

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
    "SERVER_PORT=8084" \
    "DB_HOST=${POSTGRES_HOST}" \
    "DB_PORT=5432" \
    "DB_NAME=auth_db" \
    "DB_USERNAME=${DB_ADMIN_USER}" \
    "DB_PASSWORD=${DB_ADMIN_PASSWORD}" \
    "JWT_SECRET=${JWT_SECRET}" -o table

az containerapp ingress update --name auth-service --resource-group $RESOURCE_GROUP --allow-insecure true -o none 2>/dev/null || true

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
    "SERVER_PORT=8081" \
    "DB_HOST=${POSTGRES_HOST}" \
    "DB_PORT=5432" \
    "DB_NAME=students_db" \
    "DB_USERNAME=${DB_ADMIN_USER}" \
    "DB_PASSWORD=${DB_ADMIN_PASSWORD}" \
    "AZURE_STORAGE_CONNECTION_STRING=${STORAGE_CONN_STRING}" \
    "AZURE_STORAGE_CONTAINER_NAME=academic-files" \
    "SEED_ENABLED=true" -o table

az containerapp ingress update --name students-service --resource-group $RESOURCE_GROUP --allow-insecure true -o none 2>/dev/null || true

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
    "SERVER_PORT=8082" \
    "DB_HOST=${POSTGRES_HOST}" \
    "DB_PORT=5432" \
    "DB_NAME=subjects_db" \
    "DB_USERNAME=${DB_ADMIN_USER}" \
    "DB_PASSWORD=${DB_ADMIN_PASSWORD}" \
    "AZURE_STORAGE_CONNECTION_STRING=${STORAGE_CONN_STRING}" \
    "AZURE_STORAGE_CONTAINER_NAME=academic-files" \
    "SEED_ENABLED=true" -o table

az containerapp ingress update --name subjects-service --resource-group $RESOURCE_GROUP --allow-insecure true -o none 2>/dev/null || true

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
    "SERVER_PORT=8083" \
    "DB_HOST=${POSTGRES_HOST}" \
    "DB_PORT=5432" \
    "DB_NAME=enrollments_db" \
    "DB_USERNAME=${DB_ADMIN_USER}" \
    "DB_PASSWORD=${DB_ADMIN_PASSWORD}" -o table

az containerapp ingress update --name enrollments-service --resource-group $RESOURCE_GROUP --allow-insecure true -o none 2>/dev/null || true

# 11. Despliegue de API Gateway (Ingress Público con HTTPS)
echo "🚪 Paso 10: Desplegando API Gateway con Ingress Público (HTTPS)..."

AUTH_FQDN=$(az containerapp show --name auth-service --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv)
STUDENTS_FQDN=$(az containerapp show --name students-service --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv)
SUBJECTS_FQDN=$(az containerapp show --name subjects-service --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv)
ENROLLMENTS_FQDN=$(az containerapp show --name enrollments-service --resource-group $RESOURCE_GROUP --query properties.configuration.ingress.fqdn -o tsv)

echo "🔗 Rutas internas FQDN:"
echo "   • Auth:        https://${AUTH_FQDN}"
echo "   • Students:    https://${STUDENTS_FQDN}"
echo "   • Subjects:    https://${SUBJECTS_FQDN}"
echo "   • Enrollments: https://${ENROLLMENTS_FQDN}"

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
    "SERVER_PORT=8080" \
    "AUTH_SERVICE_URL=https://${AUTH_FQDN}" \
    "STUDENTS_SERVICE_URL=https://${STUDENTS_FQDN}" \
    "SUBJECTS_SERVICE_URL=https://${SUBJECTS_FQDN}" \
    "ENROLLMENTS_SERVICE_URL=https://${ENROLLMENTS_FQDN}" \
    "REDIS_HOST=${REDIS_HOST}" \
    "REDIS_PORT=${REDIS_PORT}" \
    "REDIS_PASSWORD=${REDIS_KEY}" \
    "REDIS_SSL=true" \
    "JWT_SECRET=${JWT_SECRET}" \
    "GATEWAY_SECURITY_ENABLED=true" \
    "GATEWAY_CACHE_ENABLED=true" \
    "GATEWAY_CACHE_TTL_SECONDS=180" -o table

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
    "VITE_API_GATEWAY_URL=${GATEWAY_PUBLIC_URL}" -o table

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

