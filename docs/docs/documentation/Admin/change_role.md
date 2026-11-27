---
sidebar_position: 3
---

# Change User Role (Admin)

Changes the role of a user.

## Endpoint

- **URL:** `PATCH http://<your-domain>/api/v1/admin/users/{id}/role/{roleName}`
  - `{id}` is the user ID.
  - `{roleName}` is the new role: `ADMIN`, `USER` or `GUEST`. **It must be in uppercase**; `admin` returns `400 Bad Request`.
- **Headers:**
  - `Authorization: Bearer <token>` (the token must have the `ADMIN` role)
- **Body:** none

Example, to make user 13 an admin:

```bash
curl -X PATCH "http://<your-domain>/api/v1/admin/users/13/role/ADMIN" \
  -H "Authorization: Bearer <token>"
```

## Response Example

The updated user:

```json
{
  "userId": 13,
  "username": "user778293",
  "joinedAt": "2026-04-17T18:13:50.867+00:00",
  "role": "ADMIN",
  "downloadsPerformed": 20,
  "downloadReceived": 5,
  "averageRatingsReceive": 4.1
}
```

## When the New Role Applies

* **Uploading and deleting content** use the role saved in the database, so they follow the new role right away.
* **Admin endpoints** (`/api/v1/admin/...`) use the role saved in the token. The user keeps their old role there until they log in again or their token expires (up to 24 hours). For example, a user who was just demoted from `ADMIN` can still call the admin endpoints with their current token.

:::caution
Nothing stops you from removing the `ADMIN` role from the last admin. If that happens, you'll have to give it back from the database, as explained in [Creating the First Admin](../../tutorial/first-admin.md).
:::

## Response Status

* `200 OK` – Role changed
* `400 Bad Request` – The role doesn't exist (remember it's uppercase)
* `403 Forbidden` – Invalid or missing token, or the token doesn't have the `ADMIN` role
* `404 Not Found` – No user with this ID
