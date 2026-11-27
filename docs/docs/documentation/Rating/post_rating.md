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
- **Query Parameters:**

| Parameter | Required | Description |
|-----------|----------|-------------|
| `rating` | yes | A whole number from `1` to `5` |
| `comment` | no | A comment about the content |

:::note
The rating and the comment are sent as **query parameters** in the URL, not as a JSON body. Remember to URL-encode the comment (spaces become `%20`, and so on). Most HTTP clients do it for you if you build the URL with their query helpers.
:::

Example:

```bash
curl -X POST "http://<your-domain>/api/v1/rating/10?rating=5&comment=Great%20map" \
  -H "Authorization: Bearer <token>"
```

When a user rates again without a `comment`, their previous comment is removed.

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
* `400 Bad Request` – `rating` is missing, isn't a whole number, or isn't between 1 and 5
* `401 Unauthorized` – Missing, invalid or expired token
* `404 Not Found` – No content with this ID
