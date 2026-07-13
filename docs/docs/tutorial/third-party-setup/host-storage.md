---
sidebar_position: 2
---

# Host with Google Cloud Storage

Google Cloud Storage (GCS) can be used as the storage backend for OpenUGC.  
In this guide, we’ll create a bucket, generate credentials, and prepare it for use in your API.

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

1. Navigate to **Storage → Buckets → Create Bucket**.  
2. Fill in the fields:  
   - **Name**: Must be globally unique (e.g. `openugc-bucket`)  
   - **Location**: Choose a region closest to your users  
   - **Storage Class**: Standard (default is fine)  
   - **Access Control**: Fine-grained or uniform (uniform is recommended)  
3. Click **Create**.  

Your bucket is now ready! 🎉

---

### 5. Generate Service Account Credentials

OpenUGC needs a service account to access your GCS bucket.

1. Go to **IAM & Admin → Service Accounts**.  
2. Click **Create Service Account**.  
3. Give it a name (e.g. `openugc-service`).  
4. Assign the role **Storage Admin**.  
5. After creating, go to **Keys → Add Key → Create New Key**.  
6. Select **JSON** and download the file.  

This JSON file contains your credentials — keep it safe!

---

### 6. Connect to OpenUGC

Once your bucket and credentials are ready, follow the instructions in the [Host API](./host-api.md) section to configure OpenUGC.
