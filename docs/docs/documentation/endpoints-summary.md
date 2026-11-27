---
sidebar_position: 1
---

# Endpoints Summary

This page lists all the endpoints of the **OpenUGC API**.
For request and response examples, open the page of each endpoint. For the things that work the same way everywhere (tokens, pagination, sorting, errors), see [API Basics](./api-basics.md).

All endpoints need an `Authorization: Bearer <token>` header, except the ones marked as **public**.

---

## Authorization

| Method | Endpoint | Description                                                                           |
|--------|----------|---------------------------------------------------------------------------------------|
| POST | `/api/auth/register` | [Register](./Authorization/register.md) a new account. **Public**, rate-limited       |
| POST | `/api/auth/login` | [Log in](./Authorization/login.md) and receive a JWT token. **Public**, rate-limited      |
| POST | `/api/auth/guest` | [Get a guest token](./Authorization/guest.md) without an account. **Public**, rate-limited |

---

## Content

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/content` | [Get a page of content](./Content/get_content_batch.md), with search, tag and creator filters, pagination and sorting |
| GET | `/api/content/{id}` | [Get one content item](./Content/get_content_single.md) by ID |
| GET | `/api/content/thumbnail/{id}` | [Get the thumbnail image](./Content/get_content_thumbnail.md). **Public**, not counted as a download |
| GET | `/api/content/download/{id}` | [Download the content file](./Content/download_content.md). Counted as a download |
| POST | `/api/content` | [Upload content](./Content/post_content.md): a file, an optional thumbnail and the metadata (multipart) |
| DELETE | `/api/content/{id}` | [Delete content](./Content/delete_content.md). Soft delete by default, `?hardDelete=true` to remove it completely |

---

## Users

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | [Get a page of users](./User/get_user_batch.md), with search, pagination and sorting |
| GET | `/api/users/{id}` | [Get one user](./User/get_user_single.md) by ID |
| PATCH | `/api/users` | [Update your profile](./User/patch_user.md): `username`, `aboutMe` or `profilePicture` |
| PATCH | `/api/users/password` | [Change your password](./User/change_password.md) |

---

## Ratings

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/rating/{id}` | [Rate a content item](./Rating/post_rating.md) from 1 to 5, with an optional comment |
| GET | `/api/rating/{id}` | [Get a page of ratings](./Rating/get_ratings.md) of a content item |
| GET | `/api/rating/{id}/distribution` | [Get how many ratings](./Rating/get_rating_distribution.md) of each value (1 to 5) a content item has |

In these endpoints, `{id}` is the **content** ID.

---

## Stats

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stats/{id}` | [Get the stats of a content item](./Stats/get_stats.md): downloads, downloads in the last 7 days, number of ratings and average rating |

---

## Admin

Requires a token with the `ADMIN` role. See [Creating the First Admin](../tutorial/first-admin.md).

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/{id}` | [Get any user](./Admin/get_user.md) by ID |
| POST | `/api/admin/users` | [Create an account](./Admin/create_user.md) with any role (`USER` by default) |
| PATCH | `/api/admin/users/{id}/role/{roleName}` | [Change a user's role](./Admin/change_role.md) |

---

## Other

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Health check. **Public**. Returns `"status":"UP"` when the API is running |
| GET | `/swagger-ui.html` | Interactive API docs in the browser. **Public** |
| GET | `/v3/api-docs` | OpenAPI description of the API in JSON (`/v3/api-docs.yaml` for YAML). **Public** |
