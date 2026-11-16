---
sidebar_position: 1
---

# Intro

OpenUGC is an open-source **REST API** for user-generated content (**UGC**). It sits between your **game or app**, a **PostgreSQL database** and a **file storage**, and gives you everything needed to let players upload, browse, rate and download content.

It isn't tied to any engine. Anything that can make HTTP requests can use it: Unity, Godot, Unreal, a web launcher, a mod manager, and so on.

## Why OpenUGC?

Unity's UGC service spent about two years in open beta without some of the basics a UGC system needs, and in 2025 Unity shut it down, leaving the games that relied on it without a solution.

OpenUGC was built as an open-source alternative. Because you host it yourself, you own the data, the storage and the rules.

## What it can do

- **Accounts:** register, log in and get a JWT token. Tokens last 24 hours.
- **Guest access:** players can get a guest token to browse without creating an account. You decide whether guests can upload.
- **Roles:** `ADMIN`, `USER` and `GUEST`. They are created automatically the first time the API starts.
- **Content:** upload a file with an optional thumbnail, a name, a description, a version and tags.
- **Search:** list content with pagination and sorting, and filter it by name, tags or creator.
- **Ratings:** players rate content from 1 to 5, with an optional comment.
- **Stats:** downloads (total and last 7 days), number of ratings and average rating for each content.
- **Storage:** files can be stored on the local disk or in Google Cloud Storage.
- **Delete:** content can be hidden from the list (soft delete) or removed completely (hard delete).
- **Rate limiting:** register, login and guest login are limited to 5 requests per minute per IP.
- **Swagger UI:** interactive API docs generated from the code.

## Getting Started

To run OpenUGC you need three things:

- **A place to run the API.** It is a Java 17 Spring Boot app, and the repository includes a `Dockerfile` and a Docker Compose example.
- **A PostgreSQL database.** It stores users, content information, ratings and downloads. The tables are created automatically on the first start.
- **A storage for the files.** Either a folder on the server's disk or a Google Cloud Storage bucket.

The [Hosting Tutorial](/docs/category/hosting-tutorial) shows two ways to set this up:

- **[Self-hosting](/docs/category/self-host---set-up):** run everything on your own machine or server. The quickest option is [Docker Compose](./tutorial/self-host-setup/docker-compose.md).
- **[Third-party services](/docs/category/third-parties---set-up):** host the API and the database on Render and the files on Google Cloud Storage.

Once it is running, check the [Configuration](./tutorial/configuration.md) page for all the settings, and [Creating the First Admin](./tutorial/first-admin.md) to get an admin account.

## Using the API

The [Documentation](/docs/category/documentation) section describes every endpoint. A good place to start is:

- [Endpoints Summary](./documentation/endpoints-summary.md): all endpoints on one page.
- [API Basics](./documentation/api-basics.md): tokens, pagination, sorting and error responses, which work the same way on every endpoint.

While the API is running, you can also open `http://<your-domain>/swagger-ui.html` to see and try every endpoint from the browser.
