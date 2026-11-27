---
sidebar_position: 1
---

# Get Contents Page

Returns a page of content, with search, filters, pagination and sorting.

Content that was [soft deleted](./delete_content.md) doesn't appear in this list.

## Endpoint

- **URL:** `GET http://<your-domain>/api/v1/content`
- **Headers:**
  - `Authorization: Bearer <token>`
- **Query Parameters (all optional):**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `prompt` | `""` | Text to search for in the content **name**. It matches any part of the name and ignores upper and lower case: `castle` finds "Big Castle" and "castle_v2" |
| `tags` | none | Only return content that has **at least one** of these tags. Ignores upper and lower case. Send several as `tags=racing&tags=night` or `tags=racing,night` |
| `creatorId` | none | Only return content uploaded by this user ID |
| `page` | `0` | Page number, starting at 0 |
| `size` | `10` | Number of items per page, from `1` to `100` (see [Pagination](../api-basics.md#pagination)) |
| `sort` | `createdAt,desc` | Sort order, as `field,direction`. See the allowed fields below |

You can combine the filters. For example, the newest maps from user 5 with "castle" in the name:

```
GET /api/v1/content?prompt=castle&tags=maps&creatorId=5&sort=createdAt,desc
```

### Sort fields

| Field | Sorts by |
|-------|----------|
| `createdAt` | Upload date (default, newest first) |
| `updatedAt` | Last update date |
| `name` | Name, alphabetically |
| `version` | Version number |
| `avgRating` | Average rating. Content without ratings always goes last |
| `downloadsCount` | Total downloads |
| `latestDownloadsCount` | Downloads in the last 7 days. Useful for a "trending" list |

Any other field returns `400 Bad Request`. See [Sorting](../api-basics.md#sorting) for the format.

## Response Example

```json
{
  "content": [
    {
      "contentId": 2,
      "creatorId": 1,
      "creatorName": "Alice",
      "data": "http://<your-domain>/api/v1/content/download/2",
      "thumbnail": "http://<your-domain>/api/v1/content/thumbnail/2",
      "name": "Big Castle",
      "description": "A castle map for 4 players",
      "tags": ["castle", "maps"],
      "version": 1,
      "downloadsCount": 5,
      "latestDownloadsCount": 2,
      "avgRating": 4.5,
      "createdAt": "2026-03-10T00:50:57.150+00:00",
      "updatedAt": "2026-03-10T00:50:57.150+00:00"
    },
    {
      "contentId": 1,
      "creatorId": 1,
      "creatorName": "Alice",
      "data": "http://<your-domain>/api/v1/content/download/1",
      "thumbnail": null,
      "name": "Desert Track",
      "description": "My first track",
      "tags": [],
      "version": 1,
      "downloadsCount": 0,
      "latestDownloadsCount": 0,
      "avgRating": null,
      "createdAt": "2026-02-25T19:02:10.914+00:00",
      "updatedAt": "2026-02-25T19:02:10.914+00:00"
    }
  ],
  "page": {
    "size": 10,
    "number": 0,
    "totalElements": 2,
    "totalPages": 1
  }
}
```

The `page` fields are explained in [Pagination](../api-basics.md#pagination).

### Content fields

| Field | Description |
|-------|-------------|
| `contentId` | ID of the content |
| `creatorId`, `creatorName` | ID and username of the user who uploaded it |
| `data` | URL to [download the file](./download_content.md). Needs a token, and each call counts as a download |
| `thumbnail` | URL of the [thumbnail image](./get_content_thumbnail.md), public. `null` if the content has no thumbnail |
| `name`, `description` | Name and description given on upload |
| `tags` | Tags of the content, lowercase and in alphabetical order. Empty list if it has none |
| `version` | Version number given on upload |
| `downloadsCount` | Number of different users who downloaded it. The same user downloading it twice counts once. See [How Downloads Are Counted](./download_content.md#how-downloads-are-counted) |
| `latestDownloadsCount` | How many of those users downloaded it in the last 7 days |
| `avgRating` | Average rating from 1 to 5. `null` if nobody rated it yet |
| `createdAt`, `updatedAt` | Upload date. Since content can't be edited yet, both are the same |

## Response Status

* `200 OK` – Returns a page of content. If nothing matches, `content` is an empty list
* `400 Bad Request` – Invalid `sort` field, or a parameter with the wrong type (e.g. `creatorId=abc`)
* `403 Forbidden` – Invalid or missing token
