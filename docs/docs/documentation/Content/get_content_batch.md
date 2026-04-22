# Get Contents Page

Returns a page of contents with support for advanced search, pagination options, and sorting.

## Endpoint

- URL:
  `GET http://<your-domain>/api/content`
- **Headers:**
  - `Authorization: Bearer <token>`
- **Parameters:**
  - `prompt` (optional, default: ""): Filter content by a search prompt.
  - `page` (optional, default: 0): Page number for pagination.
  - `size` (optional, default: 10): Number of items per page.
  - `sort` (optional, default: "createdAt,desc"): Sort order (e.g., `avgRating,desc` to sort by average rating in descending order).

## Response Example

```json
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
    // ... more content objects
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

## Responses Status

* `200 OK` – Returns page of contents 
* `403 Forbidden` – Invalid or missing token
* `500 Internal Server Error` – Server issue