---
sidebar_position: 1
---

# Get User (Admin)

Returns any user by their ID. It returns the same information as [Get User by ID](../User/get_user_single.md), but only admins can call it.

## Endpoint

- **URL:** `GET http://<your-domain>/api/v1/admin/{id}`
- **Headers:**
  - `Authorization: Bearer <token>` (the token must have the `ADMIN` role)

## Response Example

```json
{
  "userId": 13,
  "username": "user778293",
  "joinedAt": "2026-04-17T18:13:50.867+00:00",
  "role": "USER",
  "downloadsPerformed": 20,
  "downloadReceived": 5,
  "averageRatingsReceive": 4.1
}
```

The fields are explained in [Get Users Page](../User/get_user_batch.md#user-fields).

## Response Status

* `200 OK` – Returns the user
* `403 Forbidden` – Invalid or missing token, or the token doesn't have the `ADMIN` role
* `404 Not Found` – No user with this ID
