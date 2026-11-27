---
sidebar_position: 3
---

# Get Content Thumbnail

Returns the thumbnail image of a content item.

This endpoint is public: no token is needed, so the URL can be used directly as an image source in a browser or game client. Fetching a thumbnail is **not** counted as a download.

## Endpoint

- **URL:** `GET http://<your-domain>/api/v1/content/thumbnail/{id}`
- **Auth:** not needed (public)

The `thumbnail` field returned by `GET /api/v1/content` and `GET /api/v1/content/{id}` already holds this URL, so you don't need to build it yourself.

## Response

The image itself, shown inline:

* `Content-Type`: `image/png`, `image/jpeg` or `image/webp`
* `Cache-Control: public, max-age=86400`. Stored files never change, so clients and proxies can cache the image for a day.

## Response Status

* `200 OK` – Returns the image
* `404 Not Found` – No content with this ID, the content has no thumbnail, or the image file is missing from the storage
