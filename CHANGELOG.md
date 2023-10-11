# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.21.0] - 2023-10-11

### Added
- Add template dataset generation (CSV and Shapefile) API to semantics-service [#214](https://github.com/IN-CORE/incore-services/issues/214)
- Owner item added to the dataset, hazard, and dfr3 object [#92](https://github.com/IN-CORE/incore-services/issues/92)
- Attenuation model Sadigh et al. 1997 [#208](https://github.com/IN-CORE/incore-services/issues/208)
- Include incore lab quota to the allocation endpoints [#217](https://github.com/IN-CORE/incore-services/issues/217)
- Allow semantics id to be added to space member [#229](https://github.com/IN-CORE/incore-services/issues/229)

### Changed
- Geoserver connection library has been removed and new connection object has been added [#190](https://github.com/IN-CORE/incore-services/issues/190)
- Semantics should display description instead of title [#206](https://github.com/IN-CORE/incore-services/issues/206)

### Fixed
- Semantic services endpoint returns correct ID [#227](https://github.com/IN-CORE/incore-services/issues/227)

## [1.20.0] - 2023-08-16

### Added
- Endpoints to return allowed demand types and units [#155](https://github.com/IN-CORE/incore-services/issues/155)
- Refactor POST /types API [#159](https://github.com/IN-CORE/incore-services/issues/159)
- Refactor GET /types endpoint [#156](https://github.com/IN-CORE/incore-services/issues/156)
- Refactor DELETE /types/{id} endpoint [#160](https://github.com/IN-CORE/incore-services/issues/160)
- Added filtering by space in GET /types endpoint [191](https://github.com/IN-CORE/incore-services/issues/191)
- Added limiting and offset for GET /types/search endpoint and to GET /types endpoint [#195](https://github.com/IN-CORE/incore-services/issues/195)

### Changed
- Refactor AllocationsController and UsageController to use updated authorizer [#143](https://github.com/IN-CORE/incore-services/issues/143)
- Upgraded project and space-service dependencies: Jersey 3.1.2 and swagger 2.1.12 for OpenAPI 3 [#152](https://github.com/IN-CORE/incore-services/issues/152)
- Upgraded project and semantics-service dependencies: Jersey 3.1.2 and swagger 2.1.12 for OpenAPI 3 [#167](https://github.com/IN-CORE/incore-services/issues/167)
- Upgraded project and hazard-service dependencies: Jersey 3.1.2 and swagger 2.1.12 for OpenAPI 3 [#163](https://github.com/IN-CORE/incore-services/issues/163)
- Upgraded project and data-service dependencies: Jersey 3.1.2 and swagger 2.1.12 for OpenAPI 3 [#165](https://github.com/IN-CORE/incore-services/issues/165)
- Refactor DAO for Semantics service [#151](https://github.com/IN-CORE/incore-services/issues/151)
- Removed jetty-runner 9 and replaced with Jetty 11 docker image [#174](https://github.com/IN-CORE/incore-services/issues/174)
- Upgraded geotools to version 29.0 and refactored codes  [#186](https://github.com/IN-CORE/incore-services/issues/186)
- Removed maestro service from the incore services repository [#145](https://github.com/IN-CORE/incore-services/issues/145)

### Fixed
- Missing log4j.properties file required to configure logging [#148](https://github.com/IN-CORE/incore-services/issues/148)
- Changed the endpoint /types/{name} response to Document [#193](https://github.com/IN-CORE/incore-services/pull/193)

## [1.14.0] - 2023-06-14
### Added
- Query parameters for sorting by given field in ascending and descending order [#111](https://github.com/IN-CORE/incore-services/issues/111)
- Get user group function to read and parse user groups from headers [#120](https://github.com/IN-CORE/incore-services/issues/120)

### Changed
- Remove LdapClient and refactor the Authorizer class along with its interface to take in userGroups parameter and modify internal calls too [#118](https://github.com/IN-CORE/incore-services/issues/118)
- Add userGroups parameter in Semantics Controller [#124](https://github.com/IN-CORE/incore-services/issues/124) 
- Add userGroups parameter in Space Controller [#123](https://github.com/IN-CORE/incore-services/issues/123)
- Add userGroups parameter in DFR3 Controller [#122](https://github.com/IN-CORE/incore-services/issues/122)
- Add userGroups parameter in Data Controller [#121](https://github.com/IN-CORE/incore-services/issues/121)
- Add userGroups parameter in Hazard Controller [#119](https://github.com/IN-CORE/incore-services/issues/119)

## [1.13.1] - 2023-03-15
### Fixed
- Earthquake hazard raster didn't handle values below the threshold that were set to null[#108](https://github.com/IN-CORE/incore-services/issues/108)

## [1.13.0] - 2023-03-08
### Added
- Space endpoint for getting space by providing the name of the space [#101](https://github.com/IN-CORE/incore-services/issues/101)

## [1.12.0] - 2022-11-16
### Changed
- Enforce demand type and demand unit check when POST to hazard values endpoint [#15](https://github.com/IN-CORE/incore-services/issues/15)

### Fixed
- Liquefaction did not handle the case of no exposure correctly [#83](https://github.com/IN-CORE/incore-services/issues/83)
- Keyword searching in fragility mapping is not behaving correctly [#78](https://github.com/IN-CORE/incore-services/issues/78)

## [1.11.0] - 2022-09-14
### Added
- Enable more demand types and demand units for Hurricane [#85](https://github.com/IN-CORE/incore-services/issues/85)

## [1.10.0] - 2022-06-29
### Added
- Endpoint to obtain the allocation for a logged-in user [#76](https://github.com/IN-CORE/incore-services/issues/76)

### Changed
- Network dataset's sub data's dataType changed from networkType to dataType [#79](https://github.com/IN-CORE/incore-services/issues/79)

## [1.9.0] - 2022-03-29
### Added
- User Allocation management to limit usage per user or group. Added validations to deny creation of new datasets, hazards and DFR3 curves when the allocations limits are met. [#66](https://github.com/IN-CORE/incore-services/issues/66) 

### Changed
- Earthquake threshold values to be agnostic of period values - with this change it is enough to just define a single threshold value for period based demands such as "SA" and "SD". [#32](https://github.com/IN-CORE/incore-services/issues/32)

## [1.8.0] - 2022-02-07
### Added
- Toro1997 attenuations model for earthquakes [#35](https://github.com/IN-CORE/incore-services/issues/35)
- Support for storing expression based curves for Repairs and Restorations [#6](https://github.com/IN-CORE/incore-services/issues/6)

### Changed
- Fully deprecate the old format DFR3 models and related code references [#31](https://github.com/IN-CORE/incore-services/issues/31)

## [1.7.1] - 2021-12-16
### Fixed
- Vulnerable log4j package updated to prevent remote code execution [#55](https://github.com/IN-CORE/incore-services/issues/55)

### Changed
- renamed master branch of the repository to be "main", and updated the github actions.

## [1.7.0] - 2021-12-15
### Changed
- Update earthquake values endpoint to accept site classification dataset [#40](https://github.com/IN-CORE/incore-services/issues/40)
- Add validation on DFR3 service to only acceptable demand types and units are being provided. Make it uniform on hazard service too enhancement [#7](https://github.com/IN-CORE/incore-services/issues/7)
- Upgrades and cleanup to semantics service [#18](https://github.com/IN-CORE/incore-services/issues/18)

## [1.6.0] - 2021-10-27
### Added
- Ability to store demand-specific threshold values in the metadata when creating hazards (pending for model-based earthquakes) [#1](https://github.com/IN-CORE/incore-services/issues/1)
- Maestro service endpoints to record the status of playbook workflow and the status of each step [#16](https://github.com/IN-CORE/incore-services/issues/16)
- Ability to include or exclude hazard datasets when querying for datasets through data service api calls [#10](https://github.com/IN-CORE/incore-services/issues/10)
- Github action to run unit tests and automatically publish docker images [#20](https://github.com/IN-CORE/incore-services/issues/20)

### Fixed
- Bug that was allowing updating datasets, hazards and DFR3 objects through the POST method that was supposed to only create new entities. [#22](https://github.com/IN-CORE/incore-services/issues/22)

## [1.5.0] - 2021-08-31
### Added
- User status endpoint for DFR3 Service to show usage information [INCORE1-1156](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1156)
- Support for hazard exposure for earthquake, tornado, tsunami, tornado and hurricane. Defined hazard thresholds for each of them [INCORE1-1361](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1361)

### Changed
- Exception handling on hazard service to return specific error codes (-9999.x) on failure [INCORE1-1364](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1364)

### Fixed
- Code formatting issues and reformatted consistently accross all services [INCORE1-1335](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1335)

## [1.4.0] - 2021-07-28

### Added
- User status endpoints for data and hazard services. It returns the no of entities and data usage for each user. [INCORE1-1155](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1155)
- New parameter "seed" value to the endpoint that gets tornado values to generate uniform random values. [INCORE1-1226](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1226)

### Changed
- Return no exposure (null) when the hazard value is out of bounds or below a threshold. Only supported for tornado & floods. [INCORE1-973](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-973)
- Migrated all fragilities in the DFR3 database to new format. [INCORE1-869](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-869)
- Deprecated the fragility curve formats that are not used anymore. [INCORE1-1269](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1241)

### Fixed
- Remove geoserver store when deleting a dataset. [INCORE1-1225](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1225)
- Hazard service was giving wrong values when demand type is of the format "x.x sec SD". [INCORE1-1258](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1258)
- Errors on GUID validation when uploading datasets. [INCORE1-1282](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1282)

## [1.3.0] - 2021-06-16

### Added
- Ability to create tornados by uploading a zip file with shapefiles [INCORE1-830](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-830)
- New field to DFR3 curve paramters to support an equation-friendly short name and a full name [INCORE1-1220](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1220)

### Changed
- Migrated all fragilities in the DFR3 database to new equation based format [INCORE1-869](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-869)

### Fixed
- Fix the mappings in the database without any rules defined [INCORE1-1215](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1215)
- Delete geoserver layer when deleting geopkg datasets [INCORE1-1208](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1208)

## [1.2.1] - 2021-05-21

### Changed
- Increased memory size for the service containers [INCORE1-1201](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1201)

## [1.2.0] - 2021-05-19

### Added
- Zipped shapefile upload to data service [INCORE1-1047](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1047)
- Allow mapping "rules" to be an Array or HashMap [INCORE1-1153](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1153)

### Changed
- Update versioning and tagging of docker images [INCORE1-1154](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1154)
- Updated model based tornado to use seed value for uniform random distribution [INCORE1-1187](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1187)

## [1.1.1] - 2021-04-15

### Fixed
- Tornado model shapefile needed to include a GUID [INCORE1-1120](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1120)

## [1.1.0] - 2021-04-14

### Changed
- Make data service to deny POSTs when GUID is not in the shapefile [INCORE1-1056](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1056)
- For CSV datasets with source inventory dataset, the merge data is now saved as geopackage (instead of shapefiles) on the geoserver. 
  This also fixes a limitation with shapefiles attributes to only have field names under 10 characters [INCORE1-762](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-762) 

### Fixed
- Fix CORS issue to work with the new platform. Move CORS variables to env [INCORE1-1087](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-1087)

## [1.0.0] - 2021-02-24

### Added
- POST endpoints to get hazard values for multiple demand types in a single call [INCORE1-921](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-921)

### Changed
- Convert demandType & demandUnits in DFR3 jsons to be arrays instead of comma separated strings [INCORE1-819](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-819)
- Upgrade morphia from 1.5 to 2.1. Replace deprecated morphia methods [INCORE1-703](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-703)
- Upgrade packages to be compatible with Java 11 [INCORE1-885](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-885)
- Get mongo hostname from env [INCORE1-936](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-936)

### Fixed
- Space information is being saved in Dataset collection even though it is a read-only field [INCORE1-918](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-918)
- Hurricane windfield full grid snapshot datasets are being saved as raster [INCORE1-926](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-926)

## [0.9.1] - 2020-12-18

### Added
- Included space information in the GET request jsons of datasets, hazards and DFR3 objects. [INCORE1-889](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-889)

## [0.9.0] - 2020-10-28

### Added
- Support for Parametric fragilities with a common fragility json format [INCORE1-783](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-783)
- DELETE endpoints for Fragilities, Repairs, Restorations & Mappings [INCORE1-614](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-614)
- GET endpoint to fetch unique dataset datatypes [INCORE1-807](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-807)
- Function to FileUtils, fileUseGeoserver, to take a file name string, and determine based on the extension if it should use Geoserver [INCORE1-612](https://opensource.ncsa.illinois.edu/jira/projects/INCORE1/issues/INCORE1-612)

### Changed
- Updated datatypes of hazard datasets to include space prefix (example: 'incore:') [INCORE1-707](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-707)

### Fixed
- Some of the gradle build warnings. Removed maestro service from the gradle farm [INCORE1-658](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-658)
- Bug in adding hurricanes & floods to a space [INCORE1-790](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-790)
- Bug in adding members to spaces [INCORE1-727](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-727)

## [0.8.0] - 2020-09-04

### Added
- Flood endpoints to accept raster based deterministic flood. [INCORE1-746](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-746) 

### Changed
- Alter the dfr3 status endpoint to include service version and database connection test. [INCORE-466](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-466)

### Fixed
- Added validation so PUT dataset endpoint will only allow updating string fields. It will return appropriate error message with HTTP response for non-string fields [INCORE1-767](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-767)

### Security
- Updated jax-rs and jersey dependencies to the latest versions [INCORE1-653](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-653)


## [0.7.0] - 2020-07-31

### Added 
- Hurricane endpoints to accept dataset based deterministic hurricanes. This also involved refactoring current hurricaneWindfields code into separate route. [INCORE1-690](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-690)

## [0.6.7] - 2020-06-30

### Changed
- Upgrade morphia version to 1.5.8 and upgrade mongo driver to 3.12.5 [INCORE1-670](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-670)
- incore.properties has been removed from docker and semantics docker added. [INCORE1-671](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-671)
- Jetty temp folder location has been changed. [INCORE1-673](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-673)
- Docker tagging in build process has been changed to distinguish prod and dev [INCORE1-678](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-678)

## [0.6.6] - 2020-06-15

### Changed
- Upgrade gradle to v6.5. Cleaned up gradle dependencies and changed geoserver related dependency links. [INCORE1-591](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-591)

### Fixed
- Resolved swagger path conflicts in delete hazard endpoints [INCORE1-659](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-659)


## [0.6.5] - 2020-05-29

### Added

- Enable cors for dfr3 services and a new property was added to list the allowed cross origin domains [INCORE1-585](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-585)
- Add DFR3 support for conditional and parametric fragilities [INCORE1-527](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-527)
- Initial framework for moving long running object creation tasks outside of the hazard service [INCORE1-514](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-514)
- DELETE endpoints for all hazards [INCORE1-560](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-560)
- Initial implementation of semantics service restricted to admin users [INCORE1-571](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-571)

### Changed
- Application properties are now initialized by system environment variables [INCORE1-587](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-587)

## [0.6.4] - 2020-03-27

### Added
- Text search endpoint to DFR3 Mappings [INCORE1-512](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-512)

### Changed
- Updated all controller's exceptions and its messages and added constructors to controllers that were missing one [INCORE-470](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-470)

### Fixed
- Tornado values response updated to be consistent with other hazards. Fixes the broken pyincore analyses for Tornado [INCORE1-555](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-555)

### Removed
- dataservice.url property. It's references are replaced with services.url [INCORE-482](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-482)

## [0.6.3] - 2020-03-02

### Added
- HTTP Exception class that can include a custom exception message in the Response [INCORE-480](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-480)
- Added UserInfoUtil class for validating user-info jsons and updated exceptions thrown in Dataset Controller. [INCORE1-476](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-476)
### Fixed
- Update docker's properties files with new property added for LDAP cache refresh, so the docker builds will succeed [INCORE1-478](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-478)

## [0.6.2] - 2020-02-14

### Added
- Added Abrahamson, Silva, Kamai 2014 and Campbell and Bozorgnia 2014 attenuation models [INCORE1-430](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-430)

### Fixed
- The user's LDAP groups in the cache will now get refreshed after a certain interval [INCORE1-431](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-431)

### Removed

## [0.6.1] - 2019-12-20
IN-CORE service release for IN-CORE v1.0

## [0.6.0] - 2019-10-22

## [0.5.0] - 2019-08-29

## [0.4.1] - 2019-07-18

## [0.4.0] - 2019-07-16

## [0.3.0] - 2019-05-15

## [0.2.0] - 2019-03-06


## [0.1.0] - 2018-01-24

Prototype for IN-CORE, a web application with a service oriented architecture based on legacy IN-CORE v1, which
was based on Ergo.

### Added

- Authentication service - uses LDAP
- Data service - MongoDB, file storage and Geoserver with initial implementation to fetch/store data
- Fragility service - MongoDB with initial implementation to return building fragilities
- Hazard service - MongoDB with initial implementation of scenario earthquake using Atkinson and Boore 1995 model
- Maestro service - MongoDB with initial implementation of building damage based on version 1 analysis
- Kong provides API Gateway pattern to service layer
- Initial web frontend providing login page, data browsing, fragility viewer, and analysis page to run building damage analysis
