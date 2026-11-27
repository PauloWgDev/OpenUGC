---
sidebar_position: 4
---

# Change Password

Changes the password of the user who sends the request. The current password is required, so someone who only has the token can't take over the account.

## Endpoint

- **URL:** `PATCH http://<your-domain>/api/v1/users/password`
- **Headers:**
    - `Authorization: Bearer <token>`
    - `Content-Type: application/json`
- **Body:**

```json
{
  "currentPassword": "my-old-password",
  "newPassword": "my-new-password"
}
```

| Field | Rules |
|-------|-------|
| `currentPassword` | Required. Must be the user's current password |
| `newPassword` | Required. 8 to 72 characters |

## Response Example

`204 No Content`, with an empty body.

Tokens that were issued before the change keep working until they expire (up to 24 hours). The next login must use the new password.

## Response Status

* `204 No Content` – Password changed
* `400 Bad Request` – `currentPassword` is wrong, or a field breaks the rules above
* `401 Unauthorized` – Missing, invalid or expired token
* `404 Not Found` – The user of the token no longer exists
