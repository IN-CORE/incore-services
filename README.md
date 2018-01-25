# IN-CORE

The Interdependent Network Community Resilience Modeling Environment (IN-CORE) is a web based application that brings 
the capabilities of the version 1.0 desktop client to the web while adding new capabilities that aren't available in 
1.0 such as a REST API, support for tools in additional languages such as Python, support for spatio-temporal data, 
surface fragilities, communicating with external tools such as OpenSEES, overlaying data from web sources such as 
OpenStreetMap, NBI, NOAA, etc.

## Services

IN-CORE uses a service oriented architecture with a REST API for communicating with the different services.  

### API Gateway
IN-CORE uses Kong to provide an API Gateway to the service layer. Users must authenticate with the Authentication 
service and then use that token to access the other services through Kong.

### Authentication Service
The authentication service currently only supports LDAP authentication. Once authenticated, the service returns a 
token to access the other services. 

### Data Service
The data service uses MongoDB and provides basic capabililities to fetch/store data from file storage. In addition, the
data service uses a Geoserver to serve GIS data.

### Fragility Service
The fragility service uses MongoDB and supports fragilities and fragility mapping. Given a set of attributes, the 
service can find a matching fragility and return it to the user.

### Hazard Service
The hazard service uses MongoDB and supports creating scenario earthquakes. After creating a scenario earthquake, users 
can request different ground shaking demand types for a given location.

### Maestro Service
The maestro service uses MongoDB and provides basic support for creating new analyses. 

## Web application

The web front-end provides a user interface for interacting with the service layer. The initial prototype also includes
basic visualization capabilities for viewing fragilities, executing an analysis, etc. 
