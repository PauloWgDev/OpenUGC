---
sidebar_position: 1
---

# Authorization

The **Authorization** endpoints let players create an account and get a **JWT token**, which is needed for almost every other endpoint.

There are two ways to get a token:

- **With an account:** [Register](./register.md) once, then [Log in](./login.md) to get a token.
- **Without an account:** [Guest Login](./guest.md) gives a guest token right away, if guests are enabled on the server.

These three endpoints are public (no token needed), and each one is limited to 5 requests per minute per IP.

Tokens last **24 hours**. Send them in the `Authorization` header:

```
Authorization: Bearer <token>
```

More details about tokens, roles and errors are on [API Basics](../api-basics.md).
