# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [0.9.1] - 2020-12-18

### Added
- Included space information in the GET request jsons of datasets, hazards and DFR3 objects.  [INCORE1-889](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-889)

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
