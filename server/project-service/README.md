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
  POST /project/api/projects
  {
        "name": "test",
        "description": "This is a description of the example project.",
        "region": "Joplin",
        "hazards": [
            {
                "id": "5c6323a0c11bb380daa9cbc1",
                "type": "tornado"
            },
            {
                "id": "5b902cb273c3371e1236b36b",
                "type": "earthquake"
            }
        ],
        "dfr3Mappings": [
            {
                "id": "5b47b2d9337d4a36187c7563",
                "type": "fragility"
            }
        ],
        "datasets": [
            {
                "id": "5a284f0bc7d30d13bc081a28",
                "type": "ergo:buildingInventoryVer5"
            },
            {
                "id": "5a284f0bc7d30d13bc081a20",
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
        "type": "tornado"
      }' \
    --data-urlencode 'hazards={
        "id": "5b902cb273c3371e1236b36b",
        "type": "earthquake"
      }' \
    --data-urlencode 'datasets={
       "id": "5a284f0bc7d30d13bc081a28",
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


#### **List Dfr3 mappings belong to a Project**

- **Method**: `GET`
- **Path**: `/project/api/projects/{projectId}/dfr3mappings`
- **Description**: Retrieve a list of dfr3 mappings associated with a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Example**:
  ```http
  GET /project/api/projects/66c60ba518da486b1e9c08d5/dfr3mappings
  ```

#### **Add Dfr3 mappings to a Project**

- **Method**: `POST`
- **Path**: `/project/api/projects/{projectId}/dfr3mappings`
- **Description**: Add dfr3mappings to a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Request Body**: List of dfr3mappings to add.
- **Example**:
  ```http
  POST /project/api/projects/66c60ba518da486b1e9c08d5/dfr3mappings
  [
   {
            "id": "5b47b2d9337d4a36187c7563",
            "type": "fragility"
        }
  ]
  ```

#### **Remove Dfr3 mappings from a Project**

- **Method**: `DELETE`
- **Path**: `/project/api/projects/{projectId}/dfr3mappings`
- **Description**: Remove dfr3mappings from a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Request Body**: List of dfr3mappings to remove.
- **Example**:
  ```http
  DELETE /project/api/projects/66c60ba518da486b1e9c08d5/dfr3mappings
  [
   {
            "id": "5b47b2d9337d4a36187c7563",
            "type": "fragility"
        }
  ]
  ```

#### **List Hazards belong to a Project**

- **Method**: `GET`
- **Path**: `/project/api/projects/{projectId}/hazards`
- **Description**: Retrieve a list of hazards associated with a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Example**:
  ```http
  GET /project/api/projects/66c60ba518da486b1e9c08d5/hazards
  ```

#### **Add Hazards to a Project**

- **Method**: `POST`
- **Path**: `/project/api/projects/{projectId}/hazards`
- **Description**: Add hazards to a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Request Body**: List of hazards to add.
- **Example**:
  ```http
  POST /project/api/projects/66c60ba518da486b1e9c08d5/hazards
  [
   {
            "id": "5b902cb273c3371e1236b36b",
            "type": "earthquake"
        },
        {
            "id": "5c6323a0c11bb380daa9cbc1",
            "type": "tornado"
        }
  ]
  ```

#### **Remove Hazards from a Project**

- **Method**: `DELETE`
- **Path**: `/project/api/projects/{projectId}/hazards`
- **Description**: Remove hazards from a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Request Body**: List of hazards to remove.
- **Example**:
  ```http
  DELETE /project/api/projects/66c60ba518da486b1e9c08d5/hazards
  [
   {
            "id": "5b902cb273c3371e1236b36b",
            "type": "earthquake"
        }
  ]
  ```


#### **List Workflows belong to a Project**

- **Method**: `GET`
- **Path**: `/project/api/projects/{projectId}/workflows`
- **Description**: Retrieve a list of workflows associated with a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Example**:
  ```http
  GET /project/api/projects/66c60ba518da486b1e9c08d5/workflows
  ```

#### **Add Workflows to a Project**

- **Method**: `POST`
- **Path**: `/project/api/projects/{projectId}/workflows`
- **Description**: Add workflows to a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Request Body**: List of workflows to add.
- **Example**:
  ```http
  POST /project/api/projects/66c60ba518da486b1e9c08d5/workflows
  [
   {
            "id": "efc14b0f-1848-4dc7-ad0d-69ef6d7f0d9c",
            "type": "workflow"
        },
        {
            "id": "e4d1c18-4250-4cc0-8fc6-d4f2afa4b9e7",
            "type": "execution"
        }
  ]
  ```

#### **Remove Workflows from a Project**

- **Method**: `DELETE`
- **Path**: `/project/api/projects/{projectId}/workflows`
- **Description**: Remove workflows from a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Request Body**: List of workflows to remove.
- **Example**:
  ```http
  DELETE /project/api/projects/66c60ba518da486b1e9c08d5/workflows
  [
   {
            "id": "e4d1c18-4250-4cc0-8fc6-d4f2afa4b9e7",
            "type": "execution"
        }
  ]
  ```