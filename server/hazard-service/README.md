# Hazard Service

To start service: **gradle task hazard-service:jettyRun**

After starting the service, you should be able to create scenario earthquakes and scenario tornadoes.

## Earthquake

### Create Scenario Earthquake based on Attenuation Model

POST http://localhost:8080/hazard/api/earthquakes

Content-Type: multipart/form-data

###### Form Parameter: "earthquake" should hold the JSON. 

{
  "name": "Memphis EQ Model",
  "description": "Memphis model based hazard",
  "eqType": "model",
  "attenuations" : {
    "AtkinsonBoore1995" : "1.0"
  },
  "eqParameters" : {
    "srcLatitude" : "35.927",
    "srcLongitude" : "-89.919",
    "magnitude" : "7.9",
    "depth" : "10.0"
  },
  "visualizationParameters" : {
    "demandType" : "PGA",
    "demandUnits" : "g",
    "minX" :"-90.3099",
    "minY" :"34.9942",
    "maxX" : "-89.6231",
    "maxY" : "35.4129",
    "numPoints" : "1025",
    "amplifyHazard": "true"
  }
}

### Create Earthquake Dataset

POST http://localhost:8080/hazard/api/earthquakes

Content-Type: multipart/form-data

###### Form Parameter: "earthquake" should hold the earthquake JSON
###### Form Parameter: "file" should contain the uploaded file(s). Multiple files can be uploaded using the same "file" parameter

When creating an earthquake dataset, the files must be included in the POST in the order found in the JSON
so the correct dataset and metadataOld are associated. Currently only supports `.tif` files 

##### Sample JSON to create Probabilistic Earthquake

{
  "name": "Memphis Deterministic EQ",
  "description": "Memphis dataset based deterministic hazard",
  "eqType": "dataset",
  "hazardDatasets" :  [ 
    {
        "hazardType" : "deterministic",
        "demandType" : "SA",
        "demandUnits" : "g",
        "period" : "0.2",
        "eqParameters" : {
	    "srcLatitude" : "35.927",
	    "srcLongitude" : "-89.919",
	    "magnitude" : "7.9",
	    "depth" : "10.0"
	    }
    },
    {	
        "hazardType" : "deterministic",
        "demandType" : "PGA",
        "demandUnits" : "g",
        "period" : "0.0",
        "eqParameters" : {
            "srcLatitude" : "35.927",
            "srcLongitude" : "-89.919",
            "magnitude" : "7.9",
            "depth" : "10.0"
        }
    }    
  ]
}

##### Sample JSON to create Probabilistic Earthquake
{ 
  "name": "Memphis Probabilistic EQ",
  "description": "Memphis dataset based probabilistic hazard",
  "eqType": "dataset",
  "hazardDatasets" :  [
    {        
        "hazardType" : "probabilistic",
        "demandType" : "SA",
        "demandUnits" : "g",
	"recurrenceInterval" : "50",
	"recurrenceUnit" : "years",
        "period" : "0.2"
    },
    {        
        "hazardType" : "probabilistic",
        "demandType" : "PGA",
        "demandUnits" : "g",
	    "recurrenceInterval" : "50",
        "recurrenceUnit" : "years",
        "period" : "0.0"
    }   
  ]
}

### Get Values from Scenario Earthquake

GET value from a scenario earthquake

Site Amplification
http://localhost:8080/hazard/api/earthquakes/soil/amplification?method=NEHRP&demandType=0.2+SA&siteLat=35.07899&siteLong=-90.0178&hazard=0.3502&defaultSiteClass=D
1.51984

GET values from a scenario earthquake, all values must be for the same demand type and period, order of points should be latitude then longitude
http://localhost:8080/hazard/api/earthquakes/{id}/values?demandType=0.2+SA&demandUnits=g&point=35.07899,-90.0178&point=35.17899,-90.0178&point=35.07899,-90.1178

### Get Liquefaction Values

GET
http://localhost:8080/hazard/api/earthquakes/{id}/liquefaction/values?geologyDataset=5b0f05a5c6a4925f6fa3be72&demandUnits=in&point=35.07899,-90.0178&point=35.17899,-90.0178

### Generate Raster

http://localhost:8080/hazard/api/earthquakes/{id}/raster?demandType=0.2+SA&demandUnits=g&minX=-90.3099&minY=34.9942&maxX=-89.6231&maxY=35.4129&gridSpacing=0.01696

## Tsunamis

### Create Tsunami Dataset

POST http://localhost:8080/hazard/api/tsunamis

Content-Type: multipart/form-data

#### Create probabilistic tsunami

###### Form Parameter: "tsunami" should hold the Tsunami JSON
###### Form Parameter: "file" should contain the uploaded file(s). Multiple files can be uploaded using the same "file" parameter

When creating a tsunami dataset, the files must be included in the POST in the order found in the JSON
so the correct dataset and metadataOld are associated.

