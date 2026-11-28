<p align="center">
  <img src="./docs/static/img/logo3.svg" alt="OpenUGC logo" width="160">
</p>

<h1 align="center">OpenUGC</h1>

<p align="center">
  A self-hostable, open-source backend for <b>user-generated content</b>.<br>
  Players upload, browse, rate and download content.
</p>

<p align="center">
  <a href="https://paulowgdev.github.io/OpenUGC/">Documentation</a> ·
  <a href="#quick-start">Quick start</a> ·
  <a href="#api-overview">API overview</a> ·
  <a href="#contributing">Contributing</a>
</p>

<p align="center">
  <img alt="Java 17" src="https://img.shields.io/badge/Java-17-orange">
  <img alt="Spring Boot 3.5" src="https://img.shields.io/badge/Spring%20Boot-3.4-6DB33F">
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-17-336791">
  <img alt="License: Apache 2.0" src="https://img.shields.io/badge/License-Apache%202.0-blue">
</p>

---

## Why OpenUGC?

Unity’s UGC service spent years in beta without some of the basics a UGC system needs, and in 2025 Unity announced it was shutting it down, leaving the games that relied on it without a solution.

OpenUGC was built as an open-source alternative. It isn’t tied to any engine: anything that can make HTTP requests (Unity, Godot, Unreal, a web launcher, a mod manager) can use it.

And because you host it yourself, you own the data, the storage and the rules, so it can’t be taken away from you.

## Features

- **Accounts & auth**: registration, login and stateless JWT authentication (tokens last 24 h).
- **Guest access**: optional guest tokens so players can browse without an account; you decide whether guests may upload.
- **Roles**: `ADMIN`, `USER` and `GUEST`, created automatically on first start. Admin-only endpoints live under `/api/v1/admin`.
- **Content**: multipart upload of a file plus an optional thumbnail, with name, description, version and tags.
- **Search**: paginated, sortable listing filtered by text, tags or creator.
- **Ratings**: per-content ratings with optional comments, plus a rating distribution endpoint.
- **Stats**: total downloads, downloads in the last 7 days, rating count and average rating per item.
- **Download tracking**: every download is recorded with the client IP.
- **Pluggable storage**: local disk or Google Cloud Storage, selected by one environment variable.
- **Soft & hard delete** for content.
- **Rate limiting** on register, login and guest login (5 requests per minute per IP).
- **OpenAPI / Swagger UI** generated from the code.

## Built with

| Layer          | Technology                                |
|----------------|-------------------------------------------|
| Language       | Java 17                                   |
| Framework      | Spring Boot 3.5 (Web, Data JPA, Security) |
| Database       | PostgreSQL                                |
| Auth           | JWT (jjwt), BCrypt password hashing       |
| Storage        | Local filesystem or Google Cloud Storage  |
| Rate limiting  | Bucket4j                                  |
| API docs       | springdoc-openapi (Swagger UI)            |
| Docs site      | Docusaurus (in [`/docs`](./docs))         |

## Quick start

### Option A — Docker Compose (recommended)

Requires Docker and Docker Compose.

```bash
git clone https://github.com/PauloWgDev/OpenUGC.git
cd OpenUGC
```

Create your own compose file from the example. `docker-compose.yml` is git-ignored, so your passwords and secret stay out of the repo:

```bash
cp docker-compose-example.yml docker-compose.yml
```

Open `docker-compose.yml` and check these before starting:

1. Change the database username and password. `DATABASE_USERNAME` / `DATABASE_PASSWORD` on the `open-ugc` service must match `POSTGRES_USER` / `POSTGRES_PASSWORD` on the `postgres` service.
2. Replace `JWT_SECRET: CHANGE_ME` with your own secret, at least 32 characters. The app won't start with the placeholder.

Then:

```bash
docker compose up --build
```

The API is now on `http://localhost:8080` and uploaded files are kept in `./storage`.

### Option B — Run locally with Maven

Requires Java 17 and a running PostgreSQL instance.

```bash
cp run.example.sh run.sh        # Windows: copy run.example.ps1 run.ps1
# edit run.sh with your database URL, credentials, JWT secret and storage settings
sh run.sh
```

