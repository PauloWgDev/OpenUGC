# Get Content Thumbnail

Returns the thumbnail image of a content item.

This endpoint is public: no token is needed, so the URL can be used directly as an image source in a browser or game client. Fetching a thumbnail is **not** counted as a download.

## Endpoint

- URL:
  `GET http://<your-domain>/api/content/thumbnail/{id}`

The `thumbnail` field returned by `GET /api/content` and `GET /api/content/{id}` already holds this URL.

## Response

The image itself, shown inline:

* `Content-Type`: `image/png`, `image/jpeg` or `image/webp`
* `Cache-Control: public, max-age=86400`. Stored files never change, so clients and proxies can cache the image for a day.

## Responses Status

* `200 OK` – Returns the image
* `404 Not Found` – Content not found, or the content has no thumbnail
* `500 Internal Server Error` – Server issue
