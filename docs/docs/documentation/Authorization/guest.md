---
sidebar_position: 4
---

# Guest Login

Gives a token with the `GUEST` role, without registering. It's useful to let players browse and download content before (or without) creating an account.

This endpoint only works if guests are enabled on the server (`GUEST_ENABLED`, `true` by default). See [Configuration](../../tutorial/configuration.md#optional-settings).

## Endpoint

- **URL:** `POST http://<your-domain>/api/v1/auth/guest`
- **Auth:** not needed (public)
- **Body:** none

## Successful Response Example

```json
{
  "token": "eyJhbGciOi..."
}
```

Use it like any other token: `Authorization: Bearer <token>`. It is valid for **24 hours**.

## What Guests Can Do

By default, guests can:

* List, search and download content, and see thumbnails, ratings and stats.
* Rate content.
* See users and update their own profile.

Guests **can't upload content** unless the server sets `GUEST_CANPOST=true`. When they try, they get `403 Forbidden`.

## Things to Know

* **Every call creates a new guest account**, with a random username like `guest_1a2b3c4d`. Save the token on the device and reuse it until it expires, instead of calling this endpoint every time the game starts.
* A guest account has no password that the player knows, so once its token expires, there's no way to log back into that same account. The next guest login creates a new one.
* Guest accounts show up in [Get Users](../User/get_user_batch.md) with the role `GUEST`.

## Response Status

* `200 OK` – Returns the guest token
* `403 Forbidden` – Guests are disabled on this server (`"detail": "Guest users are disabled."`)
* `429 Too Many Requests` – More than 5 guest login requests in a minute from the same IP. The `Retry-After` header says how many seconds to wait. See [Rate Limits](../api-basics.md#rate-limits)
