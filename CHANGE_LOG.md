# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Added
- Added "postbin" command which allows piping payloads from Postb.in to a URL in the same manner as GitHub webhooks

## [0.1.1]
### Changed
- GH-1: Fixed malformed signature (security) header generated as part of webhook requests using a secret by the `push` command

## [0.1.0]
### Added
- Added "push" command which allows sending pre-defined event payloads to a URL in the same manner as GitHub webhooks