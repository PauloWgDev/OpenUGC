---
sidebar_position: 2
---

# API Basics

This page explains the things that work the same way on every endpoint: tokens, roles, pagination, sorting, rate limits and error responses.

---

## Base URL

All endpoints start with `/api/v1`, for example:

```
http://<your-domain>/api/v1/content
```

The `v1` is the API version. Changes that would break existing clients (renamed fields, removed endpoints) go to a new version, like `/api/v2`, instead of changing `v1`. In your game, keep the base URL (`http://<your-domain>/api/v1`) in one setting, so moving to a new version is a config change.

The health check (`/actuator/health`) and the Swagger docs (`/swagger-ui.html`, `/v3/api-docs`) are not part of the versioned API, so they don't have the prefix.

Request and response bodies are JSON (`Content-Type: application/json`), except for the content upload, which is a multipart request (see [Upload Content](./Content/post_content.md)).

---

## Authentication

Most endpoints need a **JWT token**. You get one from:

- [Login](./Authorization/login.md), with a username and password.
- [Guest Login](./Authorization/guest.md), without an account (if guests are enabled).

Send it in the `Authorization` header of every request, with the word `Bearer` before it:

```
Authorization: Bearer eyJhbGciOi...
```

Things to know about tokens:

- A token is valid for **24 hours**. After that, log in again to get a new one. There is no refresh endpoint.
- There is no logout endpoint either. To "log out", the client just deletes the token.
- A missing, invalid or expired token gets `401 Unauthorized`. When your game gets a `401`, it should log in again (or get a new guest token) and retry.
- A valid token that isn't allowed to do something (for example a `USER` calling an admin endpoint) gets `403 Forbidden`. Logging in again won't help.

### Public endpoints

These don't need a token:

| Endpoint | Why |
|----------|-----|
| `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/guest` | You use them to get a token |
| `GET /api/v1/content/thumbnail/{id}` | So thumbnails can be used directly as an image URL |
| `GET /actuator/health` | Health check for Docker and hosting services |
| `/swagger-ui.html`, `/v3/api-docs` | Interactive API docs |

Everything else needs a token.

---

## Roles

Every account has one role:

| Role | Who | What they can do |
|------|-----|------------------|
| `GUEST` | Players who used [Guest Login](./Authorization/guest.md) | Browse, download and rate content, see users, edit their own profile. Upload only if the server allows it (`GUEST_CANPOST`) |
| `USER` | Players who registered | Everything a guest can do, plus upload content and delete their own content |
| `ADMIN` | Accounts promoted by an admin or in the database | Everything, plus delete any content and use the [admin endpoints](./endpoints-summary.md#admin) |

The role is stored inside the token when it's created. If an admin changes someone's role, the change reaches the admin endpoints only after that person logs in again (or their token expires).

---

## Pagination

The endpoints that return lists (`GET /api/v1/content`, `GET /api/v1/users`, `GET /api/v1/rating/{id}`) return one page at a time. Use these query parameters:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Page number. **The first page is `0`** |
| `size` | `10` | Number of items per page |

Example: `GET /api/v1/content?page=2&size=20` returns items 41 to 60.

`page` must be `0` or more and `size` must be `1` or more. Keep `size` reasonable (up to 50 or 100), because big pages are slow.

The response is a page object. The items are in `content`, and `page` tells you where you are:

```json
{
  "content": [ ... ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 42,
    "totalPages": 5
  }
}
```

| Field | Meaning |
|-------|---------|
| `content` | The items on this page |
| `page.size` | Requested page size |
| `page.number` | Current page number (starting at 0) |
| `page.totalElements` | Total number of items, across all pages |
| `page.totalPages` | Total number of pages |

To know if you're on the last page, check `page.number + 1 >= page.totalPages`. The number of items on this page is the length of `content`.

---

## Sorting

The content, user and rating lists accept a `sort` parameter in the format `field,direction`:

```
GET /api/v1/content?sort=avgRating,desc
GET /api/v1/users?sort=username,asc
GET /api/v1/rating/10?sort=createdAt,desc
```

- `direction` is `asc` (lowest first) or `desc` (highest first). If you leave it out, it's `asc`.
- Only some fields can be used. They're listed on each endpoint's page. Any other field returns `400 Bad Request`, and the error message lists the allowed fields.

---

## Rate Limits

To stop spam and password guessing, these endpoints accept at most **5 requests per minute from the same IP**:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/guest`

Each endpoint has its own limit. The 5 requests come back gradually over the minute. When the limit is reached, the API returns `429 Too Many Requests` with the usual [error body](#errors). The `Retry-After` header, and the `retryAfterSeconds` field, say how many seconds to wait before trying again:

```
HTTP/1.1 429 Too Many Requests
Retry-After: 12
Content-Type: application/problem+json

{
  "type": "about:blank",
  "title": "Too Many Requests",
  "status": 429,
  "detail": "Too many login attempts. Please try again later.",
  "instance": "/api/v1/auth/login",
  "retryAfterSeconds": 12,
  "timestamp": "2026-11-27T14:05:12.345Z"
}
```

---

## Errors

When something goes wrong, the API returns an HTTP status code and a JSON body (`Content-Type: application/problem+json`, the [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) format) like this one:

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Content not found for id: 99",
  "timestamp": "2026-11-16T15:30:00.000Z"
}
```

- `status` is the same as the HTTP status code.
- `detail` is a message that explains what happened. It's meant for developers and logs.

When the body of a request breaks a rule (for example a username that is too short), the response is `400 Bad Request` with an `errors` object that lists each invalid field:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed",
  "errors": {
    "username": "size must be between 3 and 32"
  },
  "timestamp": "2026-11-16T15:30:00.000Z"
}
```

Every error uses this format, so your game only needs one way to read errors: check the status code, and show or log `detail` if you need a message.

Successful responses are always JSON, except for file downloads and thumbnails (the file itself) and `204 No Content` (no body at all).

### Common status codes

| Code | Meaning                                                                                                       |
|------|---------------------------------------------------------------------------------------------------------------|
| `200 OK` | Successful Request                                                                                            |
| `201 Created` | An account was created (register, or an admin creating a user) |
| `204 No Content` | The request worked and there is nothing to return (delete, password change) |
| `400 Bad Request` | Something in the request is wrong: a missing or invalid field, an unknown sort field, a rating out of range... |
| `401 Unauthorized` | Missing, invalid or expired token: log in again. Also a wrong username or password on login |
| `403 Forbidden` | The token is valid but not allowed to do this (admin endpoints, deleting someone else's content, guests disabled...) |
| `404 Not Found` | The content, user or file doesn't exist                                                                       |
| `409 Conflict` | The username is already taken, or the data conflicts with what's in the database                              |
| `413 Payload Too Large` | The upload is bigger than the limit (1 MB by default)                                                         |
| `429 Too Many Requests` | Rate limit reached. Wait the seconds in `Retry-After` |
| `500 Internal Server Error` | Unexpected error on the server. Check the API logs                                                            |

---

## Dates

Dates are returned in ISO 8601 format, in UTC:

```
"createdAt": "2026-11-16T15:30:00.000+00:00"
```
