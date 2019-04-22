# README #

INCORE V2

### Skeleton for INCORE v2 source code ###

* To run the combined webapp in a dev server, run ./gradlew farmRun

REST Api data flow for test analysis:

To run test server:
```cmd
./gradlew farmRun
```

Retrieving dataset:
  To retrive metadataOld relating to a dataset:
  [http://localhost:8080/repo/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658](http://localhost:8080/repo/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658)

  To retrieve the dataset geojson:
  [http://localhost:8080/repo/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658/geojson](http://localhost:8080/repo/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658/geojson)

  To retrieve the raw shapefile(s):
  [http://localhost:8080/repo/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658/files](http://localhost:8080/repo/api/datasets/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0/Shelby_County_RES31224702005658/files)


To retrieve fragility mapping for an inventory item:
  
  //note: retrofit key is required for buildings to pick the right retrofit key, normally in V1 that's done by the analysis.
  ```
  http://localhost:8080/fragilitymapping/api/mapping/byJson?json={"no_stories":5, "year_built": 1990, "Soil": "Upland", "occ_type": "COM4", "struct_typ": "C1", "retrofit": "Non-Retrofit Fragility ID Code"}
  ```

  This returns a fragility Id:
  ```json
  {
      "fragilityId": "STR_C1_5"
  }
  ```
  This fragility Id can be used in the fragility service:


To use the fragility service:

  refer to API Documentation: [http://141.142.208.203/api/doc/index.html](http://141.142.208.203/api/doc/index.html) 



