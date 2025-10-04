---
sidebar_position: 1
---

# Self-Hosting Database

If you prefer to manage your own database instead of using a cloud provider, you can self-host **PostgreSQL**.  
In this guide, we’ll install PostgreSQL locally, use **pgAdmin** to manage it, and create the database required for OpenUGC.

---

### 1. Install PostgreSQL and pgAdmin

Download and install PostgreSQL from the official website:  
👉 [https://www.postgresql.org/download/](https://www.postgresql.org/download/)

During installation, you’ll also have the option to install **pgAdmin**, a graphical tool to manage your PostgreSQL server.  
- Choose a password for the default `postgres` user (keep it safe — you’ll need it later).  
- Once finished, you should have both PostgreSQL and pgAdmin installed.

---

### 2. Open pgAdmin and Connect to Your Server

1. Launch **pgAdmin**.  
2. On the left sidebar, expand **Servers** → **PostgreSQL**.  
3. Enter the password you set for the `postgres` user during installation.  
4. You are now connected to your local PostgreSQL server.

---

### 3. Create a New Database

1. In pgAdmin, right-click **Databases** → **Create** → **Database**.  
2. Enter a name for your database (e.g. `openugc`).  
3. Leave the owner as `postgres` (or assign another user if you prefer).  
4. Click **Save**.  

Your database is now created and ready for use! 🎉

---

### 4. (Optional) Create a Dedicated User

For better security, you can create a separate user instead of using the default `postgres` superuser.

1. In pgAdmin, go to **Login/Group Roles** → Right-click → **Create → Login/Group Role**.  
2. Enter a username (e.g. `ugc_user`).  
3. Under **Definition**, set a password.  
4. Under **Privileges**, give the user `Can login` and `Create DB` if needed.  
5. Click **Save**.  
6. Assign this user to your database:  
   - Right-click your database → **Properties → Security → Privileges** → Add your user with `ALL` privileges.  

---

### 5. Collect Your Connection Information

You’ll need the following details when setting up OpenUGC:  

- **Host**: for testing just `localhost`  
- **Port**: default is `5432`  
- **Database name**: `openugc` (or your db name)
- **Username**: `postgres` (or your custom user)  
- **Password**: the one you set during installation

---

### 6. Link to OpenUGC

Now that your database is running locally, follow the instructions in the [Host API](./host-api.md) section to configure OpenUGC to use your new PostgreSQL database.
