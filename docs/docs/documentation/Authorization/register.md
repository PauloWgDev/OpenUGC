# Register

Registers a new `users` in the users table.

## Endpoint

* **URL:** `POST http://<your-domain>/api/auth/register`
* **Headers:**
    * `Content-Type: application/json`
* **Body:**
  ```
  {
    "username": "random_username",
    "password": "password_here"
  }
  ```

* **Rules:**
    * `username`: required, 3 to 32 characters
    * `password` (or `credential`): required, 8 to 72 characters

## Response Status

* `200 OK` – User registered successfully
* `400 Bad Request` – A field breaks the rules above. The body lists each invalid field under `errors`, e.g. `{"errors": {"username": "size must be between 3 and 32"}}`
* `409 Conflict` – Username already exists
* `500 Internal Server Error` – Database issue
