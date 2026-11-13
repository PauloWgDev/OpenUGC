# Automatically set all the environment variables and run the project

# Database Profile
$env:DATABASE_PROFILE = "postgres"

# Local PostgreSQL Database Configuration
$env:DATABASE_URL = "jdbc:postgresql://<domain>"
$env:DATABASE_USERNAME = "user"             # change this
$env:DATABASE_PASSWORD = "secretPassword"   # change this

# JWT Secret for local auth
$env:JWT_SECRET = "aSecureRandomGeneratedKeyThatIsAtLeast32BytesLong!"      # change this

# Storage Type
$env:STORAGE_TYPE = "local"

# Local Storage Settings
$env:STORAGE_LOCATION = "C:=/path/to/storage/"

# Cloud Storage Settings
$env:CLOUD_BUCKET = "your-bucket"

# Google
$env:GOOGLE_APPLICATION_CREDENTIALS = "F:/path/to/credentials/arched-sorter-123-45-67890.json"

# Run Project
mvn spring-boot:run