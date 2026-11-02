-- Initialization script for PostgreSQL in Local and Azure Environments
-- Creates 4 independent databases according to Distributed Systems Lab 4 requirements

CREATE DATABASE students_db;
CREATE DATABASE subjects_db;
CREATE DATABASE enrollments_db;
CREATE DATABASE auth_db;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE students_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE subjects_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE enrollments_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE auth_db TO postgres;

