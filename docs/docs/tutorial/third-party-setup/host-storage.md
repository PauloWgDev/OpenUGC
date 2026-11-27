---
sidebar_position: 2
---

# Host with Google Cloud Storage

Google Cloud Storage (GCS) can be used as the storage for OpenUGC's uploaded files and thumbnails.
In this guide, we'll create a bucket, generate credentials, and prepare them for the API.

:::info Why not store the files on Render?
Free Render web services have a temporary disk: anything saved there is lost every time the service restarts, redeploys or goes to sleep. If you host the API on Render, store the files in a bucket like this one.
:::

The files are never served straight from the bucket. Players always download them through the API (`/api/v1/content/download/{id}` and `/api/v1/content/thumbnail/{id}`), so the bucket can stay **private**.

---

### 1. Create a Google Cloud Account

Go to [Google Cloud Console](https://console.cloud.google.com/) and sign up (or log in if you already have an account).
You may need to enable billing, but Google offers a **free trial with credits**.

---

### 2. Create a Project

From the Google Cloud dashboard:

1. Click the project dropdown (top left).
2. Select **New Project**.
3. Give it a name (e.g. `openugc-project`) and choose a billing account.
4. Click **Create**.

---

### 3. Enable the Cloud Storage API

1. In the Google Cloud Console, go to **APIs & Services → Library**.
2. Search for **Cloud Storage**.
3. Click **Enable**.

---

### 4. Create a Bucket

1. Navigate to **Cloud Storage → Buckets → Create**.
2. Fill in the fields:
   - **Name**: must be globally unique (e.g. `openugc-bucket`)
   - **Location**: choose a region close to where the API runs
   - **Storage Class**: Standard (the default is fine)
   - **Access Control**: Uniform
   - **Public access prevention**: keep it on. The bucket doesn't need to be public.
3. Click **Create**.

Your bucket is now ready.

---

### 5. Generate Service Account Credentials

OpenUGC needs a service account to read, write and delete files in your bucket.

1. Go to **IAM & Admin → Service Accounts**.
2. Click **Create Service Account**.
3. Give it a name (e.g. `openugc-service`).
4. Assign the role **Storage Object Admin**. It's enough for OpenUGC, and it gives fewer permissions than **Storage Admin**.
5. After creating it, open the service account and go to **Keys → Add Key → Create New Key**.
6. Select **JSON** and download the file.

This JSON file contains your credentials. Keep it safe and never commit it to a repository.

---

### 6. Connect to OpenUGC

OpenUGC needs three settings to use the bucket:

| Variable | Value |
|----------|-------|
| `STORAGE_TYPE` | `cloud` |
| `CLOUD_BUCKET` | the name of your bucket, e.g. `openugc-bucket` |
| `GOOGLE_APPLICATION_CREDENTIALS` | the path to the JSON key file on the machine where the API runs |

Follow the instructions in the [Host API](./host-api.md) section to set them up on Render, including how to upload the JSON file.
