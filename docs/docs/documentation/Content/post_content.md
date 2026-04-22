# Upload Content

Uploads new user-generated content.

## Endpoint

- URL:
  `POST http://<your-domain>/content`
- **Headers:**

    - **Key:** `Authorization`
    - **Value:** `Bearer eyJhbGciOiJIUzM4NCJ..."`
-
- **Body:**

  ```json
  {
    "name": "New Content",
    "description": "This is just a test",
    "version": 1,
    "data": "This is some data",
    "thumbnail": "base64encodedImage",
    "tags": ["tag1", "tag2"]
  }
  ```

**thumbnail and tags are optional, you don't have to include them in the body if you're not going to upload any.**

## Response

```json
{
    "content_id": 10,
    "data": "This is some data",
    "creator": {
        "user_id": 4,
        "username": "player5",
        "credential": "12345"
    },
    "name": "New Content",
    "description": "This is just a test",
    "version": 1,
    "contentDates": {
        "content_id": 10,
        "created_at": "2025-03-10T05:22:40.377+00:00",
        "updated_at": "2025-03-10T05:22:40.377+00:00"
    }
}
```

## Response Status:

* `201 Created` – Content successfully uploaded
* `403 Forbidden` – Invalid or missing token / User does not exist