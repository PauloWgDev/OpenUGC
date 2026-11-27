---
sidebar_position: 1
---

# Get Content Stats

Returns the download and rating numbers of a content item.

## Endpoint

- **URL:** `GET http://<your-domain>/api/v1/stats/{id}`
  - `{id}` is the **content** ID.
- **Headers:**
  - `Authorization: Bearer <token>`

## Response Example

```json
{
  "downloadsCount": 25,
  "latestDownloadsCount": 4,
  "ratingCount": 12,
  "avgRating": 4.25
}
```

| Field | Description |
|-------|-------------|
| `downloadsCount` | Number of different users who downloaded it. See [How Downloads Are Counted](../Content/download_content.md#how-downloads-are-counted) |
| `latestDownloadsCount` | How many of those users downloaded it in the last 7 days |
| `ratingCount` | Number of ratings it received |
| `avgRating` | Average rating, from 1 to 5. `null` if it has no ratings |

`downloadsCount`, `latestDownloadsCount` and `avgRating` are also included in every content item returned by the [content endpoints](../Content/get_content_single.md). This endpoint is handy when you only need the numbers, or also need `ratingCount`.

## Response Status

* `200 OK` – Returns the stats
* `403 Forbidden` – Invalid or missing token
* `404 Not Found` – No content with this ID
