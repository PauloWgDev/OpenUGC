# API for Unofficial Unity's User Generated Content (U3GC)

This REST API serves as an intermediary between a PostgreSQL database and Unity, enabling an easy integration of user-generated content (UGC) into your Unity projects.

## Why did we develop this API?

Unity's official UGC has been on Beta for years already and it lacks some crucial functionalities that any UGC system should have.
As a response we decided to create our own UGC system.

## Requirements

- Set up a postgreSQL database with tables for `user`, `content`, `rating`, `download`, `content_dates`.
- Configure your `application.properties` file with the database connection details:

```
spring.application.name=unity-api


# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5433/U3GC
spring.datasource.username=postgres
spring.datasource.password=F11215125
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Server Port
server.port=8080

# Flyway Migration Configuration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1


# Authentication key
jwt.secret=aSecureRandomGeneratedKeyThatIsAtLeast32BytesLong!

# Define Profile for DB
spring.profiles.active=postgres

# Debug (optional)
logging.level.org.springframework.security=DEBUG
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

Authenticates a user and returns a token.

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

#### Sucessfull Rensponse Example:

```json
{
"token": "eyJhbGciOiJIUzM4NCJ9..."
}
```

#### Response

* `200 OK` – Returns JWT token
* `403 Forbidden` – Invalid username or password

---

## Contents Management

### Retrieve All Content

Retrieves all user-generated content with support for advanced search, pagination, and sorting.

#### Endpoint retrieve all contents

- URL:
  `GET http://<your-domain>/api/content`
- **Headers:**
  - `Authorization: Bearer <token>`
- **Parameters:**
  - `prompt` (optional, default: ""): Filter content by a search prompt.
  - `page` (optional, default: 0): Page number for pagination.
  - `size` (optional, default: 10): Number of items per page.
  - `sort` (optional, default: "createdAt,desc"): Sort order (e.g., `avgRating,desc` to sort by average rating in descending order).

#### Response Example

```js
{
  "content": [
    {
      "contentId": 1,
      "creatorId": 1,
      "creatorName": "Alice",
      "data": "some data",
      "name": "My Content",
      "description": "This is a test content",
      "version": 1,
      "downloadsCount": 4,
      "latestDownloadsCount": 0,
      "avgRating": 4.333333333333333,
      "createdAt": "2025-02-25T19:02:10.914+00:00",
      "updatedAt": "2025-02-25T19:02:10.914+00:00"
    },
    {
      "contentId": 2,
      "creatorId": 1,
      "creatorName": "Alice",
      "data": "Example content data",
      "name": "Sample Content",
      "description": "This is a sample description",
      "version": 1,
      "downloadsCount": 5,
      "latestDownloadsCount": 0,
      "avgRating": 3.5,
      "createdAt": "2025-03-10T00:50:57.150+00:00",
      "updatedAt": "2025-03-10T00:50:57.150+00:00"
    }
    // ... additional content objects
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "empty": false,
      "sorted": true,
      "unsorted": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "last": true,
  "totalElements": 7,
  "totalPages": 1,
  "size": 10,
  "number": 0,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "first": true,
  "numberOfElements": 7,
  "empty": false
}
```

#### Responses

* `200 OK` – Returns content list
* `403 Forbidden` – Invalid or missing token
* `500 Internal Server Error` – Database issue

### Retrieve Specific Content

Fetches a specific content item by its ID.

#### Endpoint retrieve all contents

- URL:
  `GET http://<your-domain>/api/content/{id}`
- **Headers:**
  - `Authorization: Bearer <token>`

```js
{
  "contentId": 1,
  "creatorId": 1,
  "creatorName": "Alice",
  "data": "some data",
  "name": "My Content",
  "description": "This is a test content",
  "version": 1,
  "downloadsCount": 4,
  "latestDownloadsCount": 0,
  "avgRating": 4.333333333333333,
  "createdAt": "2025-02-25T19:02:10.914+00:00",
  "updatedAt": "2025-02-25T19:02:10.914+00:00"
}
```

#### Responses

* `200 OK` – Returns content list
* `403 Forbidden` – Invalid or missing token
* `404 Not Found`- Content not found
* `500 Internal Server Error` – Database issue

### Upload Content

Uploads new user-generated content.

#### Endpoint

- URL:
  `POST http://<your-domain>/content`
- **Headers:**
  
  - **Key:** `Authorization`
  - **Value:** `Bearer eyJhbGciOiJIUzM4NCJ..."`
- 
- **Body:**
  
  ```json
  {
    "creatorId": 4,
    "name": "New Content",
    "description": "This is just a test",
    "version": 1,
    "data": "This is some data",
    "thumbnail": "base64encodedImage",
    "tags": ["tag1", "tag2"]
  }
  ```

**thumbnail and tags are optional, you don't have to include them in the body if your not going to upload any.**

#### Response

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

##### Responses:

* `201 Created` – Content successfully uploaded
* `403 Forbidden` – Invalid or missing token / User does not exist

---

## Summary of API Endpoints

| Controller     | Method | Endpoint              | Description                                      |
|----------------|--------|-----------------------|--------------------------------------------------|
| Authorization  | POST   | /api/auth/register    | Register a new user                              |
| Authorization  | POST   | /api/auth/login       | Authenticates a user and returns an authentication token if valid |
| Content         | GET    | /api/content          | Retrieves all content with search, pagination and sorting options |
| Content         | GET    | /api/content/{id}     | Retrieves a specific content item by its ID      |
| Content         | GET    | /api/download/{id}    | Handles the download request for a content item  |
| Content         | POST   | /api/content          | Uploads a content file and an optional thumbnail, then creates a content record |
| Content         | DELETE | /api/content/{id}     | Deletes a content record and its associated files |
| User            | GET    | /api/users            | Retrieves a paginated list of users, optionally filtered by a prompt and sorted by a specified field and direction |
| User            | GET    | /api/users/{id}       | Retrieves a specific user by their ID            |
| User            | POST   | /api/users            | Creates a new user account                       |
| Rating          | GET    | /api/rating/{id}      | Retrieves a paginated list of ratings for the specific account |
| Rating          | GET    | /api/rating/{id}/distribution | Returns a map where the keys represent rating values and the values indicate how many users gave each rating |
| Rating          | POST   | /api/rating/{id}      | Registers a user's rating for the specific content |

