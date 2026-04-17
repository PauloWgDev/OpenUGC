# Retrieve Specific Content

Fetches a specific content item by its ID.

## Endpoint retrieve all contents

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

## Responses Status

* `200 OK` – Returns content list
* `403 Forbidden` – Invalid or missing token
* `404 Not Found`- Content not found
* `500 Internal Server Error` – Database issue

---