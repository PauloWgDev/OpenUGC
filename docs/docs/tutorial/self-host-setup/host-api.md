---
sidebar_position: 3
---

# Self-Hosting OpenUGC

If you want full control over your deployment, you can run **OpenUGC** directly on your own machine or server, without Docker.
This guide walks you through installing the required tools, setting up the environment variables, and running the API.
The examples show both Windows (PowerShell) and Linux/macOS.

Before starting, you need a PostgreSQL database. If you don't have one yet, follow [Self-Hosting Database](./host-database.md) first.

---

## 1. Install Prerequisites

- **[Java 17](https://adoptium.net/)** (required, Spring Boot runs on it)
- **[Postman](https://www.postman.com/)** (optional, to test the endpoints)

You don't need to install Maven: the repository includes the Maven Wrapper (`mvnw` / `mvnw.cmd`), which downloads the right version the first time you use it.

---

## 2. Clone the Repository

Open a terminal and run:

```bash
git clone https://github.com/PauloWgDev/OpenUGC.git
cd OpenUGC
```

---

## 3. Configure Environment Variables

OpenUGC reads its settings from environment variables. The repository includes two example scripts that set them and start the API:

- `run.example.ps1` for Windows (PowerShell)
- `run.example.sh` for Linux and macOS

Copy the one for your system. The copies (`run.ps1` and `run.sh`) are ignored by git, so your passwords won't end up in the repository.

```bash
# Windows (PowerShell)
copy run.example.ps1 run.ps1

# Linux / macOS
cp run.example.sh run.sh
```

Open the copy and fill in your values. For example, on Windows:

```powershell
# Database
$env:DATABASE_PROFILE = "postgres"
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/openugc"
$env:DATABASE_USERNAME = "postgres"
$env:DATABASE_PASSWORD = "your-database-password"

# JWT secret (required, at least 32 bytes)
$env:JWT_SECRET = "<your-generated-secret>"

# Storage
$env:STORAGE_TYPE = "local"
$env:STORAGE_LOCATION = "C:/OpenUGC/storage"

# Run the project
.\mvnw.cmd spring-boot:run
```

And on Linux/macOS:

```bash
#!/bin/sh

# Database
export DATABASE_PROFILE="postgres"
export DATABASE_URL="jdbc:postgresql://localhost:5432/openugc"
export DATABASE_USERNAME="postgres"
export DATABASE_PASSWORD="your-database-password"

# JWT secret (required, at least 32 bytes)
export JWT_SECRET="<your-generated-secret>"

# Storage
export STORAGE_TYPE="local"
export STORAGE_LOCATION="/var/lib/openugc/storage"

# Run the project
./mvnw spring-boot:run
```

A few notes:

- **`JWT_SECRET`** signs the login tokens. It must be at least 32 bytes, and the API won't start with a shorter value or with the `CHANGE_ME` placeholder. Generate one with:

  ```bash
  # Linux / macOS (or Git Bash on Windows)
  openssl rand -base64 48
  ```

  ```powershell
  # Windows PowerShell
  $b = New-Object byte[] 48; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
  ```

  If you change the secret later, every token that was already issued stops working and players have to log in again.

- **`STORAGE_LOCATION`** is the folder where uploaded files are saved. It is created if it doesn't exist.
- **Google Cloud Storage:** to store the files in a bucket instead, set `STORAGE_TYPE` to `cloud` and add `CLOUD_BUCKET` and `GOOGLE_APPLICATION_CREDENTIALS`. See [Host with Google Cloud Storage](../third-party-setup/host-storage.md). When you use cloud storage, `STORAGE_LOCATION` is not needed.
- The example scripts call `mvn`. If you don't have Maven installed, change that line to `./mvnw spring-boot:run` (or `.\mvnw.cmd spring-boot:run` on Windows) as shown above.

The full list of settings is on the [Configuration](../configuration.md) page.

Save the file and run it:

```bash
# Windows (PowerShell)
.\run.ps1

# Linux / macOS
sh run.sh
```

:::note
If PowerShell says that running scripts is disabled, run `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` once, then try again.
:::

---

## 4. Verify the API is Running

The first run takes a while because the wrapper downloads Maven and the dependencies. When you see a line like `Started OpenUgcApplication`, the API is ready on port `8080`.

Open this URL in your browser:

```
http://localhost:8080/actuator/health
```

If everything is fine, the response contains `"status":"UP"`.

You can also open `http://localhost:8080/swagger-ui.html` to see all the endpoints and try them from the browser.

---

## 5. Testing the API

Before connecting your game, it's a good idea to test the main flow. The examples below use cURL; in Postman you can send the same requests.

### Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "password": "mypassword"}'
```

The username must be 3 to 32 characters and the password 8 to 72 characters. If it works, the response is:

```
User registered successfully!
```

### Login

The login body uses `credential` for the password:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "credential": "mypassword"}'
```

The response includes a JWT token:

```json
{
  "token": "eyJhbGciOi..."
}
```

### Test an Authenticated Endpoint

Send the token in the `Authorization` header:

```bash
curl "http://localhost:8080/api/content?page=0&size=5" \
  -H "Authorization: Bearer <your-jwt-token>"
```

It returns a page of content. On a new install the list is empty:

```json
{
  "content": [],
  "page": {
    "size": 5,
    "number": 0,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

:::note
On Windows PowerShell, `curl` can point to a different command. Use `curl.exe` instead, and write the JSON body with escaped quotes, for example `-d "{\"username\": \"testuser\", \"password\": \"mypassword\"}"`.
:::

---

## Next Steps

- [Create the first admin account](../first-admin.md).
- Read [API Basics](../../documentation/api-basics.md) to see how tokens, pagination and errors work.
- If the API will be reachable from the internet, put it behind a reverse proxy with HTTPS (for example nginx or Caddy), and check the reverse proxy notes on the [Configuration](../configuration.md#running-behind-a-reverse-proxy) page.
