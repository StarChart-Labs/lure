# Lure

[![Travis CI](https://img.shields.io/travis/com/StarChart-Labs/lure.svg?branch=master)](https://travis-ci.com/StarChart-Labs/lure) [![Changelog validated by Chronicler](https://chronicler.starchartlabs.org/images/changelog-chronicler-success.png)](https://chronicler.starchartlabs.org/)

Command line utility for generating mock GitHub webhook test messages

## Contributing

Information for how to contribute can be found in [the contribution guidelines](./docs/CONTRIBUTING.md)

## Legal

Lure is distributed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0). The only requirement for use is inclusion of the following line within your NOTICES file:

```
StarChart-Labs Lure Command Line Utility
Copyright 2017-2019 StarChart Labs Authors.

This product includes software developed at
StarChart Labs (http://www.starchartlabs.org/).
```

The requirement for a copy of the license being included in distributions is fulfilled by a copy of the [LICENSE](./LICENSE) file being included in constructed JAR archives


## Reporting Vulnerabilities

If you discover a security vulnerability, contact the development team by e-mail at `vulnerabilities@starchartlabs.org`

## Use

*Lure is still in initial development*

To use, download the latest release from the [releases page](https://github.com/StarChart-Labs/lure/releases). From the download location, application may be run via `java -jar lure-(version)-capsule.jar`, followed by the desired command.

Lure is be a command-line interface for sending webhooks payloads on a local system. With the current support, it is recommended to use something like Request Bin to capture real webhook payloads, and then utilize lure to send them to your environment for testing (in situations where direct calls cannot be made)
The current in-development code base supports:

- `push`: Pushes a provided payload to an indicated web URL, mimicing the GitHub webook call pattern
  - `--target-url`: Specifies the server URL to POST the event to. Required
  - `--event-name`: Specifies the GitHub event name to send as. Required
  - `--secret`: Specifies the webhook secret to secure the event post with
  - `--content`: Specifies the file containing the event content to send. Required
  
Example: `java -jar lure-(version)-capsule.jar push --target-url http://localhost/webhook --secret totallySecure --event-name event --content ./content.json`

- `postbin`: Pipes a payloads from postb.in to an indicated web URL, mimicing the GitHub webook call pattern
  - `--target-url`: Specifies the server URL to POST the event to. Required
  - `--bin-id`: Specifies the Postbin bin ID to poll. Required
  - `--poll-frequency`: Specifies the frequency in seconds to poll Postbin for new requests at, must be greater than 0. Defaults to 10 seconds
  - `--postbin-root-url`: Specifies the root URL of Postbin to use. Defaults to https://postb.in/api
  
Example: `java -jar lure-(version)-capsule.jar postbin --target-url http://localhost/webhook --bin-id abcdefg`

 
