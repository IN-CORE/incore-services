# IN-CORE Web Services

IN-CORE Web Services is a component of IN-CORE. IN-CORE uses a service oriented architecture with a REST API for communicating with the different services. The followings are services in IN-CORE Web Services in this repository

## Authentication Service
The authentication service  

## Data Service
The data service uses MongoDB and provides basic capabililities to fetch/store data from file storage. In addition, the data service uses a Geoserver to serve GIS data.

## DFR3 Service
DFR3 stands for Damage, Functionality, Repair, Restoration, Recovry. The service uses MongoDB to store and manage DFR3 curves and mapping. Given a set of attributes, the service can find a matching entity and return it to the user.

## Hazard Service
The hazard service supports creating hazard objects such as earthquake, tornado, tsunami, and hurrican-wind. After creating a hazard, users can request hazard value of a given location.

## Space Service
The spce service manages groups of users and control access by those groups to certain entity throughout services.