##### Sample Tsunami Json
{
  "name": "Seaside Probabilistic Tsunami - 100 yr",
  "description": "Seaside dataset based probabilistic tsunami hazard",
  "tsunamiType": "dataset",
  "hazardDatasets" :  [
    {
        "hazardType" : "probabilistic",
        "demandType" : "Vmax",
        "demandUnits" : "m/s", 
        "recurrenceInterval" : "100",
        "recurrenceUnit" : "years"
    },
    {
        "hazardType" : "probabilistic",
        "demandType" : "Mmax",
        "demandUnits" : "m^3/s^2", 
        "recurrenceInterval" : "100",
        "recurrenceUnit" : "years"
    },
    {
        "hazardType" : "probabilistic",
        "demandType" : "Hmax",
        "demandUnits" : "m", 
        "recurrenceInterval" : "100",
        "recurrenceUnit" : "years"
    }
   
  ]
}

### Get Value from Tsunami

GET /values

http://localhost:8080/hazard/api/tsunamis/{id}/values?demandType=Vmax&demandUnits=m/s&point=long,lat
http://localhost:8080/hazard/api/tsunamis/{id}/values?demandType=hmax&demandUnits=m&point=46.006,-123.935&point=46.007,-123.969

## Tornadoes

### Create Tornado based on Tornado Model
POST http://localhost:8080/hazard/api/tornadoes

Content-Type: multipart/form-data

Supported tornado models are:
* MeanWidthTornado
* RandomWidthTornado
* MeanLengthWidthAngleTornado
* RandomLengthWidthAngleTornado
* RandomAngleTornado

For the example below, replace the tornadoModel with the model you want to create and if applicable, update the number of simulations for how many tornadoes to create.

##### Form Parameter: "tornado" should hold the JSON, see example for creating mean width tornado model

{
  "name": "Centerville Tornado",
  "description": "Centerville mean width tornado hazard",
  "tornadoType": "model",
  "tornadoModel" : "MeanWidthTornado",
  "tornadoParameters" : {
    "efRating" : "EF5",
    "startLatitude" : "35.218",
    "startLongitude" : "-97.510",
    "randomSeed" : "1234",
    "endLatitude" : [35.246],
    "endLongitude" : [-97.438],
    "windSpeedMethod" : "1",
    "numSimulations" : "1"
  }
}

* tornadoModel (required) - specify tornado model (Mean width will create a tornado using the mean width from historical
data for the EF rating)
* efRating (required) - specify the Enhanced Fujita (EF) scale intensity of the tornado (EF0 - EF5)
* startLatitude/startLongitude (required) - specify the starting location of the tornado
* endLatitude/endLongitude (optional)- depending on the model, specify an end latitude/longitude value. Some tornado
models (e.g. mean length width, and angle) calcuate endLat and endLon, others (e.g. random angle) will generate multiple endpoints programmatically so the input must be passed as an array
* windSpeedMethod(optional) - for computing wind speed within an EF boundary, 0 indicates using linear interpolation, 1
indicates uniform random distribution. Default is Uniform random distribution.

### Create Tornado Dataset

POST http://localhost:8080/hazard/api/tornadoes

Content-Type: multipart/form-data

###### Form Parameter: "tornado" should hold the tornado JSON
###### Form Parameter: "file" should contain the uploaded file(s). Multiple files can be uploaded using the same "file" parameter

When creating a tornado dataset, the only file type currently supported are shapefiles.

##### Sample JSON to create Tornado Dataset

{
  "name": "Joplin Tornado",
  "description": "Joplin tornado hazard",
  "tornadoType": "dataset"
}

### Get Values from Tornado

http://localhost:8080/hazard/api/tornadoes/{id}/value?demandUnits=mph&siteLat=35.2265&siteLong=-97.4788
value should be between 65 and 85 mph

http://localhost:8080/hazard/api/tornadoes/{id}/value?demandUnits=mph&siteLat=35.2286&siteLong=-97.4770
value should be between 136 and 165 mph

http://localhost:8080/hazard/api/tornadoes/{id}/values?demandUnits=m&point=35.027,-90.131&point=35.16,-88.88

## Hurricanes 

### Create Hurricane Windfield (Creates shapefiles of windfield grids in data service)

POST http://localhost:8080/hazard/api/hurricaneWindfields/

Content-Type: application/json

{
	"name": "Florida coast cat-1 hurricane",
	"description": "Simulated test hurricane",
	"coast": "florida",
	"category": 1,
    "demandType": "3s",
    "demandUnits": "mph",
	"gridResolution": 6,
	"rasterResolution": 1,
	"transD": -83,
	"landfallLocation": "28.08, -80.61",
	"gridPoints": 10
}

### Simulate a Hurricane Windfield by returning the result as JSON 

GET http://localhost:8080/hazard/api/hurricaneWindfields/json/florida?category=1&TransD=-83&LandfallLoc=28.08,-80.61&resolution=6&gridPoints=10&demandType=3s&demandUnits=mph

This API does not store shapefiles in data service and is probably not needed in production environment

### GET values from hurricane simulation

GET http://localhost:8080/hazard/api/hurricaneWindfields/5bcfe8a82d2ad516bb7ebac3/values?point=28.01,-81.01&demandType=60s&elevation=32.1&roughness=2.2&demandUnits=mps

The converted json model files are also in box:
https://uofi.box.com/s/ycneuzlzyt8rqwuu03gfeq4srbhucpch
