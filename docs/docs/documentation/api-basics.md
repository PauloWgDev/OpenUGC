---
sidebar_position: 2
---

# API Basics

This page explains the things that work the same way on every endpoint: tokens, roles, pagination, sorting, rate limits and error responses.

---

## Base URL

All endpoints start with `/api`, for example:

```
http://<your-domain>/api/content
```

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
- A missing, invalid or expired token gets `403 Forbidden`.

### Public endpoints

These don't need a token:

| Endpoint | Why |
|----------|-----|
| `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/guest` | You use them to get a token |
| `GET /api/content/thumbnail/{id}` | So thumbnails can be used directly as an image URL |
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

The endpoints that return lists (`GET /api/content`, `GET /api/users`, `GET /api/rating/{id}`) return one page at a time. Use these query parameters:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Page number. **The first page is `0`** |
| `size` | `10` | Number of items per page |

Example: `GET /api/content?page=2&size=20` returns items 41 to 60.

`page` must be `0` or more and `size` must be `1` or more. Keep `size` reasonable (up to 50 or 100), because big pages are slow.

The response is a page object. The items are in `content`, and these fields tell you where you are:

```json
{
  "content": [ ... ],
  "number": 0,
  "size": 10,
  "numberOfElements": 10,
  "totalElements": 42,
  "totalPages": 5,
  "first": true,
  "last": false,
  "empty": false,
  "pageable": { ... },
  "sort": { ... }
}
```

| Field | Meaning |
|-------|---------|
| `content` | The items on this page |
| `number` | Current page number (starting at 0) |
| `size` | Requested page size |
| `numberOfElements` | How many items this page actually has |
| `totalElements` | Total number of items, across all pages |
| `totalPages` | Total number of pages |
| `first` / `last` | Whether this is the first or the last page |
| `empty` | Whether this page has no items |

---

## Sorting

The content and user lists accept a `sort` parameter in the format `field,direction`:

```
GET /api/content?sort=avgRating,desc
GET /api/users?sort=username,asc
```

- `direction` is `asc` (lowest first) or `desc` (highest first). If you leave it out, it's `asc`.
- Only some fields can be used. They're listed on each endpoint's page. Any other field returns `400 Bad Request`, and the error message lists the allowed fields.

---

## Rate Limits

To stop spam and password guessing, these endpoints accept at most **5 requests per minute from the same IP**:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/guest`

Each endpoint has its own limit. The 5 requests come back gradually over the minute. When the limit is reached, the API returns `429 Too Many Requests` with a plain text message, for example:

```
Too many login attempts. Please try again later.
```

---

## Errors

When something goes wrong, the API returns an HTTP status code and, in most cases, a JSON body in the following format:

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

A few responses are plain text instead of JSON: the `429` rate limit messages and the `403` "Guest users are disabled." message.

### Common status codes

| Code | Meaning                                                                                                       |
|------|---------------------------------------------------------------------------------------------------------------|
| `200 OK` | Successful Request                                                                                            |
| `201 Created` | An account was created by an admin                                                                            |
| `204 No Content` | The request worked and there is nothing to return (e.g. delete)                                               |
| `400 Bad Request` | Something in the request is wrong: a missing or invalid field, an unknown sort field, a rating out of range... |
| `401 Unauthorized` | Wrong username or password on login                                                                           |
| `403 Forbidden` | Missing, invalid or expired token, or you're not allowed to do this (e.g. deleting someone else's content)    |
| `404 Not Found` | The content, user or file doesn't exist                                                                       |
| `409 Conflict` | The username is already taken, or the data conflicts with what's in the database                              |
| `413 Payload Too Large` | The upload is bigger than the limit (1 MB by default)                                                         |
| `429 Too Many Requests` | Rate limit reached                                                                                            |
| `500 Internal Server Error` | Unexpected error on the server. Check the API logs                                                            |

---

## Dates

Dates are returned in ISO 8601 format, in UTC:

```
"createdAt": "2026-11-16T15:30:00.000+00:00"
```
