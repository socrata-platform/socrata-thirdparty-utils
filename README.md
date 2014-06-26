socrata-thirdparty-utils
========================

Wrappers and helpers for third-party libraries

opencsv
-------

`CSVIterator`, which produces an `Iterator` from a `CSVReader`.

typesafe-config
---------------

`Propertizer`, which converts a (part of) a `Config` hierarchy into a set of
`Properties` for use with systems that want to be configured that way.

async-http-client
-----------------

`FAsyncHandler`, which gathers the status and headers before creating a consumer
for the response body.

curator
-------

* `CuratorConfig`, a common config class for Curator services
* `CuratorInitializer`, common initialization stuff for Curator service discovery
* `CuratorServiceBase`, a Trait for curator-based service clients
* `ProviderCache`, a cache for Curator service providers

Releasing
=========

Run `sbt-release` and set an appropriate version.
