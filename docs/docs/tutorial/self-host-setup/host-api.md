---
sidebar_position: 3
---

# Self-Hosting OpenUGC

If you want full control over your deployment, you can run **OpenUGC** locally on your own machine.  
This guide walks you through installing the required tools, setting up environment variables, and running the API.
On this guide we will show how to host it on Windows, but you can still use this as guide and do the equivalent for Linux or MacOS.

---

## 1. Install Prerequisites

Before running OpenUGC locally, make sure you have the following installed:

- **[Java 17](https://adoptium.net/)** (required for Spring Boot)
- **[Maven](https://maven.apache.org/)** (for building and running the project)
- **[Docker Desktop](https://www.docker.com/)** (optional, in case you prefere running it in a container)
- **[Postman](https://www.postman.com/)** (optional, but recommended to test the API endpoints)

---

## 2. Clone the Repository

Open a terminal and run:

```bash
git clone https://github.com/PauloWgDev/OpenUGC.git
cd OpenUGC
```

---

## 3. Configure Environment Variables

OpenUGC uses environment variables for database, authentication, and storage configuration.

To make this easy, you can create a PowerShell script (run.ps1) like this:

```
# Automatically set all the environment variables and run the project

# Database Profile
$env:DATABASE_PROFILE = "postgres"

# Local PostgreSQL Database Configuration
$env:DATABASE_URL = "jdbc:postgresql://localhost:5433/openugc"
$env:DATABASE_USERNAME = "postgres"
$env:DATABASE_PASSWORD = "secretpassword"

# JWT Secret for local auth
$env:JWT_SECRET = "aSecureRandomGeneratedKeyThatIsAtLeast32BytesLong!"

# Storage Type
$env:STORAGE_TYPE = "local"

# Local Storage Settings
$env:STORAGE_LOCATION = "F:/Capstone/API/unityapi/UGC_Storage"
$env:STORAGE_BASEURL = "http://localhost:8080/storage/"

# Cloud Storage Settings
$env:CLOUD_BUCKET = "u3gc-uploads"
$env:CLOUD_BASEURL = "https://storage.googleapis.com/open-uploads/"

# Google
$env:GOOGLE_APPLICATION_CREDENTIALS = "F:/Path/To/Credentials/arched-sorter-459106-f6-be936d47ab6a.json"

# Run Project
mvn spring-boot:run

```

Save the file and run it in PowerShell

```
.\run.ps1
```

---

## 4. Verify the API is Running

Once the project compiles and runs, the API should be available at:

```
http://localhost:8080/api
```

## 5. Testing with Postman

Before connecting to your game engine, it’s a good idea to test endpoints with Postman.

### Register a User

Method: POST

URL: http://localhost:8080/api/auth/register

Body (JSON):

```
{
  "username": "testuser",
  "password": "mypassword",
}
```

If successful, you’ll get back a success message.

### Login

**Method**: POST

**URL**: http://localhost:8080/api/auth/login

**Body (JSON)**:

```
{
  "username": "testuser",
  "password": "mypassword"
}

```

Response will include a JWT token you can use for authenticated requests.

### Test Authenticated Endpoints
**Method**: GET

**URL**: `http://localhost:8080/api/content?page=0&size=5`

**Headers**:
```
Authorization: Bearer <your-jwt-token>
```

**cURL example**:
```
curl -X GET "http://localhost:8080/api/content?page=0&size=5" \
  -H "Authorization: Bearer <your-jwt-token>"
```

✅ Returns a paginated list of content, e.g.:

```
{
  "content": [],
  "pageable": { "pageNumber": 0, "pageSize": 5 },
  "totalElements": 0
}
```