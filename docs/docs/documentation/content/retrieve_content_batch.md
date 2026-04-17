# Contents Management

The **Content Controller** provides endpoints for managing user-generated content (UGC).
It allows clients to retrieve, search, filter, and upload content while supporting authentication through JWT tokens.
These endpoints form the core of the UGC system, enabling users to share, access, and manage their creations securely and efficiently.

## Retrieve All Content

Retrieves all user-generated content with support for advanced search, pagination, and sorting.

### Endpoint retrieve all contents

- URL:
  `GET http://<your-domain>/api/content`
- **Headers:**
  - `Authorization: Bearer <token>`
- **Parameters:**
  - `prompt` (optional, default: ""): Filter content by a search prompt.
  - `page` (optional, default: 0): Page number for pagination.
  - `size` (optional, default: 10): Number of items per page.
  - `sort` (optional, default: "createdAt,desc"): Sort order (e.g., `avgRating,desc` to sort by average rating in descending order).

### Response Example

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

### Responses

* `200 OK` – Returns content list
* `403 Forbidden` – Invalid or missing token
* `500 Internal Server Error` – Database issue

## Retrieve Specific Content

Fetches a specific content item by its ID.

### Endpoint retrieve all contents

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

### Responses

* `200 OK` – Returns content list
* `403 Forbidden` – Invalid or missing token
* `404 Not Found`- Content not found
* `500 Internal Server Error` – Database issue

---
