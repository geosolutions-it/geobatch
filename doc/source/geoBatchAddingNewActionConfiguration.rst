**Adding new Action**
====================================================================





* *Create the action's directory:* ::



	mkdir ${GEOBATCH}/src/actions/gb-${ACTION_NAME}

* *Create the new directory structure:* ::


	mkdir ${GEOBATCH}/src/actions/gb-${ACTION_NAME}/src/
	mkdir ${GEOBATCH}/src/actions/gb-${ACTION_NAME}/src/main
	mkdir ${GEOBATCH}/src/actions/gb-${ACTION_NAME}/src/main/java
	mkdir ${GEOBATCH}/src/actions/gb-${ACTION_NAME}/src/main/resources
	mkdir ${GEOBATCH}/src/actions/gb-${ACTION_NAME}/src/test
	mkdir ${GEOBATCH}/src/actions/gb-${ACTION_NAME}/src/test/java
	mkdir ${GEOBATCH}/src/actions/gb-${ACTION_NAME}/src/test/resources


* *To build a simple action you have to create a minimal set of classes into:* ::

 
	${GEOBATCH}/src/actions/gb-${ACTION_NAME}/src/main/java

#. Implement a Service: ::

	<EO extends EventObject, AC ActionConfiguration>

#. Define the Action class implementing an Action: ::

	<EXO extends EventObject>

#. Implement a Configuration: ::

	<EO extends EventObject>
   
This class will represent the configuration used by the Action defined above.
Here you have define all the variables needed by the Action.
*Notice:* each member variable will be binded to nodes defined into the Execution_flow execution Flow.

Actually GeoBatch make use of Xstream as class (de)serializer so we recommend to add: :: 

	-Pdao.xstream 

to mvn command string. 

Look ahead for the MVN section in this document.
To add the configuration class to the Xstream Catalog you also have to:

* Extend an AliasRegistrar: ::
	#This is used by the 
	it.geosolutions.geobatch.registry 
	#package to store the name of the Configuration class to perform the bind.

*Example:*

.. sourcecode:: java

	/**
	 * Register XStream aliases for the relevant services we ship in this class.
	 */
	public class ACTIONAliasRegistrar extends AliasRegistrar {

		public ACTIONAliasRegistrar(AliasRegistry registry) {
			LOGGER.info(getClass().getSimpleName() + ": registering alias.");
			registry
					.putAlias(
							"ACTIONActionConfiguration",
							ACTIONActionConfiguration.class);
		}
	}



* *Other details* http://geoserver.org/display/GEOS/Configuration+Persistence 

Since GeoBatch is a Spring based framework you have to add into: :: 

	${GEOBATCH}/src/actions/gb-${ACTION_NAME}/src/main/resources 

an XML file called *applicationContext.xml* which will be used to load beans on server startup: 

.. sourcecode:: xml

	<?xml version="1.0" encoding="UTF-8"?>
	<!DOCTYPE beans PUBLIC "-//SPRING//DTD BEAN//EN" "http://www.springframework.org/dtd/spring-beans.dtd">

	<beans default-init-method="init" default-destroy-method="dispose">

		<!-- Environment Initialization -->

		<bean id="ACTIONGeneratorService" class="it.geosolutions.geobatch.ACTION_PACKAGE.ACTIONGeneratorService">
		 <constructor-arg type="String"><value>ACTIONGeneratorService</value></constructor-arg><!--"id"-->
		 <constructor-arg type="String"><value>ACTIONGeneratorService</value></constructor-arg><!--"name"-->
		 <constructor-arg type="String"><value>ACTIONGeneratorService</value></constructor-arg><!--"description"-->
		</bean>
		
		<bean id="ACTION_IDAliasRegistrar" class="it.geosolutions.geobatch.ACTION_PACKAGE.ACTIONAliasRegistrar" lazy-init="false">
			<constructor-arg ref="aliasRegistry" />
		</bean>
	</beans>


*MAVEN:*
--------------------------------------------------------------- 
* GeoBatch uses maven2 to handle module dependencies, To add the above action be sure to apply following steps.


Create the maven configuration file: ::


	touch ${GEOBATCH}/src/actions/gb-${ACTION_NAME}/pom.xml


Edit the pom.xml file setting project name and dependencies: 

