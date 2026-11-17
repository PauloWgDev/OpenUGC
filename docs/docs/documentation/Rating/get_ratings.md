---
sidebar_position: 2
---

# Get Ratings of a Content

Returns a page of the ratings that a content item received.

## Endpoint

- **URL:** `GET http://<your-domain>/api/rating/{id}`
  - `{id}` is the **content** ID.
- **Headers:**
  - `Authorization: Bearer <token>`
- **Query Parameters (all optional):**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `rating` | `-1` | Only return ratings with this value (`1` to `5`). Any other value, like the default `-1`, returns all ratings |
| `page` | `0` | Page number, starting at 0 |
| `size` | `10` | Number of ratings per page |

For example, to show only the 5-star reviews:

```
GET /api/rating/10?rating=5
```

:::note
This endpoint also accepts a `sort` parameter, but it has no effect yet: ratings come in no particular order. Ratings don't have a date either.
:::

## Response Example

```json
{
  "content": [
    {
      "userId": 4,
      "contentId": 10,
      "rating": 5.0,
      "comment": "Great map"
    },
    {
      "userId": 7,
      "contentId": 10,
      "rating": 3.0,
      "comment": null
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "number": 0,
  "size": 10,
  "first": true,
  "last": true,
  ...
}
```

Each rating has the `userId` of the user who wrote it, not the username. To show the username, get it with [Get User by ID](../User/get_user_single.md).

The page fields are explained in [Pagination](../api-basics.md#pagination).

## Response Status

* `200 OK` – Returns a page of ratings. If the content has no ratings, or doesn't exist, `content` is an empty list
* `400 Bad Request` – A parameter has the wrong type (e.g. `rating=abc`)
* `403 Forbidden` – Invalid or missing token
