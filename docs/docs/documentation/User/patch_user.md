---
sidebar_position: 3
---

# Update User

Updates the profile of the user who sends the request. The user is identified by the token, so players can only change their own profile.

Only the fields you send are changed. Fields that are missing (or `null`) keep their current value.

## Endpoint

- **URL:** `PATCH http://<your-domain>/api/v1/users`
- **Headers:**
    - `Authorization: Bearer <token>`
    - `Content-Type: application/json`
- **Body:** only include the fields you want to update.

```json
{
  "username": "newUsername",
  "profilePicture": "https://example.com/image.png",
  "aboutMe": "Hello! This is my new bio"
}
```

| Field | Rules |
|-------|-------|
| `username` | 3 to 32 characters, can't be only spaces. Spaces at the start and end are removed. Must not be taken by another user |
| `profilePicture` | A URL or any text that identifies the picture. Up to 255 characters |
| `aboutMe` | Up to 2,000 characters |

Other fields (like `role` or `userId`) are ignored. To change the password, use [Change Password](./change_password.md).

:::note
`profilePicture` is only saved as text. OpenUGC doesn't upload or check the image, so your game decides what to put there (for example a URL to an image hosted somewhere else).
:::

## Response Example

`200 OK` with the updated user, so the game can refresh the profile without another request:

```json
{
  "userId": 13,
  "username": "player1",
  "joinedAt": "2026-11-27T14:05:12.345+00:00",
  "role": "USER",
  "profilePicture": "https://example.com/avatar.png",
  "aboutMe": "I make racing tracks",
  "downloadsPerformed": 20,
  "downloadReceived": 5,
  "averageRatingsReceive": 4.1
}
```

The fields are the same as in [Get Users Page](./get_user_batch.md#user-fields).

## Response Status

* `200 OK` – User updated. The body is the updated user
* `400 Bad Request` – `username` is blank or not 3 to 32 characters, or `aboutMe` is over 2,000 characters. The body lists each invalid field under `errors`
* `401 Unauthorized` – Missing, invalid or expired token
* `404 Not Found` – The user of the token no longer exists
* `409 Conflict` – The new username is already taken, or `profilePicture` is longer than 255 characters
