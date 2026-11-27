---
sidebar_position: 3
---

# Login

Checks a username and password and returns a JSON Web Token (JWT).

## Endpoint

- **URL:** `POST http://<your-domain>/api/v1/auth/login`
- **Auth:** not needed (public)
- **Headers:**
    - `Content-Type: application/json`
- **Body:**

  ```json
  {
    "username": "random_username",
    "credential": "password_here"
  }
  ```

:::note
The login body uses `credential` for the password, not `password`. Sending `password` here returns `400 Bad Request`.
:::

## Successful Response Example

```json
{
  "token": "eyJhbGciOi..."
}
```

Use this token in the `Authorization: Bearer <token>` header of the next requests. It is valid for **24 hours**. After that, the player has to log in again.

The token contains the user's ID, username and role. The role is saved when the token is created, so if the role changes, the player must log in again to get a token with the new role.

## Response Status

* `200 OK` – Returns the JWT token
* `400 Bad Request` – `username` or `credential` is missing or empty
* `401 Unauthorized` – Wrong username or password. The message is the same in both cases, so it doesn't reveal which usernames exist
* `429 Too Many Requests` – More than 5 login requests in a minute from the same IP. The `Retry-After` header says how many seconds to wait. See [Rate Limits](../api-basics.md#rate-limits)
