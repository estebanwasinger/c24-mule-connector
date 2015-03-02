
Mule C24-iO Connector
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
			<version>4.6.9</version>
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

Please see doc/usermanual_md.



Building
--------

Building is via Maven:

	mvn install
	
Technically only a package should be required however there currently appears to be an issue in AnypointStudio which requires the jar to also be installed in your local Maven repository.

