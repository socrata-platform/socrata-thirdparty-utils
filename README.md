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

Releasing
=========

Run `sbt-release` and set an appropriate version.
