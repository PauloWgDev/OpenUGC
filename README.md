# OpenUGC

![Logo](./docs/static/img/logo3.svg)

This REST API serves as an intermediary between a PostgreSQL database and **your game engine**, enabling an easy integration of user-generated content (UGC) into your projects.

## Why did we develop this API?

Unity's official UGC has been on Beta for years already and it lacks some crucial functionalities that any UGC system should have.
As a response we decided to create our own UGC system.

## Documentation

Documentation and a hosting guide is available here:

-> [OpenUGC Documentation & Hosting Guide](https://paulowgdev.github.io/OpenUGC/)

!! Note: The documentation is still under development. Your feedback and contributions are always welcome!

Note: the documentation is still a work in progress. Any constructive feedback is appreciated.

## Summary of API Endpoints

| Controller     | Method | Endpoint              | Description                                      |
|----------------|--------|-----------------------|--------------------------------------------------|
| Authorization  | POST   | /api/auth/register    | Register a new user                              |
| Authorization  | POST   | /api/auth/login       | Authenticates a user and returns an authentication token if valid |
| Content         | GET    | /api/content          | Retrieves all content with search, pagination and sorting options |
| Content         | GET    | /api/content/{id}     | Retrieves a specific content item by its ID      |
| Content         | GET    | /api/download/{id}    | Handles the download request for a content item  |
| Content         | POST   | /api/content          | Uploads a content file and an optional thumbnail, then creates a content record |
| Content         | DELETE | /api/content/{id}     | Deletes a content record and its associated files |
| User            | GET    | /api/users            | Retrieves a paginated list of users, optionally filtered by a prompt and sorted by a specified field and direction |
| User            | GET    | /api/users/{id}       | Retrieves a specific user by their ID            |
| User            | POST   | /api/users            | Creates a new user account                       |
| Rating          | GET    | /api/rating/{id}      | Retrieves a paginated list of ratings for the specific account |
| Rating          | GET    | /api/rating/{id}/distribution | Returns a map where the keys represent rating values and the values indicate how many users gave each rating |
| Rating          | POST   | /api/rating/{id}      | Registers a user's rating for the specific content |



