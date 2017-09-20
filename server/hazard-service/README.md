After running gradle task hazard-service:jettyRun,
you should be able to do a get request to:

hazard/api/earthquake

0.2 SA
http://localhost:8080/hazard/api/earthquake/model?modelId=AtkinsonBoore1995&demandType=0.2+SA&demandUnits=g&siteLat=35.07899&siteLong=-90.0178&eqJson={"magnitude":"7.9","depth" :"10.0", "srcLatitude" : "35.927", "srcLongitude":"-89.919"}
0.3502

PGA
http://localhost:8080/hazard/api/earthquake/model?modelId=AtkinsonBoore1995&demandType=PGA&demandUnits=g&siteLat=35.07899&siteLong=-90.0178&eqJson={"magnitude":"7.9","depth" :"10.0", "srcLatitude" : "35.927", "srcLongitude":"-89.919"}

PGA = 0.1765

Site Amplification
http://localhost:8080/hazard/api/earthquake/soil/amplification?method=NEHRP&demandType=0.2+SA&siteLat=35.07899&siteLong=-90.0178&hazard=0.3502&defaultSiteClass=D
1.51984

hazard/api/tornado

http://localhost:8080/hazard/api/tornado/model?modelId=TornadoMeanWidth&datasetId=some-dataset-id&demandUnits=mph&siteLat=35.07899&siteLong=-90.0178