`run.sh` and `run.ps1` are git-ignored so your secrets stay out of the repo.

### Check it works

```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"player1","password":"secret123"}'

# Log in and grab the token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"player1","credential":"secret123"}'
# -> {"token":"eyJhbGciOi..."}

# List content
curl http://localhost:8080/api/v1/content \
  -H "Authorization: Bearer <token>"
```

Interactive API docs are available at **`http://localhost:8080/swagger-ui.html`** while the API is running. To try protected endpoints, log in with `POST /api/v1/auth/login`, click **Authorize** and paste the token. To use Postman instead, import `http://localhost:8080/v3/api-docs` (**Import → Link**) and Postman builds the collection from the server, so it always matches the code.

## Configuration

All settings are read from environment variables.

| Variable                         | Required        | Description                                                              |
|----------------------------------|-----------------|--------------------------------------------------------------------------|
| `DATABASE_PROFILE`               | yes             | Database profile. Currently only `postgres` is supported.                |
| `DATABASE_URL`                   | yes             | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/openugc`                |
| `DATABASE_USERNAME`              | yes             | Database user                                                            |
| `DATABASE_PASSWORD`              | yes             | Database password                                                        |
| `JWT_SECRET`                     | yes             | Secret used to sign tokens. At least 32 bytes                            |
| `STORAGE_TYPE`                   | yes             | `local` or `cloud`                                                       |
| `STORAGE_LOCATION`               | local storage   | Directory where uploaded files are written                               |
| `STORAGE_BASEURL`                | no              | Only for data uploaded before storage keys existed (rows with full URLs) |
| `CLOUD_BUCKET`                   | cloud storage   | Google Cloud Storage bucket name                                         |
| `CLOUD_BASEURL`                  | no              | Same as `STORAGE_BASEURL`, for cloud storage                             |
| `GOOGLE_APPLICATION_CREDENTIALS` | cloud storage   | Path to the service-account JSON key                                     |
| `PORT`                           | no              | HTTP port (default `8080`)                                               |
| `GUEST_ENABLED`                  | no              | Allow `POST /api/v1/auth/guest` (default `true`)                            |
| `GUEST_CANPOST`                  | no              | Allow guests to upload content (default `false`)                         |
| `SERVER_TOMCAT_REMOTEIP_INTERNAL_PROXIES` | no    | Regex of reverse-proxy IPs allowed to set `X-Forwarded-For`              |
| `CORS_ALLOWED_ORIGINS`           | no              | Browser origins allowed to call the API, comma-separated (default `*`). Empty turns CORS off |
| `MAX_PAGE_SIZE`                  | no              | Largest `size` for the paginated lists (default `100`); bigger values return `400` |

**Other defaults**

- Uploads are limited to **1 MB per request** (`spring.servlet.multipart.max-*` in `application.properties`). Raise it if your content is larger.
- Security and SQL debug logging are **off** by default. To turn them on while developing, set `LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG` and/or `SPRING_JPA_SHOW_SQL=true`.
- **Running behind a reverse proxy** (nginx, Render, etc): the client IP used for rate limits and download records comes from `X-Forwarded-For`, but only when the request arrives from a trusted proxy. If your proxy connects from a public IP, set `SERVER_TOMCAT_REMOTEIP_INTERNAL_PROXIES`.
- The schema is managed by Hibernate (`ddl-auto=update`), so tables are created on first start.

## Creating the first admin

Roles are created automatically, but no admin account is. To get your first admin you have to do it from the database:

1. Register a normal account with `POST /api/v1/auth/register`.
2. Give it the `ADMIN` role directly in the database (set the user's role to the `ADMIN` row in the roles table).
3. Log in again to get a token with the new role. 

From then on, that admin can create accounts with any role (`POST /api/v1/admin/users`) and change roles (`PATCH /api/v1/admin/users/{id}/role/{roleName}`). 
**Note:** Existing tokens keep their old role until they expire.

## API overview

All endpoints except `/api/v1/auth/**` and the Swagger docs require an `Authorization: Bearer <token>` header.

### Auth

| Method | Endpoint              | Description                                                   |
|--------|-----------------------|---------------------------------------------------------------|
| POST   | `/api/v1/auth/register`  | Create an account (`username`, `password`). Rate-limited.     |
| POST   | `/api/v1/auth/login`     | Log in (`username`, `credential`) and receive a JWT. Rate-limited. |
| POST   | `/api/v1/auth/guest`     | Get a guest token, if guests are enabled. Rate-limited.       |

### Content

| Method | Endpoint                       | Description                                                                 |
|--------|--------------------------------|-----------------------------------------------------------------------------|
| GET    | `/api/v1/content`                 | Paginated list. Query params: `prompt`, `tags`, `creatorId`, `page`, `size`, `sort` (e.g. `createdAt,desc`). |
| GET    | `/api/v1/content/{id}`            | Get one content item                                                        |
| GET    | `/api/v1/content/download/{id}`   | Download the file and record the download                                   |
| POST   | `/api/v1/content`                 | Upload (multipart): `content` (JSON: name, description, version, tags), `file`, optional `thumbnail` |
| DELETE | `/api/v1/content/{id}`            | Soft delete; add `?hardDelete=true` to remove the record and its files      |

### Users

| Method | Endpoint               | Description                                                       |
|--------|------------------------|-------------------------------------------------------------------|
| GET    | `/api/v1/users`           | Paginated list. Query params: `prompt`, `page`, `size`, `sort`.   |
| GET    | `/api/v1/users/{id}`      | Get one user                                                      |
| PATCH  | `/api/v1/users`           | Update the current user's profile                                 |
| PATCH  | `/api/v1/users/password`  | Change the current user's password (`currentPassword`, `newPassword`) |

### Ratings & stats

| Method | Endpoint                          | Description                                                        |
|--------|-----------------------------------|--------------------------------------------------------------------|
| POST   | `/api/v1/rating/{contentId}`         | Rate content. JSON body: `rating` (1-5), optional `comment`.       |
| GET    | `/api/v1/rating/{contentId}`         | Paginated ratings, newest first; filter with `rating`, `sort` by `createdAt`, `updatedAt` or `rating`. |
| GET    | `/api/v1/rating/{contentId}/distribution` | Count of ratings per value                                    |
| GET    | `/api/v1/stats/{contentId}`          | Downloads (total and last 7 days), rating count and average        |

### Admin (`ADMIN` role only)

| Method | Endpoint                                   | Description                                       |
|--------|--------------------------------------------|---------------------------------------------------|
| GET    | `/api/v1/admin/{id}`                          | Get any user                                      |
| POST   | `/api/v1/admin/users`                         | Create an account with a chosen role (default `USER`) |
| PATCH  | `/api/v1/admin/users/{id}/role/{roleName}`    | Change a user's role (`ADMIN`, `USER`, `GUEST`)   |

Full request and response examples are in the [documentation](https://paulowgdev.github.io/OpenUGC/).

## Project structure

```
src/main/java/com/diversestudio/openugc/
├── controllers/   REST endpoints
├── service/       Business logic (auth, content, ratings, stats, rate limiting)
├── repository/    Spring Data JPA repositories
├── entities/      JPA entities (User, Content, Rating, Download, Tag, Role)
├── dto/           Request/response records and query projections
├── security/      JWT filter and helpers
├── storage/       Local and Google Cloud Storage implementations
├── config/        Security, startup role seeding, guest settings
└── util/          Database-specific native queries
docs/              Docusaurus documentation site 
```

## Documentation

The full guide lives at **[paulowgdev.github.io/OpenUGC](https://paulowgdev.github.io/OpenUGC/)**. Its source is in [`/docs`](./docs), run it locally with:

The documentation is still a work in progress, and feedback welcome :)

## Contributing

Issues and pull requests are welcome!

1. Fork the repo and create a branch: `git checkout -b feature/my-change`
2. Make your changes
3. Open a pr describing what and why

## License

OpenUGC is released under the [Apache License 2.0](./LICENSE).
