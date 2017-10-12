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
To uploade parent file (e.g. shapefile)
    1) create dataset object first by ingesting necessary information
        ```
        http://localhost:8080/data/api/datasets/ingest-dataset 
        ```
        headers:
        Content-Type: multipart/form-data 
        
        body:
        dataset Text 
        { schema: "buildingDamage", type: "http://localhost:8080/semantics/edu.illinois.ncsa.ergo.eq.buildings.schemas.buildingInventoryVer5.v1.0", sourceDataset: "59dce5d3a748be10cc9c4ea0", format: "csv", spaces: ["ywkim", "ergo"] }
        
        If the dataset is parent the sourceDataset should be empty
        
        ![Figure 1](images/ingest-dataset.png?raw=true)

    2) uploade files and tie them with the dataset object ingested above
        
        ![Figure 2](images/ingest-multi-files.png?raw=true)

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

