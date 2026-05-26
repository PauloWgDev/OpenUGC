#!/bin/sh

# Database Profile
export DATABASE_PROFILE="postgres"

# Local PostgreSQL Database Configuration
export DATABASE_URL="jdbc:postgresql://<domain>"
export DATABASE_USERNAME="user"
export DATABASE_PASSWORD="password"

# JWT Secret for local auth
export JWT_SECRET="aSecureRandomGeneratedKeyThatIsAtLeast32BytesLong!"

# Storage Type
export STORAGE_TYPE="local"

# Local Storage Settings
export STORAGE_LOCATION="/path/to/storage/"
export STORAGE_BASEURL="http://localhost:8080/storage/"

# Cloud Storage Settings
export CLOUD_BUCKET="u3gc-uploads"
export CLOUD_BASEURL="https://storage.googleapis.com/openugc-uploads/"

# Google Credentials
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/credentials/arched-sorter-459106-f6-be936d47ab6a.json"

# Run Project
mvn spring-boot:run