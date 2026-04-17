# Login

Authenticates a user and returns a JSON Web Token (JWT).

## Endpoint for Login:

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

## Sucessfull Rensponse Example:

```json
{
"token": "eyJhbGciOiJIUzM4NCJ9..."
}
```

### Response Status

* `200 OK` – Returns JWT token
* `403 Forbidden` – Invalid username or password

---