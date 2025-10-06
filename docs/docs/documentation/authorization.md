# Authorization

The **Authorization Controller** provides endpoints for user registration and login.
These endpoints allow clients to securely create accounts and obtain a JWT token for accessing protected routes.

## Register

Registers a new `users` in the users table.

### Endpoint

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

### Responsess

* `200 OK` – User registered successfully
* `403 Forbidden` – Username already exists
* `500 Internal Server Error` – Database issue

## Login

Authenticates a user and returns a JSON Web Token (JWT).

### Endpoint for Login:

- **URL:**
  `POST http://<your-domain>/api/auth/login`
- **Headers:**
  
  - **Key:** `Content-Type`
  - **Value:** `application/json`
- **Body:**
  
  ```json
  {
    "username": "random_username",
    "credential": "password_here"
  }
  ```

### Sucessfull Rensponse Example:

```json
{
"token": "eyJhbGciOiJIUzM4NCJ9..."
}
```

### Response

* `200 OK` – Returns JWT token
* `403 Forbidden` – Invalid username or password

---