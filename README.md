# API for Unofficial Unity's User Generated Content (U3GC)

This REST API serves as an intermediary between a PostgreSQL database and Unity, enabling an easy integration of user-generated content (UGC) into your Unity projects.

---

## Authorization

### Register

Registering a user will create a new entry in the **users** table of the database.

- **Successful registration:** Returns `"User registered successfully!"`
- **Failure (e.g., username already exists):** Returns `"403 Forbidden"`

#### Endpoint for Registration:

- **Endpoint:**  
  `POST http://<your-domain>/api/auth/register`

- **Headers:**  
  - **Key:** `Content-Type`  
  - **Value:** `application/json`

- **Body:**
  ```json
  {
    "username": "random_username",
    "credential": "password_here"
  }


### Login

Login will search the username in the database and will make sure the credential is the correct one.

- **Successful login:** Returns 
```json
{
    "token": "eyJhbGciOiJIUzM4NCJ..."
}
```
- **Failure (e.g., username already exists):** Returns `"403 Forbidden"`

#### Endpoint for Login:

- **Endpoint:**  
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

---


