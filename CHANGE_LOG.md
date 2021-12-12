# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]

## [0.3.1]
### Changed
- Updated com.google.code.gson:gson from 2.8.6 to 2.8.9
- Updated com.squareup.okhttp3:mockwebserver from 3.14.1 to 4.9.3
- Updated com.squareup.okhttp3:okhttp from 3.14.1 to 4.9.3
- Updated commons-codec:commons-codec from 1.13 to 1.15
- Updated info.picocli:picocli from 4.1.4 to 4.6.2
- Updated org.slf4j:slf4j-api from 1.7.29 to 1.7.32
- Updated org.slf4j:slf4j-simple from 1.7.29 to 1.7.32
- Updated org.starchartlabs.alloy:alloy-core from 0.5.0 to 1.0.2
- UPdated org.testng:testng from 6.14.3 to 7.4.0

## [0.3.0]
### Changed
- Updated dependencies to latest bugfix release (gson, okhttp, commons-codec, slf4j, alloy)
- (GH-16) Switched from args4j CLI parsing to picocli, as args4j no longer seems actively maintained

## [0.2.1]
### Changed
- (GH-9) Fixed a bug where the request body sent did not match the one received by postbin, which was invalidating security checks based on a hash of the payload and some shared secret

## [0.2.0]
### Added
- Added "postbin" command which allows piping payloads from Postb.in to a URL in the same manner as GitHub webhooks
- Added support for automatic creation of PostBin if bin ID is omitted

### Changed
- GH-4: Fixed incorrect usage documentation of commands

## [0.1.1]
### Changed
- GH-1: Fixed malformed signature (security) header generated as part of webhook requests using a secret by the `push` command

## [0.1.0]
### Added
- Added "push" command which allows sending pre-defined event payloads to a URL in the same manner as GitHub webhooks