---
sidebar_position: 1
---

# Rate Content

Saves the rating of the current user for a content item, from 1 to 5, with an optional comment.

Each user has **one rating per content**. If the user rates the same content again, the new rating replaces the old one (comment included).

## Endpoint

- **URL:** `POST http://<your-domain>/api/rating/{id}`
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
curl -X POST "http://<your-domain>/api/rating/10?rating=5&comment=Great%20map" \
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
  "comment": "Great map"
}
```

## Response Status

* `200 OK` – Rating saved
* `400 Bad Request` – `rating` is missing, isn't a whole number, or isn't between 1 and 5
* `403 Forbidden` – Invalid or missing token
* `404 Not Found` – No content with this ID
