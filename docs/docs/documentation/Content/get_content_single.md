# Retrieve Specific Content

Fetches a specific content item by its ID.

## Endpoint 

- URL:
  `GET http://<your-domain>/api/content/{id}`
- **Headers:**
    - `Authorization: Bearer <token>`

```json
{
  "contentId": 1,
  "creatorId": 1,
  "creatorName": "Pepe",
  "data": "http://<your-domain>/api/content/download/1",
  "thumbnail": "http://<your-domain>/api/content/thumbnail/1",
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

`data` and `thumbnail` are API URLs, not direct links to storage:

* `data` → `GET /api/content/download/{id}`: needs the `Authorization` header and is counted as a download.
* `thumbnail` → `GET /api/content/thumbnail/{id}`: public, so it can be used directly as an image source. `null` if the content has no thumbnail.

## Responses Status

* `200 OK` – Returns content
* `403 Forbidden` – Invalid or missing token
* `404 Not Found`- Content not found
* `500 Internal Server Error` – Server issue

---