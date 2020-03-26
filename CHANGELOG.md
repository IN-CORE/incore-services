# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [0.6.4] - 2020-03-27

### Added
- Text search endpoint to DFR3 Mappings [INCORE1-512](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-512)

### Updated
- Updated all controller's exceptions and its messages and added constructors to controllers that were missing one [INCORE-470](https://opensource.ncsa.illinois.edu/jira/browse/INCORE1-470)

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
