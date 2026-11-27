---
sidebar_position: 2
---

# Get User by ID

Returns a single user by their ID.

## Endpoint

- **URL:** `GET http://<your-domain>/api/v1/users/{id}`
- **Headers:**
    - `Authorization: Bearer <token>`

## Response Example

```json
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
}
```

The fields are the same as in [Get Users Page](./get_user_batch.md#user-fields). `profilePicture` and `aboutMe` are left out if the user didn't set them.

To get the content uploaded by this user, use `GET /api/v1/content?creatorId={id}`.

## Response Status

* `200 OK` – Returns the user
* `401 Unauthorized` – Missing, invalid or expired token
* `404 Not Found` – No user with this ID
