---
sidebar_position: 0
---

# Run with Docker Compose

This is the quickest way to run OpenUGC. Docker Compose starts two containers: one with the API and one with a PostgreSQL database. Uploaded files are saved in a folder on your machine.

---

## 1. Install Docker

Install **[Docker Desktop](https://www.docker.com/products/docker-desktop/)** (Windows and macOS) or **Docker Engine** with the Compose plugin (Linux).

You don't need Java, Maven or PostgreSQL installed, since everything runs inside the containers.

---

## 2. Clone the Repository

```bash
git clone https://github.com/PauloWgDev/OpenUGC.git
cd OpenUGC
```

---

## 3. Create Your Compose File

The repository includes `docker-compose-example.yml`. Copy it to `docker-compose.yml`:

```bash
# Linux / macOS
cp docker-compose-example.yml docker-compose.yml

# Windows (PowerShell)
copy docker-compose-example.yml docker-compose.yml
```

Then open `docker-compose.yml` and change these values:

1. **Database user and password.** `DATABASE_USERNAME` and `DATABASE_PASSWORD` on the `open-ugc` service must be the same as `POSTGRES_USER` and `POSTGRES_PASSWORD` on the `postgres` service. Change both from the example values.
2. **JWT secret.** Replace `JWT_SECRET: CHANGE_ME` with your own secret. The API refuses to start with the placeholder or with a secret shorter than 32 bytes. You can generate one with:

   ```bash
   openssl rand -base64 48
   ```

3. **Google Cloud lines (only if you use local storage).** The example mounts `./credentials/google.json` for Google Cloud Storage. If you keep `STORAGE_TYPE: local`, you can delete the `CLOUD_BUCKET` and `GOOGLE_APPLICATION_CREDENTIALS` lines and the `./credentials/google.json` volume line. Otherwise Docker expects that file to exist.


---

## 4. Start It

```bash
docker compose up --build
```

The first build takes a few minutes. 

When it's done, the API is available at `http://localhost:8080`, and uploaded files are saved in the `./storage` folder.

---

## 5. Check It Works

Open `http://localhost:8080/actuator/health` in your browser. If everything is fine, the response contains `"status":"UP"`.

You can also open `http://localhost:8080/swagger-ui.html` to see all the endpoints and try them.

To test the main flow (register, log in and list content), follow the [Testing the API](./host-api.md#5-testing-the-api) section of the self-host guide. It works the same way here.

---

## Useful Commands

| Command | What it does |
|---------|--------------|
| `docker compose up -d --build` | Starts everything in the background |
| `docker compose logs -f open-ugc` | Shows the API logs |
| `docker compose down` | Stops everything. The data is kept |
| `docker compose down -v` | Stops everything and **deletes the database data** |

After changing the code or pulling a new version, run `docker compose up --build` again to rebuild the API.

For every setting you can add to the `environment` section, see the [Configuration](../configuration.md) page.
