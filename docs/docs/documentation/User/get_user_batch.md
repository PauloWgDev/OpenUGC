---
sidebar_position: 1
---

# Get Users Page

Returns a page of users, with search, pagination and sorting.

## Endpoint

- **URL:** `GET http://<your-domain>/api/users`
- **Headers:**
    - `Authorization: Bearer <token>`
- **Query Parameters (all optional):**

| Parameter | Default | Description |
|-----------|---------|-------------|
| `prompt` | `""` | Text to search for in the **username**. It matches any part of the name and ignores upper and lower case |
| `page` | `0` | Page number, starting at 0 |
| `size` | `10` | Number of users per page |
| `sort` | `joinedAt,desc` | Sort order, as `field,direction`. See the allowed fields below |

### Sort fields

| Field | Sorts by |
|-------|----------|
| `joinedAt` | Registration date (default, newest first) |
| `username` | Username, alphabetically |
| `downloadsPerformed` | How many downloads the user made |
| `downloadReceived` | How many downloads the user's content received |
| `averageRatingsReceive` | Average rating of the user's content |

Any other field returns `400 Bad Request`. For example, to get the best-rated creators:

```
GET /api/users?sort=averageRatingsReceive,desc
```

## Response Example

```json
{
  "content": [
    {
      "userId": 13,
      "username": "user778293",
      "joinedAt": "2026-04-17T18:13:50.867+00:00",
      "role": "USER",
      "profilePicture": "https://example.com/avatar.png",
      "aboutMe": "I make racing tracks",
      "downloadsPerformed": 20,
      "downloadReceived": 5,
      "averageRatingsReceive": 4.1
    },
    {
      "userId": 12,
      "username": "guest_1a2b3c4d",
      "joinedAt": "2026-04-16T10:02:11.120+00:00",
      "role": "GUEST",
      "downloadsPerformed": 3,
      "downloadReceived": 0,
      "averageRatingsReceive": 0.0
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

The page fields are explained in [Pagination](../api-basics.md#pagination).

### User fields

| Field | Description |
|-------|-------------|
| `userId` | ID of the user |
| `username` | Username |
| `joinedAt` | Date the account was created |
| `role` | `ADMIN`, `USER` or `GUEST` |
| `profilePicture` | Profile picture URL set by the user. **Left out** of the response if the user didn't set one |
| `aboutMe` | Bio set by the user. **Left out** of the response if the user didn't set one |
| `downloadsPerformed` | How many content items this user downloaded (each one counted once) |
| `downloadReceived` | How many downloads this user's content received |
| `averageRatingsReceive` | Average rating of all the ratings on this user's content. `0` if their content has no ratings |

Guest accounts (`guest_...`) are included in this list.

## Response Status

* `200 OK` – Returns a page of users
* `400 Bad Request` – Invalid `sort` field or parameter
* `403 Forbidden` – Invalid or missing token
