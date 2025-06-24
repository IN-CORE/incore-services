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
    - `name`, `creator`, `owner`, `region`, `type`, `text`, `space`, `skip`, `limit`
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
- **Query Parameters**:
  - `skip`, `limit`, `type`, `text`
- **Example**:
  ```http
  GET /project/api/projects/66c60ba518da486b1e9c08d5/datasets?text=Building Inventory
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
    "id": "63053ddaf5438e1f8c517fed",
    "deleted": false,
    "title": "Galveston Buildlings",
    "description": "Galveston Buildings",
    "date": "2022-08-23T20:51:38+0000",
    "creator": "cwang138",
    "owner": "cwang138",
    "spaces": [
        "cwang138",
        "incore"
    ],
    "contributors": [],
    "fileDescriptors": [
        {
            "type":"ergo:buildingInventoryVer7",
            "id": "63053e4085ac6b569e37713b",
            "deleted": false,
            "filename": "galveston_bldgs_w_guid.shx",
            "mimeType": "application/octet-stream",
            "size": 1380372,
            "dataURL": "63/05/63053e4085ac6b569e37713b/galveston_bldgs_w_guid.shx",
            "md5sum": "a94f800d57affc7d2c53e3ea7a4e7739"
        },
        {
            "id": "63053e4085ac6b569e37713e",
            "deleted": false,
            "filename": "galveston_bldgs_w_guid.shp",
            "mimeType": "application/octet-stream",
            "size": 4831052,
            "dataURL": "63/05/63053e4085ac6b569e37713e/galveston_bldgs_w_guid.shp",
            "md5sum": "7334b46c5062d8271e2843850bd72ab5"
        },
        {
            "id": "63053e4185ac6b569e377141",
            "deleted": false,
            "filename": "galveston_bldgs_w_guid.dbf",
            "mimeType": "application/octet-stream",
            "size": 73500606,
            "dataURL": "63/05/63053e4185ac6b569e377141/galveston_bldgs_w_guid.dbf",
            "md5sum": "8ecc68be18baf3d3c8de905f481d97d7"
        },
        {
            "id": "63053e4185ac6b569e377144",
            "deleted": false,
            "filename": "galveston_bldgs_w_guid.prj",
            "mimeType": "application/octet-stream",
            "size": 145,
            "dataURL": "63/05/63053e4185ac6b569e377144/galveston_bldgs_w_guid.prj",
            "md5sum": "c742bee3d4edfc2948a2ad08de1790a5"
        }
    ],
    "dataType": "ergo:buildingInventoryVer7",
    "storedUrl": "",
    "format": "shapefile",
    "sourceDataset": "",
    "boundingBox": [
        -95.23305969592319,
        29.09475403626702,
        -94.380636631043,
        29.59576151450004
    ],
    "networkDataset": null
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
    "5a284f0bc7d30d13bc081a20",
    "5a284f0bc7d30d13bc081a28"
  ]
  ```


#### **List Dfr3 mappings belong to a Project**

