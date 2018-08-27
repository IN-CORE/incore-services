# Hazard Service

To start service: **gradle task hazard-service:jettyRun**

After starting the service, you should be able to create scenario earthquakes and scenario tornadoes.

## Earthquake

### Create Scenario Earthquake

http://localhost:8080/hazard/api/earthquakes

POST - Create scenario earthquake
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

When creating an earthquake dataset, the files must be include in the POST in the order found in the JSON
so the correct dataset and metadata are associated.

http://localhost:8080/hazard/api/earthquakes

POST - Create deterministic earthquake
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

POST - Create probabilistic earthquake
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

http://localhost:8080/hazard/api/earthquakes/{id}/value?demandType=0.2+SA&demandUnits=g&siteLat=35.07899&siteLong=-90.0178
Value: 0.3502

Site Amplification
http://localhost:8080/hazard/api/earthquakes/soil/amplification?method=NEHRP&demandType=0.2+SA&siteLat=35.07899&siteLong=-90.0178&hazard=0.3502&defaultSiteClass=D
1.51984

GET values from a scenario earthquake, all values must be for the same demand type and period, order of points should be latitude then longitude
http://localhost:8080/hazard/api/earthquakes/{id}/values?demandType=0.2+SA&demandUnits=g&point=35.07899&point=-90.0178&point=35.17899&point=-90.0178&point=35.07899&point=-90.1178

### Get Liquefaction Values

GET
http://localhost:8080/hazard/api/earthquakes/{id}/liquefaction/values?geologyDataset=5b0f05a5c6a4925f6fa3be72&demandUnits=in&point=35.07899&point=-90.0178&point=35.17899&point=-90.0178

### Generate Raster

http://localhost:8080/hazard/api/earthquakes/{id}/raster?demandType=0.2+SA&demandUnits=g&minX=-90.3099&minY=34.9942&maxX=-89.6231&maxY=35.4129&gridSpacing=0.01696

## Tornadoes

### Create Scenario Tornado
Supported tornado models are:
* MeanWidthTornado
* RandomWidthTornado

For the example below, replace the tornadoModel with the model you want to create and if applicable, update the number of simulations for how many tornadoes to create.

http://localhost:8080/hazard/api/tornadoes

POST - Create mean width scenario tornado

{
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

### Get Values from Scenario Tornado

http://localhost:8080/hazard/api/tornadoes/{id}/value?demandUnits=mph&siteLat=35.2265&siteLong=-97.4788
value should be between 65 and 85 mph

http://localhost:8080/hazard/api/tornadoes/{id}/value?demandUnits=mph&siteLat=35.2286&siteLong=-97.4770
value should be between 136 and 165 mph

http://localhost:8080/hazard/api/earthquakes/{id}/values?demandUnits=mph&point=35.1393&point=-89.9996&point=35.207&point=-89.871
