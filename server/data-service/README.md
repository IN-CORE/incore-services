Overview
========
This is a Maestro service to list and register analyses within incore

How-to run
==========

1) Compile The project compiles using gradle. If you already have gradle installed, compile using:

```
gradle build
```

If you do not have gradle installed, you can utilize the gradle wrapper included in the source

```
./gradlew war
```

The war file is compiled to: `build/libs/jersey-starterkit.war`

To start the service

```
./gradlew data:AppRun
```

2) Usage

   Data (Repository) Service (http://hostname/data/api/datasets)

   | Route | Method | Description | 
      | ----- | ------ | ----------- | 
   | / | GET | Returns a list of datasets in the Dataset collection | 
   | /{id}    | GET | Returns Dataset about a particular dataset specified by {id} |
   | /{id}/blob |    GET    | Returns a zip file that contains all the files attached to a dataset specified by {id} using FileDescriptor in the dataset |
   | /{id}/files/{file_id} | GET | Return a FileDescriptor with given file_id and dataset id |
   | /{id}/files/{file_id}/blob | GET | Returns a file that is attached to a FileDescriptor specified by {file_id} in a dataset specified by {id} |
   | /files | GET | Return list of FileDescriptors | 
   | / | POST | Post a dataset to create a new dataset object |
   | | | headers: Form |
   | | | content: multipart/form-data |
   | | | body: Form |
   | | | input: Dataset json |
   | | | input json example when it is a parent dataset (parameter name: dataset, item type: text) |
   | | | { dataType: "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.schemas.buildingInventoryVer4.v1.0", title: "Shelby_County_Essential_Facilities", sourceDataset: "", format: "shapefile"} |
   | | | input json example when it is a result dataset example (parameter name : dataset, item type: text) |
   | | | { dataType: "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.schemas.buildingDamageVer4.v1.0", title: "shelby building damage", sourceDataset: "59e5098168f47426547409f3", format: "csv"} |
   | | | input json example when it is a parent network dataset (parameter name: dataset, item type: text) |
   | | | { dataType: "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.schemas.buildingInventoryVer4.v1.0", title: "Shelby_County_Essential_Facilities", sourceDataset: "", format: "shp-network", networkDataset:{link:{dataType:  "pipeline"}, node:{dataType: "water facility"}, graph:{dataType: "table"}} |
   | | | output: Dataset |
   | /{id}/files | POST | upload file(s) to attach to a dataset by FileDescriptor |
   | | | headers: Form |
   | | | content: multipart/form-data |
   | | | body: Form |
   | | | input: file(s) to upload and json contains the information about the dataset id for attaching the file |
   | | | input file selection example (parameter name: file, item type: File) |
   | | |     shapefile.shp |
   | | |     shapefile.shx |
   | | |     shapefilx.prj |
   | | |     shapefile.dbf |
   | | | When it is a network dataset, the parameter name should be |
   | | | link-file, node-file, graph-file |
   | | | output: Dataset |
   | /{id} | PUT | Update properties of datasets |
   | | | headers: Form
   | | | content: multipart/form-data
   | | | body: Form
   | | | input json example (parameter name: update, item type: text) |
   | | | {"property name": "sourceDataset", "property value": "59e0eb7d68f4742a342d9738"} |
   | | | output: Object (e.g. Dataset) |
   |/{id} | DELETE | Delete the dataset |
   | | | the files related to the dataset set also be deleted |
   | | | if the file is uploaded in geoserver, it will also be deleted |


3) Deploy the war file to web container. I've been using apache-tomcat [http://tomcat.apache.org], and typically copy
   the war to the tomcat webapps directory. On my machine:

```
cp build/libs/jersey-starterkit.war /Applications/apache-tomcat-6.0.33/webapps/
```

Shortcut: if you are using tomcat, and $CATALINA_HOME is set, you can run: `./deploy.sh`

4) Confirm that it is running by fetching the URL at on webcontainer + /jersey-helloworld/rest/hello. On my machine:

```
curl localhost:8080/jersey-starterkit/rest/hello
```

The supported endpoints are:

```
http://localhost:8080/jersey-starterkit/rest/customer/id/1
```

```
http://localhost:8080/jersey-starterkit/rest/echo?m=hello
```

```
http://localhost:8080/jersey-starterkit/rest/hello
```

There is a log4j configuration defined in `src/main/resources/log4j.properties`. By default this will log to the STDOUT
and to a series of log files. Change the logging configuration as needed.

If you would like to use the default logging, create the logging folders:

```
> sudo mkdir /restapi
> chmod a+wr /restapi
````

