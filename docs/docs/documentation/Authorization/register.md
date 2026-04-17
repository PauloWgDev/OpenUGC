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

## Response Status

* `200 OK` – User registered successfully
* `403 Forbidden` – Username already exists
* `500 Internal Server Error` – Database issue
