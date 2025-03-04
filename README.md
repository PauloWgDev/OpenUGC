# API for Unnoficial Unity's User Generated Content (U3GC).

This REST API acts as the connection between the the postgreSQL database and Unity.


# Authorization

## Register

Registering a user will create a new entry in the "users" table of the databse.
It will return "User registered successfully!" if successfull and "403 Forbidden" if the there was a problem registering, for example the username inserted already exist.

#### Endpoint for Registration:
##### Endpoint
POST http://<direction>/api/auth/register
##### Header
**Key**: Content-Type
**Value**: application/json
##### body
 {"username": "random_username", "credential": "password_here"}
