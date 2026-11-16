---
sidebar_position: 4
---

# Creating the First Admin

OpenUGC creates the `ADMIN`, `USER` and `GUEST` roles the first time it starts, but it doesn't create any admin account. The first admin has to be set directly in the database. After that, admins can manage roles from the API.

---

## 1. Register a Normal Account

Register the account that will become admin, like any other user:

```bash
curl -X POST http://<your-domain>/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "a-long-and-safe-password"}'
```

---

## 2. Give It the ADMIN Role in the Database

Connect to the database (with pgAdmin, `psql`, or the console of your provider) and run:

```sql
UPDATE users
SET role_id = (SELECT role_id FROM roles WHERE role_name = 'ADMIN')
WHERE username = 'admin';
```

Change `'admin'` to the username you registered. The query should report `UPDATE 1`.

---

## 3. Log In Again

The role is saved inside the token when you log in, so a token you got before the change still says `USER`. Log in again to get a token with the `ADMIN` role:

```bash
curl -X POST http://<your-domain>/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "credential": "a-long-and-safe-password"}'
```

---

## What Admins Can Do

With an admin token you can:

- Create accounts with any role: [Create User](../documentation/Admin/create_user.md).
- Change the role of any user: [Change User Role](../documentation/Admin/change_role.md).
- Soft delete or hard delete any content, not only their own: [Delete Content](../documentation/Content/delete_content.md).

**Note**:
When an admin changes someone's role, the user's existing tokens still carry the old role for the admin endpoints until they expire (up to 24 hours). For example, a demoted admin can keep calling `/api/admin/...` until their token expires.