- **Method**: `GET`
- **Path**: `/project/api/projects/{projectId}/dfr3mappings`
- **Description**: Retrieve a list of dfr3 mappings associated with a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Query Parameters**:
  - `skip`, `limit`, `type`, `hazardType`, `inventoryType`, `text`
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
    "id": "617c47d098c35d24cc573adc",
    "name": "pytest - Building Mean structural repair time mapping for RES1, RES2, RES3",
    "hazardType": "earthquake",
    "inventoryType": "building",
    "mappings": [
        {
            "legacyEntry": {},
            "entry": {
                "Repair ID Code": "617b79d898c35d24cc573ada"
            },
            "rules": {
                "AND": [
                    "java.lang.String occ_type EQUALS RES1"
                ]
            }
        },
        {
            "legacyEntry": {},
            "entry": {
                "Repair ID Code": "617b7aa7eb5adf3936e01e47"
            },
            "rules": {
                "AND": [
                    "java.lang.String occ_type EQUALS RES2"
                ]
            }
        },
        {
            "legacyEntry": {},
            "entry": {
                "Repair ID Code": "617b7ad298c35d24cc573adb"
            },
            "rules": {
                "AND": [
                    "java.lang.String occ_type EQUALS RES3"
                ]
            }
        }
    ],
    "creator": "jiateli",
    "owner": "jiateli",
    "mappingEntryKeys": null,
    "spaces": [
        "jiateli"
    ],
    "mappingType": "repair"
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
    "5b47b2d9337d4a36187c7563",
    "5b47b2d9337d4a36187c7564"
  ]
  ```

#### **List Hazards belong to a Project**

- **Method**: `GET`
- **Path**: `/project/api/projects/{projectId}/hazards`
- **Description**: Retrieve a list of hazards associated with a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Query Parameters**:
  - `skip`, `limit`, `type`, `text`
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
    "eqType": "dataset",
    "id": "5ba8f1c0ec2309043520906d",
    "name": "Seaside Probabilistic EQ 2500yr",
    "description": "Seaside dataset based probabilistic earthquake hazard 2500yr",
    "date": "2024-10-03T20:53:09+0000",
    "creator": "ywkim",
    "owner": "ywkim",
    "spaces": [
        "ywkim",
        "coe",
        "incore"
    ],
    "hazardDatasets": [
        {
            "hazardType": "probabilistic",
            "datasetId": "5ba8f1beec230904354e554c",
            "demandType": "PGA",
            "demandUnits": "g",
            "period": 0,
            "threshold": null,
            "recurrenceInterval": 2500,
            "recurrenceUnit": "years"
        },
        {
            "hazardType": "probabilistic",
            "datasetId": "5ba8f1bfec230904354e5558",
            "demandType": "PGD",
            "demandUnits": "g",
            "period": 0,
            "threshold": null,
            "recurrenceInterval": 2500,
            "recurrenceUnit": "years"
        },
        {
            "hazardType": "probabilistic",
            "datasetId": "5ba8f1bfec230904354e5564",
            "demandType": "SA",
            "demandUnits": "g",
            "period": 0.4,
            "threshold": null,
            "recurrenceInterval": 2500,
            "recurrenceUnit": "years"
        },
        {
            "hazardType": "probabilistic",
            "datasetId": "5ba8f1bfec230904354e5570",
            "demandType": "SA",
            "demandUnits": "g",
            "period": 0.35,
            "threshold": null,
            "recurrenceInterval": 2500,
            "recurrenceUnit": "years"
        },
        {
            "hazardType": "probabilistic",
            "datasetId": "5ba8f1bfec230904354e557c",
            "demandType": "SA",
            "demandUnits": "g",
            "period": 0.75,
            "threshold": null,
            "recurrenceInterval": 2500,
            "recurrenceUnit": "years"
        },
        {
            "hazardType": "probabilistic",
            "datasetId": "5ba8f1bfec230904354e5588",
            "demandType": "SA",
            "demandUnits": "g",
            "period": 1,
            "threshold": null,
            "recurrenceInterval": 2500,
            "recurrenceUnit": "years"
        },
        {
            "hazardType": "probabilistic",
            "datasetId": "5ba8f1c0ec230904354e5594",
            "demandType": "SA",
            "demandUnits": "g",
            "period": 1.45,
            "threshold": null,
            "recurrenceInterval": 2500,
            "recurrenceUnit": "years"
        }
    ]
   },
   {
    "floodType": "dataset",
    "id": "6526c53d288963151469731d",
    "name": "Galveston SLR - 8ft. (MHHW)",
    "description": "Galveston Sea Level Rise (SLR) - 8ft. (above MHHW); Data from NOAA Digital Coast; SLR relative to MHHW; Map of inundation footprint in feet.",
    "date": "2023-10-11T15:54:33+0000",
    "creator": "sanderdy",
    "owner": "sanderdy",
    "spaces": [
        "sanderdy"
    ],
    "hazardDatasets": [
        {
            "hazardType": "deterministic",
            "datasetId": "6526c539adce7a5cb978d51d",
            "demandType": "inundationDepth",
            "demandUnits": "ft",
            "threshold": null,
            "floodParameters": {
                "model": "NOAA SLR Inundation"
            }
        }
    ]
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
    "5b902cb273c3371e1236b36b",
    "5c6323a0c11bb380daa9cbc1"
  ]
  ```


#### **List Workflows belong to a Project**

