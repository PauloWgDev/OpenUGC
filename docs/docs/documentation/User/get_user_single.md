# Retrieve Specific User

Fetches a specific User by its ID.

## Endpoint

- URL:
  `GET http://<your-domain>/api/users/{id}`
- **Headers:**
    - `Authorization: Bearer <token>`

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

* `200 OK` – Returns User
* `403 Forbidden` – Invalid or missing token
* `404 Not Found`- User not found
* `500 Internal Server Error` – Server issue

---