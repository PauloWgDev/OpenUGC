# API for Unofficial Unity's User Generated Content (U3GC)

This REST API serves as an intermediary between a PostgreSQL database and Unity, enabling an easy integration of user-generated content (UGC) into your Unity projects.

## Why did we develop this API?

Unity's official UGC has been on Beta for years already and it lacks some crucial functionalities that any UGC system should have.
As a response we decided to create our own UGC system.

## Requirements

- Set up a postgreSQL database with tables for `user`, `content`, `rating`, `download`, `content_dates`.
- Configure your `<span>application.properties</span>` file with the database connection details:

  ```
  spring.application.name=unity-api

  # Database configuration

  # Database Configuration
  spring.datasource.url=jdbc:postgresql://localhost:5433/U3GC
  spring.datasource.username=your_user
  spring.datasource.password=your_password
  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

  # Server Port
  server.port=8080

  # Flyway Migration Configuration
  spring.flyway.baseline-on-migrate=true
  spring.flyway.baseline-version=1


  # Authentication key
  jwt.secret=aSecureRandomGeneratedKeyThatIsAtLeast32BytesLong!
  ```
- Ensure your database schema includes the necessary tables. Consider using Flyway for migrations.

---

## Authorization

### Register

Registers a new user in the users table.

#### Endpoint

* **URL:** `<span>POST http://<your-domain>/api/auth/register</span>`
* **Headers:**
  * `<span>Content-Type: application/json</span>`
* **Body:**
  ```
  {
    "username": "random_username",
    "password": "password_here"
  }
  ```

#### Responses

* `200 OK` – User registered successfully
* `403 Forbidden` – Username already exists
* `500 Internal Server Error` – Database issue

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

---

## Contents

### Retrieve Content

It will return a json with the following format

```js
[
    {
        "contentId": 1,
        "creatorId": 1,
        "creatorName": "PotatoHead",
        "data": "some data",
        "name": "Impossible Level",
        "description": "Only for pro players",
        "version": 1,
        "createdAt": "2025-02-25T19:02:10.914+00:00",
        "updatedAt": "2025-02-25T19:02:10.914+00:00"
    },
    {
        "contentId": 6,
        "creatorId": 2,
        "creatorName": "TomatoHead",
        "data": "some data",
        "name": "Very Epic Content",
        "description": "This is a very Epic content",
        "version": 1,
        "createdAt": "2025-02-27T04:41:01.025+00:00",
        "updatedAt": "2025-02-27T04:41:01.025+00:00"
    },
]
```

#### Endpoint retrieve all contents:

- URL:
  `GET http://<your-domain>/api/content`
- **Headers:**
  - **Key:** `Authorization`
  - **Value:** `Bearer eyJhbGciOiJIUzM4NCJ..."`

### Upload Content

Creates a new Content entry in the database, the content has to be associated with an existing user.

A successfull Post will return something like:

```json
{
    "content_id": 10,
    "data": "This is some data",
    "creator": {
        "user_id": 4,
        "username": "player5",
        "credential": "12345"
    },
    "name": "New Content",
    "description": "This is just a test",
    "version": 1,
    "contentDates": {
        "content_id": 10,
        "created_at": "2025-03-10T05:22:40.377+00:00",
        "updated_at": "2025-03-10T05:22:40.377+00:00"
    }
}
```

An unsuccessfull post will return `403 Forbidden`. It can be unsuccessfull for two reasons:

- Incorrect Authorization
- The user id used does not exist

#### Endpoint uploading content:

- URL:
  `POST http://<your-domain>/content?userId={id}`
- **Headers:**

  - **Key:** `Authorization`
  - **Value:** `Bearer eyJhbGciOiJIUzM4NCJ..."`
-
- **Body:**

  ```json
  {
             "data": "This is some data",
             "name": "New Content",
             "description": "This is just a test",
             "version": 1
  }
  ```
