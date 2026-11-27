---
sidebar_position: 2
---

# Register

Creates a new account with the `USER` role.

Registering doesn't log the player in. Call [Login](./login.md) afterwards to get a token.

## Endpoint

* **URL:** `POST http://<your-domain>/api/auth/register`
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

`200 OK` with a plain text message:

```
User registered successfully!
```

## Response Status

* `200 OK` – User registered successfully
* `400 Bad Request` – A field breaks the rules above. The body lists each invalid field under `errors`, e.g. `{"errors": {"username": "size must be between 3 and 32"}}`
* `409 Conflict` – The username is already taken
* `429 Too Many Requests` – More than 5 register requests in a minute from the same IP