- **Method**: `GET`
- **Path**: `/project/api/projects/{projectId}/workflows`
- **Description**: Retrieve a list of workflows associated with a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Query Parameters**:
  - `skip`, `limit`, `type`, `text`
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
        "deleted": false,
        "title": "retrofit_strategy_wf",
        "description": "",
        "created": "2024-03-15T18:51:21+0000",
        "creator": {
            "id": "d39e1679-4dcc-4158-9655-a604801871a7",
            "deleted": false,
            "firstName": "Christopher",
            "lastName": "Navarro",
            "email": "cmnavarr@illinois.edu"
        },
        "contributors": [],
        "type": "workflow"
    }
  ]
  ```

#### **Finalize Workflows belong to a Project**

- **Method**: `POST`
- **Path**: `/project/api/projects/{projectId}/workflows/{workflowId}/finalize`
- **Description**: Finalize workflow a specific workflow in a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
  - `workflowId` (string) - ID of the workflow.
- **Example**:
  ```http
  GET /project/api/projects/66c60ba518da486b1e9c08d5/workflows/c81ab517-77a3-472a-a1a6-db08d1fadb35/finalize

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
     "efc14b0f-1848-4dc7-ad0d-69ef6d7f0d9c",
     "e4d1c18-4250-4cc0-8fc6-d4f2afa4b9e7"
  ]
  ```

#### **List Visualizations belong to a Project**

- **Method**: `GET`
- **Path**: `/project/api/projects/{projectId}/visualizations`
- **Description**: Retrieve a list of visualizations associated with a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Query Parameters**:
  - `skip`, `limit`, `type`, `text`
- **Example**:
  ```http
  GET /project/api/projects/66c60ba518da486b1e9c08d5/visualizations
  ```


#### **Add Visualizations to a Project**

- **Method**: `POST`
- **Path**: `/project/api/projects/{projectId}/visualizations`
- **Description**: Add visualizations to a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Request Body**: List of visualizations to add.
- **Example**:
  ```http
  POST /project/api/projects/66c60ba518da486b1e9c08d5/visualizations
  [
    {
        "id": "6375502f3a28a17d261fd682",
        "type": "MAP",
        "boundingBox": [
            -90.07376669874641,
            35.03298062856903,
            -89.71464767735003,
            35.207753220358086
        ],
        "layers": [
            {
                "workspace": "incore",
                "layerId": "6375502f3a28a17d261fd682",
                "styleName": "incore:point"
            },
            {
                "workspace": "incore",
                "layerId": "6375502f3a28a17d261fd682",
                "styleName": "incore:point"
            }
        ]
    }
  ]
  ```

#### **Update Fields in a Visualization**

- **Method**: `PATCH`
- **Path**: `/project/api/projects/{projectId}/visualizations/{visualizationId}`
- **Description**: Patch fields of a specific visualization within a project. Supports partial updates via form parameters. Automatically synchronizes layerOrder if layers or layerOrder is modified.
- **Path Parameters**:
  - projectId (string) – ID of the project. 
  - visualizationId (string) – ID of the visualization to update.
- **Request Type**: application/x-www-form-urlencoded
- **Form Parameters (all optional)**:
  - name (string) – Name of the visualization. 
  - description (string) – Description of the visualization.
  - type (string) – Visualization type: MAP, CHART, or TABLE.
  - boundingBox (repeated double) – Array of 4 values: [minX, minY, maxX, maxY].
  - vegaJson (string) – Vega JSON spec for CHART/TABLE visualizations.
  - layerOrder (string of JSON array) – Ordered list of layer IDs.
  - layers (string of JSON array) – List of serialized Layer objects in JSON string format.
- **Example**:
```http
  PATCH /project/api/projects/66c60ba518da486b1e9c08d5/visualizations/6375502f3a28a17d261fd682
  Content-Type: application/x-www-form-urlencoded

  name=Building Footprints Map&
  type=MAP&
  boundingBox=10.0&boundingBox=20.0&boundingBox=30.0&boundingBox=40.0&
  layerOrder=["layer1","layer2"]&
  layers=[{"layerId":"layer1","displayName":"Buildings"},{"layerId":"layer2","displayName":"Roads"}]
```




#### **Remove Visualizations from a Project**

- **Method**: `DELETE`
- **Path**: `/project/api/projects/{projectId}/visualizations`
- **Description**: Remove visualizations from a specific project.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
- **Request Body**: List of visualizations to remove.
- **Example**:
  ```http
  DELETE /project/api/projects/66c60ba518da486b1e9c08d5/visualizations
  [
   "6375502f3a28a17d261fd682",
   "6375502f3a28a17d261fd683",
  ]
  ```
  
#### **Add Layers to Map Visualization**

- **Method**: `POST`
- **Path**: `/project/api/projects/{projectId}/visualizations/{visualizationId}/layers`
- **Description**: Add layers to a specific map visualization.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
  - `visualizationId` (string) - ID of the visualization.
- **Request Body**: List of layers to add.
- **Example**:
  ```http
  POST /project/api/projects/66c60ba518da486b1e9c08d5/visualizations/6375502f3a28a17d261fd682/layers
  [
    {
        "workspace": "incore",
        "layerId": "6375502f3a28a17d261fd682",
        "styleName": "incore:point"
    },
    {
        "workspace": "incore",
        "layerId": "6375502f3a28a17d261fd683",
        "styleName": "incore:polygon"
    }
  ]
  ```

#### **Update Layer in Map Visualization**

- **Method**: `PUT`
- **Path**: `/project/api/projects/{projectId}/visualizations/{visualizationId}/layers`
- **Description**: Update layer in a specific map visualization.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
  - `visualizationId` (string) - ID of the visualization.
- **Request Body**: New layer.
- **Example**:
  ```http
  PUT /project/api/projects/66c60ba518da486b1e9c08d5/visualizations/6375502f3a28a17d261fd682/layers
    {
        "workspace": "incore",
        "layerId": "6375502f3a28a17d261fd682",
        "styleName": "incore:polygon"
    }
  ```
  
#### **Remove Layers from Map Visualization**

- **Method**: `DELETE`
- **Path**: `/project/api/projects/{projectId}/visualizations/{visualizationId}/layers`
- **Description**: Remove layers from a specific map visualization.
- **Path Parameter**:
  - `projectId` (string) - ID of the project.
  - `visualizationId` (string) - ID of the visualization.
- **Request Body**: List of layers IDs to remove.
- **Example**:
  ```http
  DELETE /project/api/projects/66c60ba518da486b1e9c08d5/visualizations/6375502f3a28a17d261fd682/layers
  [
    "6375502f3a28a17d261fd682",
    "6375502f3a28a17d261fd683"
  ]
  ```