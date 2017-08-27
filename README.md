# Introduction
A java web crawler based on vertx.

## Packaging
Please run:

    mvn dependency:copy-dependencies -DoutputDirectory=target/lib
    mvn package

## Features
1.0.0 Version
* Job Queue for request
* Time interval to avoid influence the target site

But with callback hell.

1.0.1 Version
* Reduce memory use space

1.0.2 Version
* Avoid callback hell by moduling
* Ensure all jobs be consumed

1.0.3 Version
* Fix Packaging Bugs
