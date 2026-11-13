#!/bin/sh

# Database Profile
export DATABASE_PROFILE="postgres"

# Local PostgreSQL Database Configuration
export DATABASE_URL="jdbc:postgresql://<domain>"
export DATABASE_USERNAME="user"       # change this
export DATABASE_PASSWORD="password"   # change this

# JWT Secret for local auth
export JWT_SECRET="aSecureRandomGeneratedKeyThatIsAtLeast32BytesLong!"  # change this

# Storage Type
export STORAGE_TYPE="local"

# Local Storage Settings
export STORAGE_LOCATION="/path/to/storage/"
export STORAGE_BASEURL="http://localhost:8080/storage/"

# Cloud Storage Settings
export CLOUD_BUCKET="your-bucket"
export CLOUD_BASEURL="https://storage.googleapis.com/example-url/"

# Google Credentials
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/credentials/arched-sorter-1234-56-789.json"

# Run Project
mvn spring-boot:run