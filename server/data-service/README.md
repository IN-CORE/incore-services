Overview
========
This is a Maestro service to list and register analyses within incore

How-to run
==========
1) Compile
The project compiles using gradle.  If you already have gradle installed, compile using:
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
    | /{id}	| GET | Returns Dataset about a particular dataset specified by {id} |
    | /list | GET | Returns a list of datasets in the Dataset collection | 
    | /list-spaces | GET | Returns a list of spaces in the Space collection |
    | /{id}/files |	GET	Returns a zip file that contains all the files attached to a dataset specified by {id} using FileDescriptor in the dataset |
    | /{id}/filedescriptors/{fdid}/files | GET | Returns a file that is attached to a FileDescriptor specified by {fdid} in a dataset specified by {id} |
    | /joinshptable/{id} | GET | Returns a zip file of shapefile after joinig analysis result table dataset specified by {id} using result dataset's source dataset shapefile |
    | /ingest-dataset | POST | ingest a dataset to create a new dataset object |
    | | | headers: Form |
    | | | content: multipart/form-data |
    | | | body: Form |
    | | | input: Dataset json |
    | | | input json example when it is a parent dataset (parameter name: dataset, item type: text) |
    | | | { schema: "buildingInventory", type: "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.schemas.buildingInventoryVer4.v1.0", title: "Shelby_County_Essential_Facilities", sourceDataset: "", format: "shapefile", spaces: ["ywkim", "ergo"] } |
    | | | input json example when it is a result dataset example (parameter name : dataset, item type: text) |
    | | | { schema: "buildingDamage", type: "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.schemas.buildingDamageVer4.v1.0", title: "shelby building damage", sourceDataset: "59e5098168f47426547409f3", format: "csv", spaces: ["ywkim", "ergo"] } |
    | | | output: Dataset |
    | /upload-files | POST | upload file(s) to attach to a dataset by FileDescriptor |
    | | | headers: Form |
    | | | content: multipart/form-data |
    | | | body: Form |
    | | | input: file(s) to upload and json contains the information about the dataset id for attaching the file |
    | | | input file selection example (parameter name: file, item type: File) |
    | | |     shapefile.shp |
    | | |     shapefile.shx |
    | | |     shapefilx.prj |
    | | |     shapefile.dbf |
    | | | input json example (parameter name: parentdataset, item type: Text) |
    | | |    {datasetId: "59e6107863f9401f64b86b4c"} |
    | | | output: Dataset |
    | /update | POST | file(s) to upload to attach to a dataset by FileDescriptor |
    | | | headers: Form
    | | | content: multipart/form-data
    | | | body: Form
    | | | input : json contains a dataset id to update and property name and value for update |
    | | | input json example (parameter name: update, item type: text) |
    | | | {datasetId: "59e0ec0c68f4742a340411d2", property name: "sourceDataset", property value: "59e0eb7d68f4742a342d9738"} |
    | | | output: Object (e.g. Dataset, Space) |
 

3) Deploy the war file to web container.  I've been using apache-tomcat [http://tomcat.apache.org], and typically copy the war to the tomcat webapps directory.  On my machine:
```
cp build/libs/jersey-starterkit.war /Applications/apache-tomcat-6.0.33/webapps/
```

Shortcut: if you are using tomcat, and $CATALINA_HOME is set, you can run: `./deploy.sh`


4) Confirm that it is running by fetching the URL at on webcontainer + /jersey-helloworld/rest/hello.  On my machine:
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

There is a log4j configuration defined in `src/main/resources/log4j.properties`.  By default this will log to the STDOUT and to a series of log files.  Change the logging configuration as needed.

If you would like to use the default logging, create the logging folders:
```
> sudo mkdir /restapi
> chmod a+wr /restapi
````

