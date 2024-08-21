# Project Service

To start service: **gradle task project-service:jettyRun**

After starting the service, you should be able to create projects. This API allows for managing projects in the IN-CORE
system, including creating, updating, and deleting projects, as well as managing associated datasets.


### Endpoints
#### **Get All Projects**

- **Method**: `GET`
- **Path**: `/project/api/projects`
- **Description**: Retrieve a list of all available projects, optionally filtered by parameters.
- **Query Parameters**:
    - `name`, `creator`, `owner`, `region`, `type`, `space`, `skip`, `limit`
- **Example**:
  ```http
  GET /project/api/projects?name={name}&creator={creator}&skip={skip}&limit={limit}
  ```

#### **Get Project by ID**

- **Method**: `GET`
- **Path**: `/project/api/projects/{projectId}`
- **Description**: Retrieve a specific project by its ID.
- **Path Parameter**:
    - `projectId` (string) - ID of the project to retrieve.
- **Example**:
  ```http
  GET /project/api/projects/66c5f70943e23b2bb24413ef
  ```

#### **Create a Project**

- **Method**: `POST`
- **Path**: `/project/api/projects`
- **Description**: Create a new project.
- **Request Body**: JSON representation of the project.
- **Example**:
  ```http
  POST /project/api/projects/66c5f70943e23b2bb24413ef
  {
        "name": "test",
        "description": "This is a description of the example project.",
        "region": "Joplin",
        "hazards": [
            {
                "id": "5c6323a0c11bb380daa9cbc1",
                "status": "UNAUTHORIZED",
                "type": "tornado"
            },
            {
                "id": "5b902cb273c3371e1236b36b",
                "status": "EXISTING",
                "type": "earthquake"
            }
        ],
        "dfr3Mappings": [
            {
                "id": "5b47b2d9337d4a36187c7563",
                "status": "EXISTING",
                "type": "fragility"
            }
        ],
        "datasets": [
            {
                "id": "5a284f0bc7d30d13bc081a28",
                "status": "EXISTING",
                "type": "ergo:buildingInventoryVer5"
            },
            {
                "id": "5a284f0bc7d30d13bc081a20",
                "status": "EXISTING",
                "type": "ergo:buildingInventoryVer5"
            }
        ],
        "workflows": []
    }
  ```

#### **Update a Project**

- **Method**: `PUT`
- **Path**: `/project/api/projects/{projectId}`
- **Description**: Update an existing project by its ID.
- **Path Parameter**:
    - `projectId` (string) - ID of the project to update.
- **Request Body**: JSON representation of the updated project.
- **Example**:
  ```http
  PUT /project/api/projects/66c5f70943e23b2bb24413ef
  {
        "name": "Updated project name",
        "description": "Updated project description.",
        "region": "Joplin",
        "hazards": [],
        "dfr3Mappings": [],
        "datasets": [],
        "workflows": []
    }
  ```

#### **Patch a Project**

- **Method**: `PATCH`
- **Path**: `/projects/{projectId}`
- **Description**: **Partially** update a project by its ID.
- **Path Parameter**:
    - `projectId` (string) - ID of the project to update.
- **Request Body**: URLEncoded Form data with fields to update.
  - **Example**:
    ```http
    curl --location --request PATCH '/project/api/projects/66c60ba518da486b1e9c08d5' \
    --header 'x-auth-userinfo: {"preferred_username":"cwang138"}' \
    --header 'x-auth-usergroup: {"groups":["incore_ncsa"]}' \
    --header 'Accept: application/json' \
    --header 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode 'name=test-patch' \
    --data-urlencode 'hazards={
        "id": "5c6323a0c11bb380daa9cbc1",
        "status": "UNAUTHORIZED",
        "type": "tornado"
      }' \
    --data-urlencode 'hazards={
        "id": "5b902cb273c3371e1236b36b",
        "status": "EXISTING",
        "type": "earthquake"
      }' \
    --data-urlencode 'datasets={
       "id": "5a284f0bc7d30d13bc081a28",
       "status": "EXISTING",
       "type": "ergo:buildingInventoryVer5"
      }'
    ```

#### **Delete a Project**

- **Method**: `DELETE`
- **Path**: `/project/api/projects/{projectId}`
- **Description**: Delete a project by its ID.
- **Path Parameter**:
    - `projectId` (string) - ID of the project to delete.
- **Example**:
  ```http
  DELETE /project/api/projects/66c60ba518da486b1e9c08d5
  ```

#### **List Datasets belong to a Project**

- **Method**: `GET`
- **Path**: `/project/api/projects/{projectId}/datasets`
- **Description**: Retrieve a list of datasets associated with a specific project.
- **Path Parameter**:
    - `projectId` (string) - ID of the project.
- **Example**:
  ```http
  GET /project/api/projects/66c60ba518da486b1e9c08d5/datasets
  ```

#### **Add Datasets to a Project**

- **Method**: `POST`
- **Path**: `/project/api/projects/{projectId}/datasets`
- **Description**: Add datasets to a specific project.
- **Path Parameter**:
    - `projectId` (string) - ID of the project.
- **Request Body**: List of datasets to add.
- **Example**:
  ```http
  POST /project/api/projects/66c60ba518da486b1e9c08d5/datasets
  [
   {
        "id": "5a284f0bc7d30d13bc081a20",
        "type": "ergo:buildingInventoryVer5"
    }
  ]
  ```

#### **Remove Datasets from a Project**

- **Method**: `DELETE`
- **Path**: `/project/api/projects/{projectId}/datasets`
- **Description**: Remove datasets from a specific project.
- **Path Parameter**:
    - `projectId` (string) - ID of the project.
- **Request Body**: List of datasets to remove.
- **Example**:
  ```http
  DELETE /project/api/projects/66c60ba518da486b1e9c08d5/datasets
  [
   {
        "id": "5a284f0bc7d30d13bc081a20",
        "type": "ergo:buildingInventoryVer5"
    }
  ]
  ```