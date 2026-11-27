---
sidebar_position: 2
---

# Get Content by ID

Returns a single content item by its ID.

## Endpoint

- **URL:** `GET http://<your-domain>/api/v1/content/{id}`
- **Headers:**
    - `Authorization: Bearer <token>`

## Response Example

```json
{
  "contentId": 1,
  "creatorId": 1,
  "creatorName": "Pepe",
  "data": "http://<your-domain>/api/v1/content/download/1",
  "thumbnail": "http://<your-domain>/api/v1/content/thumbnail/1",
  "name": "My Content",
  "description": "This is a test content",
  "tags": ["maps", "racing"],
  "version": 1,
  "downloadsCount": 4,
  "latestDownloadsCount": 0,
  "avgRating": 4.333333333333333,
  "createdAt": "2026-02-25T19:02:10.914+00:00",
  "updatedAt": "2026-02-25T19:02:10.914+00:00"
}
```

The fields are the same as in [Get Contents Page](./get_content_batch.md#content-fields).

`data` and `thumbnail` are API URLs, not direct links to the storage:

* `data` → `GET /api/v1/content/download/{id}`: needs the `Authorization` header and is counted as a download. See [Download Content](./download_content.md).
* `thumbnail` → `GET /api/v1/content/thumbnail/{id}`: public, so it can be used directly as an image source. `null` if the content has no thumbnail.

:::note
Content that was [soft deleted](./delete_content.md) is hidden from the list, but it can still be fetched by ID with this endpoint (and downloaded, rated and so on). Only a hard delete removes it completely.
:::

## Response Status

* `200 OK` – Returns the content
* `401 Unauthorized` – Missing, invalid or expired token
* `404 Not Found` – No content with this ID
