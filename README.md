# API for Unofficial Unity's User Generated Content (U3GC)

This REST API serves as an intermediary between a PostgreSQL database and Unity, enabling an easy integration of user-generated content (UGC) into your Unity projects.

## Why did we develop this API?

Unity's official UGC has been on Beta for years already and it lacks some crucial functionalities that any UGC system should have. 
As a response we decided to create our own UGC system.

## How to set up?

- Set up a postgreSQL database with tables for `user`, `content`, `rating`, `download`, `content_dates`. 
- Create an application.properties file where you define the domain of your postgre database.
- Run the API.

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


