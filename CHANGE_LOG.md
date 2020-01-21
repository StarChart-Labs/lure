# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Changed
- Updated dependencies to latest bugfix release (gson, okhttp, commons-codec, slf4j, alloy)

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