.. sourcecode:: xml


	<?xml version="1.0" encoding="UTF-8"?>
	<!-- =======================================================================    
			Maven Project Configuration File                                   	 
																				 
			GeoSolutions GeoBatch Project                                               	 
				http://geobatch.codehaus.org
																							 
			Version: $Id: pom.xml 329 2009-12-17 17:24:49Z dany111 $         	 
		======================================================================= -->
	  <project xmlns="http://maven.apache.org/POM/4.0.0"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
								http://maven.apache.org/maven-v4_0_0.xsd">
	  <modelVersion>4.0.0</modelVersion>

	  <parent>
		<groupId>it.geosolutions.geobatch</groupId>
		<artifactId>gb-actions</artifactId>
		<version>0.9-SNAPSHOT</version>
	  </parent>
	 

	  <!-- =========================================================== -->
	  <!-- 	Module Description                                  	-->
	  <!-- =========================================================== -->
	  <groupId>it.geosolutions</groupId>
	  <!-- TODO: Set the action artifactId name -->
	  <artifactId>gb-action-ACTION</artifactId>
	  <packaging>jar</packaging>
	  <!-- TODO: Set a package description -->
	  <name>GeoBatch action: ACTION executor</name>
	  <url>http://www.geo-solutions.it/maven_reports/gb/flowmanagers/</url>
	 
	  <scm>
		<connection>
		scm:svn:http://svn.geotools.org/geotools/trunk/gt/modules/flowmanagers/
		</connection>
		<url>http://svn.geotools.org/geotools/trunk/gt/modules/flowmanagers/</url>
	  </scm>
	 
	  <!-- TODO: Set a description -->
	  <description>
		GeoSolutions GeoBatch flow managers - ACTION executor.
	  </description>

	  <licenses>
		<license>
		<name>Lesser General Public License (LGPL)</name>
		<url>http://www.gnu.org/copyleft/lesser.txt</url>
		<distribution>repo</distribution>
		</license>
	  </licenses>

	  <!-- =========================================================== -->
	  <!-- 	Dependency Management                               	-->
	  <!-- =========================================================== -->
	  <dependencies>
		<dependency>
				<groupId>it.geosolutions.geobatch</groupId>
				<artifactId>gb-fs-catalog</artifactId>
			</dependency>
			<dependency>
				<groupId>it.geosolutions.geobatch</groupId>
				<artifactId>gb-alias-registry</artifactId>
			</dependency>
		
		<!-- TODO: Add dependencies here  -->

	  </dependencies>
	</project>



Edit the: ::

	${GEOBATCH}/src/actions/pom.xml 

to add the module action profile, flags and setting dependencies.

.. sourcecode:: xml

	<?xml version="1.0" encoding="UTF-8"?>
	<!--
		=======================================================================
			Maven Project Configuration File

			GeoSolutions GeoBatch Project
				http://geobatch.codehaus.org
		=======================================================================
	-->
	<project xmlns="http://maven.apache.org/POM/4.0.0"
			xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
			http://maven.apache.org/maven-v4_0_0.xsd">
		<modelVersion>4.0.0</modelVersion>

		...    

		<!-- =========================================================== -->
		<!-- 	Modules for the build in approximate dependency order   -->
		<!-- =========================================================== -->
		<profiles>

		...

			<profile>
				<id>PROFILE</id>
				<activation>
					<property>
						<name>all</name>
					</property>
				</activation>
				<modules>
					<module>gb-ACTION</module>
				</modules>
			</profile>

			<!-- You can configure a module to load this action as dependency -->

			<profile>
				<id>MASTER_PROFILE</id>
				<modules>
					<module>MODULE_1</module>
					...
					<module>MODULE_N</module>

					<module>gb-ACTION</module>
				</modules>
			</profile>

			...

		</profiles>

	</project>



Edit the main maven pom.xml file found in the GeoBatch project sources folder: ::

	${GEOBATCH}/src/pom.xml

.. sourcecode:: xml

	<?xml version="1.0" encoding="UTF-8"?>
	<!-- =======================================================================
			Maven Project Configuration File

			GeoSolutions GeoBatch Project
				http://geobatch.codehaus.org

			Version: $Id: pom.xml 63 2008-04-04 11:22:11Z alessio $
	======================================================================= -->
	<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0                             	http://maven.apache.org/maven-v4_0_0.xsd">

		...

		<!-- Profiles set on the command-line overwrite default properties. -->
		<profiles>

			...

			<!-- If you need it, add here the profile -->
			<profile>
				<id>ACTION</id>
				<modules>
					<module>gb-ACTION</module>
				</modules>
			</profile>

			...

		</profiles>


		<!-- =========================================================== -->
		<!-- 	Dependency Management                               	-->
		<!-- 	If a POM declares one of those dependencies, then it	-->
		<!-- 	will use the version specified here. Otherwise, those   -->
		<!-- 	dependencies are ignored.                           	-->
		<!-- =========================================================== -->
		<dependencyManagement>
			<dependencies>

				...
			 
				<!-- TODO: Add a dependency to the project -->

			<dependency>
		 <groupId>it.geosolutions</groupId>
		 <artifactId>gb-action-ACTION</artifactId>
					<version>${gb.version}</version>
			</dependency>
			 
				...

			</dependencies>
		</dependencyManagement>

	</project>


Now run the following mvn command from the GeoBatch source dir: ::

	cd ${GEOBATCH}/src/
	mvn eclipse:clean eclipse:eclipse -P${PROFILE}


Where: :: 

	${PROFILE}
 
can be a list of profiles containing the ACTION's one and/or a master profile which include the desired modules.

If you are working with multiple version of the platform, be sure to use the *eclipse.addVersionToProjectName* flag which add version informations to the package. ::

	mvn eclipse:clean eclipse:eclipse -P${PROFILE} -Declipse.addVersionToProjectName=true


*Notes:*

Remember to set accordingly the editor formatter and the template of the code following this http://docs.geoserver.org/stable/en/developer/eclipse-guide/index.html guide.
Short How-TO:
Window -> Preferences -> Java -> Code Style:
-> Code Templates: e importate dal codice di geotools /build/eclipse/codetemplates.xml
-> Formatter: e importare dal codice di geotools /build/eclipse/formatter.xml

* More details can be found here:

Building building GeoBatch
GeoBatch_UML UML
http://docs.geoserver.org/stable/en/developer/maven-guide/index.html#maven-guide GeoBatch Maven guide