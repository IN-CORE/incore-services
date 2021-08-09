# Space Service

To start service: **gradle task space-service:jettyRun**

After starting the service, you should be able to create spaces.

## Space

### Create Space

POST http://localhost:8080/space/api/spaces

Content-Type: multipart/form-data

###### Form Parameter: "space" should hold the JSON.

{
"metadata": {
"name": "my space"
},
"privileges": {
"userPrivileges": {
"user": "ADMIN",
"another-user": "READ"
},
"groupPrivileges": {
"group-users": "ADMIN"
} },
"members": []
}

### Grant privileges to space

POST http://localhost:8080/space/api/spaces/{id}/grant

Content-Type: multipart/form-data

###### Form Parameter: "grant" should hold the JSON.

{
"userPrivileges": {
"foo": "ADMIN"
},
"groupPrivileges": {
"groups": "ADMIN"
} }

### Modify space

By modifying a space you can either modify its metadata and add members, or provide a list of members that you want to
remove from the space.

PUT http://localhost:8080/space/api/spaces/{id}

Content-Type: multipart/form-data

###### Form Parameter: "space" should hold the JSON.

{
"metadata": {
"name": "my space"
},
"members": [
"member 1",
"...",
"member n"
]
}

##### For removing a list of members from the space:

###### Form Parameter: "remove" should hold the JSON.

{
"members": ["id 1", "id 2", "...", "id n"]
}

### Get all spaces that a member is part of

GET http://localhost:8080/space/api/spaces?member={member_id}

### Remove a member from a space

DELETE http://localhost:8080/space/api/spaces/{space_id}/members/{member_id}