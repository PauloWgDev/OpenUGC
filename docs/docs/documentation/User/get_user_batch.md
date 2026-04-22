# Retrieve Specific User

Fetches a page of Users.

## Endpoint

- URL:
  `GET http://<your-domain>/api/users`
- **Headers:**
    - `Authorization: Bearer <token>`
- **Parameters:**
    - `prompt` (optional, default: ""): Filter content by a search prompt.
    - `page` (optional, default: 0): Page number for pagination.
    - `size` (optional, default: 10): Number of items per page.
    - `sort` (optional, default: "createdAt,desc"): Sort order (e.g., `avgRating,desc` to sort by average rating in descending order).

```json
{
    "userId": 13,
    "username": "user778293",
    "joinedAt": "2026-04-17T18:13:50.867+00:00",
    "role": "USER",
    "downloadsPerformed": 20,
    "downloadReceived": 5,
    "averageRatingsReceive": 4.1
}
```

## Responses Status

* `200 OK` – Returns Page of Users
* `403 Forbidden` – Invalid or missing token
* `500 Internal Server Error` – Server issue

---