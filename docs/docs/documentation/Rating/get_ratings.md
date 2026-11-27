---
sidebar_position: 2
---

# Get Ratings of a Content

Returns a page of the ratings that a content item received.

## Endpoint

- **URL:** `GET http://<your-domain>/api/v1/rating/{id}`
  - `{id}` is the **content** ID.
- **Headers:**
  - `Authorization: Bearer <token>`
- **Query Parameters (all optional):**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `rating` | `-1` | Only return ratings with this value (`1` to `5`). Any other value, like the default `-1`, returns all ratings |
| `sort` | `createdAt,desc` | Order of the ratings, as `field,direction` (see below) |
| `page` | `0` | Page number, starting at 0 |
| `size` | `10` | Number of ratings per page |

For example, to show only the 5-star reviews:

```
GET /api/v1/rating/10?rating=5
```

### Sorting

By default the newest ratings come first. The `sort` parameter accepts these fields, with `asc` or `desc` (see [Sorting](../api-basics.md#sorting)):

| Field | Sorts by |
|-------|----------|
| `createdAt` | When the user first rated the content |
| `updatedAt` | When the user last changed their rating or comment |
| `rating` | The rating value |

For example, the lowest ratings first:

```
GET /api/v1/rating/10?sort=rating,asc
```

Ratings with the same value come in the same order on every request, so pages never repeat or skip a rating.

## Response Example

```json
{
  "content": [
    {
      "userId": 4,
      "contentId": 10,
      "rating": 5.0,
      "comment": "Great map",
      "createdAt": "2026-03-12T18:20:05.512+00:00",
      "updatedAt": "2026-03-14T09:02:41.003+00:00"
    },
    {
      "userId": 7,
      "contentId": 10,
      "rating": 3.0,
      "comment": null,
      "createdAt": "2026-03-11T22:47:13.880+00:00",
      "updatedAt": "2026-03-11T22:47:13.880+00:00"
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

`createdAt` is when the user first rated the content, and `updatedAt` is when they last changed it. They're the same until the user rates again. Ratings saved by a server older than 0.0.1 have `null` dates, and come last when sorting by a date.

Each rating has the `userId` of the user who wrote it, not the username. To show the username, get it with [Get User by ID](../User/get_user_single.md).

The page fields are explained in [Pagination](../api-basics.md#pagination).

## Response Status

* `200 OK` – Returns a page of ratings. If the content has no ratings, or doesn't exist, `content` is an empty list
* `400 Bad Request` – A parameter has the wrong type (e.g. `rating=abc`), or `sort` uses a field that isn't in the list above
* `401 Unauthorized` – Missing, invalid or expired token
