# Update User

Updates specific fields of the currently authenticated user. Only non-null fields will be updated.

## Endpoint

- **URL**:
  `PATCH http://<your-domain>/api/users`
- **Headers:**
    - `Authorization: Bearer <token>`
- **Request Body**
  - Only include the fields you want to update

### Request Body
```json
{
    "username": "newUsername",
    "profilePicture": "https://example.com/image.png",
    "aboutMe": "Hello! This is my new bio"
}
```
(The user is identified from the JWT token, not from the request body)

## Response Example

```json
"User Patched Successfully"
```

## Responses Status

* `200 OK` – User updated
* `400 Bad Request` – `username` is blank or not 3 to 32 characters, `aboutMe` is over 2,000 characters, or `profilePicture` is over 2,048 characters. The body lists each invalid field under `errors`
* `409 Conflict` – The new username is already taken
* `403 Forbidden` – Invalid or missing token
* `404 Not Found`- User not found
* `500 Internal Server Error` – Server issue

---