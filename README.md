
Mule C24 Connector
=========================

FILL IN DESCRIPTION

Installation and Usage
----------------------

For information about usage and installation you can check our documentation at http://mulesoft.github.com/mule-c24-connector.

Install C24 Dependencies
-------------------------

mvn install:install-file -Dfile=/Users/johndemic/AnypointStudio/workspace/c24-sample-mule-transformer/lib/c24-io-api-4.1.1.jar -DgroupId=biz.c24  -DartifactId=c24-io-api -Dversion=4.1.1 -Dpackaging=jar

mvn install:install-file -Dfile=/Users/johndemic/AnypointStudio/workspace/c24-sample-mule-transformer/lib/fix44.jar -DgroupId=fix44 -DartifactId=fix44 -Dversion=1.0 -Dpackaging=jar

 mvn install:install-file -Dfile=/Users/johndemic/AnypointStudio/workspace/c24-sample-mule-transformer/lib/swift2012-2012.jar -DgroupId=swift2012 -DartifactId=swift2012 -Dversion=2012 -Dpackaging=jar

Reporting Issues
----------------

We use GitHub:Issues for tracking issues with this connector. You can report new issues at this link https://github.com/mulesoft/mule-c24-connector/issues