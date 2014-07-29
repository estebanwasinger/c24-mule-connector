
Mule C24 Connector
=========================

A connector to allow C24 iO to be used to parse, validate, transform and marshal messages.

Installation
----------------------
Install the Mule C24 Connector into your Anypoint Studio via the standard Eclipse install mechanism. If you wish to install the connector built from your local sources, either use the UpdateSite.zip or create a new software repository which points at the target/update-site directory.

Note that so far it has proven necessary to install the generated jar to your local Maven cache too. Until such time as Mulesoft have investigated, please follow the instructions under Build below.


Setup
-----

The Mule C24 Connector can be used with Mule Maven projects. To get started, create a new Mule Maven project inside Anypoint Studio. You will need to add the following dependencies to the generated Maven POM:

		<dependency>
			<groupId>biz.c24.io</groupId>
			<artifactId>c24-io-api</artifactId>
			<version>4.6.1</version>
		</dependency>
		
To specify the version of iO that you wish to use. Don't forget to add the C24 nexus to the repositories if you haven't installed the jars locally:

		<repository>
			<id>c24-nexus</id>
			<name>C24 Nexus</name>
			<url>http://repo.c24io.net/nexus/content/groups/public</url>
		</repository>

You will also need to add your iO-generated jars containing your models and transformations. A portable way to do this is to create a Maven repository layout within your project and to add it as a repository, for example:

		<repository>
			<id>lib</id>
			<name>lib</name>
			<releases>
				<enabled>true</enabled>
				<checksumPolicy>ignore</checksumPolicy>
			</releases>
			<snapshots>
				<enabled>false</enabled>
			</snapshots>
			<url>file://${project.basedir}/lib</url>
		</repository>

Install your jars below this directory as per standard Maven practice, then you can declare your dependencies in the same way as for any other jar, e.g.:

		<dependency>
			<groupId>biz.c24.io</groupId>
			<artifactId>gettingstarted</artifactId>
			<version>1.0</version>
		</dependency>
		

Usage
-----

Create a basic configuration component by going to the Global Elements tab and Creating a Connector Configuration -> C24 Connector element.

If you are using licensed components and do not have your IO_HOME environment variable pointing to a directory containing your C24 iO licence, you can specify its location here. For paths starting with a '/', iO will check both the root of your filesystem and the classpath used by the ClassLoader which loaded the C24 classes.

Back in your Message Flow you can now add a C24 Connector, configured by the global element we just created.

Currently the connector exposes a single operation 'transform' whose configuration is:

* Transform - the C24 iO-generated Transform class to use. Only 1:1 transformations are currently supported.
* Object reference - where to read the message from. Defaults to incoming message payload.
* Encoding - allows overriding the default text encoding to use when parsing the source message.
* Validate Input - should the message be validated post-parsing. Throws an exception if a validation failure is encountered.
* Validate Output - should the output of the transformation be validated. Throws an exception if a validation failure is encountered.

If the object supplied to the Connector is not derived from iO's ComplexDataObject and is either an InputStream, Reader or a String then the expected input to the Transform is inspected and this used to drive the parsing of the object (using that type's default format). If an error is encountered during parsing then an exception will be thrown.

Post-transformation the resulting object will be marshalled into its default wire format.


DataSense
---------
The C24 Connector uses Reflections to find all available classes which extend the C24 iO Transform class, and thus are legal values to use in the Transform field. As well as selecting from the dropdown, the full path to the Transform class can also be manually entered.

Reflections has a Maven task which precomputes the metadata it needs. The C24 Connector will look for the presence of these files on start-up - if none are found it will resort to dynamically querying the ClassLoaders. For large classpaths/slow machines this could cause a delay on first opening the C24 Connector or refreshing the DataSense data.


Building
========

Building is via Maven:

	mvn -Dmaven.test.skip=true install
	
Technically only a package should be required however there currently appears to be an issue in AnypointStudio which requires the jar to also be installed in your local Maven repository.

Awaiting details from Mulesoft on how to construct tests - there appears to be a test-time resolution problem with XSDs as the test flow works perfectly in Anypoint Studio.
