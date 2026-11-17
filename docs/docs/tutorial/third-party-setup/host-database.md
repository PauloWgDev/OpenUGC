---
sidebar_position: 1
---

# Host Database with Render

[Render](https://render.com/) is a cloud service that lets you host APIs and databases with minimal setup.
It has a free tier, which makes it a good option for trying out OpenUGC with cloud services.

In this guide, we'll walk you through how to create and configure your **Database** on Render.

:::caution Free plan limits
A free Render Postgres database **expires 30 days after it is created**. After that you have 14 days to upgrade it to a paid plan before Render deletes it along with all its data. It also has 1 GB of storage and no backups. The free plan is fine to try OpenUGC, but use a paid plan (or another provider) for a real game.
:::

---

### 1. Create an Account

Go to [render.com](https://render.com/) and sign up for a free account.

![Render sign-up page](create_account.png)

---

### 2. Create a New Postgres Database

From the Render dashboard, click on **"New +" → "Postgres"**.

![Render "New" menu with the Postgres option](image.png)

---

### 3. Configure Postgres

Fill in the fields:

- **Name**: any name you like (e.g. `openugc-db`)
- **Region**: pick the one closest to your users. Use the **same region** for the API later, so they can talk over Render's private network.
- **PostgreSQL Version**: 17
- **Plan Options**: Free (to try it out) or a paid plan

You can always upgrade the plan later.

---

### 4. Get the Connection Details

Once the database is created, open it and look at the **Connections** section. Render gives you:

- **Hostname**
- **Port** (usually `5432`)
- **Database**
- **Username**
- **Password**
- **Internal Database URL** and **External Database URL**

OpenUGC needs a **JDBC URL**, which is **not** the same format as the URLs Render shows. Render's URLs look like this:

```
postgresql://openugc_user:password@dpg-xxxxx-a/openugc_db
```

For OpenUGC, take the hostname, port and database name and write them like this, with the username and password as separate values:

```
DATABASE_URL=jdbc:postgresql://dpg-xxxxx-a:5432/openugc_db
DATABASE_USERNAME=openugc_user
DATABASE_PASSWORD=password
```

If the API runs on Render in the same region, use the **internal** hostname (the short one, like `dpg-xxxxx-a`). If the API runs somewhere else, use the hostname from the **External Database URL** (the long one, ending in `render.com`).

You'll need these values when configuring the API.
Follow the steps in [Host with Google Cloud Storage](./host-storage.md) to set up the storage, and then [Host API](./host-api.md).
