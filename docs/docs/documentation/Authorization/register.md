---
sidebar_position: 2
---

# Register

Creates a new account with the `USER` role.

Registering doesn't log the player in. Call [Login](./login.md) afterwards to get a token.

## Endpoint

* **URL:** `POST http://<your-domain>/api/v1/auth/register`
* **Auth:** not needed (public)
* **Headers:**
    * `Content-Type: application/json`
* **Body:**

  ```json
  {
    "username": "random_username",
    "password": "password_here"
  }
  ```

* **Rules:**
    * `username`: required, 3 to 32 characters. Spaces at the start and end are removed.
    * `password`: required, 8 to 72 characters. You can also send it as `credential` (the same name the login uses).

Any other field in the body (like `role` or `userId`) is ignored, so players can't choose their own role.

## Response Example

`201 Created` with the new user:

```json
{
  "userId": 13,
  "username": "player1",
  "joinedAt": "2026-11-27T14:05:12.345+00:00",
  "role": "USER",
  "downloadsPerformed": 0,
  "downloadReceived": 0,
  "averageRatingsReceive": 0.0
}
```

The fields are the same as in [Get Users Page](../User/get_user_batch.md#user-fields). Registering doesn't log the player in: call [Login](./login.md) next to get a token.

## Response Status

* `201 Created` – User registered. The body is the new user
* `400 Bad Request` – A field breaks the rules above. The body lists each invalid field under `errors`, e.g. `{"errors": {"username": "size must be between 3 and 32"}}`
* `409 Conflict` – The username is already taken
* `429 Too Many Requests` – More than 5 register requests in a minute from the same IP. The `Retry-After` header says how many seconds to wait. See [Rate Limits](../api-basics.md#rate-limits)
