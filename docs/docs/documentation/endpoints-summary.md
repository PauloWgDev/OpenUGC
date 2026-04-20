---
sidebar_position: 1
---

# Endpoints Summary

This page provides a quick overview of all available endpoints in **OpenUGC API**.  
For detailed request/response examples, see the dedicated sections for each controller.

---

## Authorization

| Method | Endpoint             | Description                                   |
|--------|----------------------|-----------------------------------------------|
| POST   | `/api/auth/register` | Register a new user                           |
| POST   | `/api/auth/login`    | Authenticate and receive a JWT token          |

---

## Content

| Method | Endpoint              | Description                                        |
|--------|-----------------------|----------------------------------------------------|
| GET    | `/api/content`        | Get all content (with pagination, search, sorting) |
| GET    | `/api/content/{id}`   | Get a specific content item by ID                  |
| POST   | `/api/content`        | Upload new content (optionally with thumbnail/tags)|
| DELETE | `/api/content/{id}`   | Delete a content record and its files              |
| GET    | `/api/download/{id}`  | Download a specific content item                   |

---

## Users

| Method | Endpoint             | Description                                            |
|--------|----------------------|--------------------------------------------------------|
| GET    | `/api/users`         | Get all users (with pagination, search, sorting)       |
| GET    | `/api/users/{id}`    | Get a specific user by ID                              |
| POST   | `/api/users`         | Create a new user account (admin or system use only)   |

---

## Ratings

| Method | Endpoint                                | Description                                                      |
|--------|-----------------------------------------|------------------------------------------------------------------|
| GET    | `/api/rating/{id}`                      | Get a paginated list of ratings for a content item               |
| GET    | `/api/rating/{id}/distribution`         | Get rating distribution for a content item (e.g., 5★ = 10 users) |
| POST   | `/api/rating/{id}`                      | Submit a rating for a content item                               |

---

Use the **Authorization token** (`Bearer <JWT>`) for all protected routes (Content, Users, Ratings).
