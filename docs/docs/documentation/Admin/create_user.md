---
sidebar_position: 2
---

# Create User (Admin)

Creates an account. Unlike [Register](../Authorization/register.md), the admin can choose the role, for example to create another admin account.

## Endpoint

- **URL:** `POST http://<your-domain>/api/v1/admin/users`
- **Headers:**
  - `Authorization: Bearer <token>` (the token must have the `ADMIN` role)
  - `Content-Type: application/json`
- **Body:**

```json
{
  "username": "new_admin",
  "password": "a-long-and-safe-password",
  "role": "ADMIN"
}
```

| Field | Rules |
|-------|-------|
| `username` | Required, 3 to 32 characters. Must not be taken |
| `password` | Required, 8 to 72 characters. Can also be sent as `credential` |
| `role` | Optional. `ADMIN`, `USER` or `GUEST` (upper or lower case). Defaults to `USER` |

## Response Example

`201 Created` with the new user:

```json
{
  "userId": 21,
  "username": "new_admin",
  "joinedAt": "2026-11-16T15:30:00.000+00:00",
  "role": "ADMIN",
  "downloadsPerformed": 0,
  "downloadReceived": 0,
  "averageRatingsReceive": 0.0
}
```

The new user can log in right away with [Login](../Authorization/login.md).

## Response Status

* `201 Created` – Account created
* `400 Bad Request` – A field breaks the rules above (listed under `errors`), or the role doesn't exist
* `403 Forbidden` – Invalid or missing token, or the token doesn't have the `ADMIN` role
* `409 Conflict` – The username is already taken
