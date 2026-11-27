---
sidebar_position: 4
---

# Download Content

Returns the content file and records the download.

## Endpoint

- **URL:** `GET http://<your-domain>/api/v1/content/download/{id}`
- **Headers:**
  - `Authorization: Bearer <token>`

The `data` field returned by `GET /api/v1/content` and `GET /api/v1/content/{id}` already holds this URL.

## Response

The file itself, as a download:

* `Content-Type: application/octet-stream`
* `Content-Disposition: attachment; filename="<content name>.<extension>"`

The suggested file name is the content's name plus the extension of the file that was uploaded. For example, a content named "Big Castle" uploaded as `castle.zip` downloads as `Big Castle.zip`. Characters that aren't allowed in file names are removed.

Example with cURL, saving the file with the suggested name:

```bash
curl -OJ "http://<your-domain>/api/v1/content/download/2" \
  -H "Authorization: Bearer <token>"
```

## How Downloads Are Counted

Every successful download is recorded with the user, the content version, the date and the client IP. The download counters (`downloadsCount` and `latestDownloadsCount`) count **each user once** per content: if the same user downloads it again, the count doesn't change, and only the date (and IP) of their last download is updated.

So `downloadsCount` is really "how many different users downloaded it", and `latestDownloadsCount` is "how many of them downloaded it in the last 7 days".

A download is only recorded if the file was found, so failed downloads don't count. Thumbnails are never counted.

## Response Status

* `200 OK` – Returns the file
* `403 Forbidden` – Invalid or missing token
* `404 Not Found` – No content with this ID, or the file is missing from the storage
