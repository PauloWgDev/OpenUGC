---
sidebar_position: 1
---

# Rate Content

Saves the rating of the current user for a content item, from 1 to 5, with an optional comment.

Each user has **one rating per content**. If the user rates the same content again, the new rating replaces the old one (comment included). The rating keeps its `createdAt` date and gets a new `updatedAt`, so it doesn't move to the top of the [ratings list](./get_ratings.md).

## Endpoint

- **URL:** `POST http://<your-domain>/api/v1/rating/{id}`
  - `{id}` is the **content** ID.
- **Headers:**
  - `Authorization: Bearer <token>`
  - `Content-Type: application/json`
- **Body:**

```json
{
  "rating": 5,
  "comment": "Great map"
}
```

| Field | Required | Description |
|-------|----------|-------------|
| `rating` | yes | A whole number from `1` to `5` |
| `comment` | no | A comment about the content, up to 2,000 characters |

Example:

```bash
curl -X POST "http://<your-domain>/api/v1/rating/10" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"rating": 5, "comment": "Great map"}'
```

When a user rates again without a `comment` (or with an empty one), their previous comment is removed and `comment` is `null`.

## Response Example

The saved rating:

```json
{
  "userId": 4,
  "contentId": 10,
  "rating": 5.0,
  "comment": "Great map",
  "createdAt": "2026-03-12T18:20:05.512+00:00",
  "updatedAt": "2026-03-12T18:20:05.512+00:00"
}
```

## Response Status

* `200 OK` – Rating saved
* `400 Bad Request` – `rating` is missing, isn't a whole number, or isn't between 1 and 5, or `comment` is over 2,000 characters. The body lists each invalid field under `errors`
* `403 Forbidden` – Invalid or missing token
* `404 Not Found` – No content with this ID
