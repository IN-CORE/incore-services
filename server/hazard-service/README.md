# Hazard Service

To start service: **gradle task hazard-service:jettyRun**

After starting the service, you should be able to create scenario earthquakes and scenario tornadoes.

## Earthquake

### Create Scenario Earthquake

http://localhost:8080/hazard/api/earthquakes

POST - Create scenario earthquake

{
  "attenuations" : {
    "AtkinsonBoore1995" : "1.0"
  },
  "eqParameters" : {
    "srcLatitude" : "35.927",
    "srcLongitude" : "-89.919",
    "magnitude" : "7.9",
    "depth" : "10.0"
  }
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

### Generate Raster

http://localhost:8080/hazard/api/earthquakes/{id}/raster?demandType=0.2+SA&demandUnits=g&minX=-90.3099&minY=34.9942&maxX=-89.6231&maxY=35.4129&gridSpacing=0.01696

## Tornadoes

### Create Scenario Tornado

http://localhost:8080/hazard/api/tornadoes

POST - Create scenario tornado

{
  "tornadoModel" : "MeanWidthTornado",
  "tornadoParameters" : {
    "efRating" : "EF5",
    "startLatitude" : "35.218",
    "startLongitude" : "-97.510",
    "randomSeed" : "1234",
    "endLatitude" : [35.246],
    "endLongitude" : [-97.438],
    "windSpeedMethod" : "1"
  }
}

* tornadoModel (required) - specify tornado model (Mean width will create a tornado using the mean width from historical
data for the EF rating)
* efRating (required) - specify the Enhanced Fujita (EF) scale intensity of the tornado (EF0 - EF5)
* startLatitude/startLongitude (required) - specify the starting location of the tornado
* endLatitude/endLongitude (optional)- depending on the model, specify an end latitude/longitude value. Some tornado
models (e.g. random angle) will generate multiple endpoints programmatically so the input must be passed as an array
* windSpeedMethod(optional) - for computing wind speed within an EF boundary, 0 indicates using linear interpolation, 1
indicates uniform random distribution. Default is Uniform random distribution.

### Get Values from Scenario Tornado

http://localhost:8080/hazard/api/tornadoes/{id}/value?demandUnits=mph&siteLat=35.2265&siteLong=-97.4788
value should be between 65 and 85 mph

http://localhost:8080/hazard/api/tornadoes/{id}/value?demandUnits=mph&siteLat=35.2286&siteLong=-97.4770
value should be between 136 and 165 mph
