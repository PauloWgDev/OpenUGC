---
sidebar_position: 6
---

# Delete Content

Deletes a content item. There are two kinds of delete:

* **Soft delete (default):** the content is hidden from the [content list](./get_content_batch.md), but nothing is removed. It can still be fetched by ID, downloaded, rated and checked in stats by anyone who has its ID or URL. Think of it as "unlisted".
* **Hard delete:** the file, the thumbnail and the content record are removed for good. After that, the content returns `404 Not Found` everywhere. This can't be undone.

Only the user who uploaded the content, or an `ADMIN`, can delete it.

## Endpoint

- **URL:** `DELETE http://<your-domain>/api/v1/content/{id}`
- **Headers:**
  - `Authorization: Bearer <token>`
- **Query Parameters:**
  - `hardDelete` (optional, default: `false`): `true` to remove the content completely.

Examples:

```bash
# Soft delete (hide it from the list)
curl -X DELETE "http://<your-domain>/api/v1/content/10" \
  -H "Authorization: Bearer <token>"

# Hard delete (remove it completely)
curl -X DELETE "http://<your-domain>/api/v1/content/10?hardDelete=true" \
  -H "Authorization: Bearer <token>"
```

## Response

`204 No Content`, with an empty body.

Soft deleting content that is already soft deleted works too, and just updates the delete date. There's no endpoint to restore soft-deleted content yet.

## Response Status

* `204 No Content` – Content deleted
* `403 Forbidden` – Invalid or missing token, or the content belongs to another user and you're not an `ADMIN`
* `404 Not Found` – No content with this ID
