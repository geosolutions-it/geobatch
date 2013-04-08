.. |GB| replace:: **GeoBatch**
.. |GS| replace:: **GeoServer**
.. |GH| replace:: *GitHub*

.. _`dvlpAction`:

Develop an action
=================


Class naming conventions
------------------------------------------------

It is recommended to have one action per maven module, unless the actions are strictly related to one another.

A maven module implementing one (or more) actions should be called ``gb-action-modulename``, where  **modulename** is a name specific to the action.

An action is composed of 4 mandatory classes that should follow this naming convention: Given an Action named **ExampleAction** the four classes shall be called 

* **ExampleActionAction.java** -   the Actions business logic.
* **ExampleActionConfiguration.java** - any configuration you need in the action.
* **ExampleActionGeneratorService.java** - a Service that creates an ExampleActionAction using a ExampleActionConfiguration
* **ExampleActionAliasRegistrar.java** - a bean used to register the GeneratorService into the Spring context.


The 4 classes (and any other utility class developed for the action) should be placed under a package called *it.geosolutions.geosolutions.modulename.exampleaction* 

Next paragraphs will show how the four classes are to be implemented. Some code templates are also provided.

You can use these templates replacing the placeholder ``#ACTION_NAME#`` with the custom action name and implementing where the comments starts with TODO.
You'll need to fill in the imports, the package declaration and the license as well.



Configuration
-------------

The class #ACTION_NAME#Configuration.java is the bean where the action configuration, extracted from the whole flow configuration, will be unmarshalled.

A standard template is provided here:: 

	public class #ACTION_NAME#Configuration
	                  extends ActionConfiguration 
	                  implements Configuration {
		
		// TODO: add your conf members 
		
		public #ACTION_NAME#Configuration(String id, String name, String description) {
			super(id, name, description);
			
			// TODO: your initialization
		}
		
		@Override
		public #ACTION_NAME#Configuration clone(){
			final #ACTION_NAME#Configuration ret=(#ACTION_NAME#Configuration)super.clone();
			
			// TODO: deep copy your members if needed
		
			return ret;
		}
	}

You have to fill in the 3 *todo* if needed.



AliasRegistrar
--------------

The class #ACTION_NAME#AliasRegistrar.java is responsible for settings the XStream aliases in order to write a human readable Flow configuration.

A template is provided here::

	public class #ACTION_NAME#AliasRegistrar extends AliasRegistrar {

		public #ACTION_NAME#AliasRegistrar(AliasRegistry registry) {
			
			LOGGER.info(getClass().getSimpleName() + ": registering alias.");
			
			// Setting up the Alias for the root of the Configuration
			registry.putAlias("#ACTION_NAME#Configuration", #ACTION_NAME#Configuration.class);
			
			// TODO Add here other Aliases...
		}
	}


Note that ``registry.putAlias(aliasName, aliasedClass)`` calls the `XStream.alias  <http://xstream.codehaus.org/javadoc/com/thoughtworks/xstream/XStream.html#alias(java.lang.String,%20java.lang.Class)>`_ method. 
We're using the AliasRegistrar class in order to decouple the action code from the XStream libs.

For a deeper documentation about XStream aliases see the official documentations and `this tutorial <http://xstream.codehaus.org/alias-tutorial.html>`_.


Action
------

The class #ACTION_NAME#Action.java holds the business logic of the action. The implementation of the ``execute()`` method is the main task for a |GB| action developer.

The template below shows a typical structure of the execute method that iterate on all the events intercepted.

The whole loop body is wrapped inside a ``try`` block so any Exception that isn't explicitally handled will be caught by the corresponding ``catch`` block and an ActionException will be thrown.

The template::

   public class #ACTION_NAME#Action extends BaseAction<EventObject> {
      private final static Logger LOGGER = LoggerFactory.getLogger(#ACTION_NAME#Action.class);

      // Action configuration
      private final #ACTION_NAME#Configuration conf;

      public #ACTION_NAME#Action(#ACTION_NAME#Configuration configuration) {
         super(configuration);
         conf = configuration;
         //TODO initialize your members here
      }

      public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {

         // return object
         final Queue<EventObject> ret=new LinkedList<EventObject>();

         while (events.size() > 0) {
            final EventObject ev;
            try {
               if ((ev = events.remove()) != null) {
                  if (LOGGER.isTraceEnabled()) {
                     LOGGER.trace("Working on incoming event: "+ev.getSource());
                  }
                  
                  // TODO: DO SOMETHING WITH THE INCOMING EVENT, 
                  //       ADD THE ACTION IMPLEMENTATION

                  // add the event to the return
                  ret.add(ev);

               } else {
                  if (LOGGER.isErrorEnabled()) {
                     LOGGER.error("Encountered a NULL event: SKIPPING...");
                  }
                  continue;
               }
            } catch (Exception ioe) {
               final String message = "Unable to produce the output: " + ioe.getLocalizedMessage();
               if (LOGGER.isErrorEnabled())
                  LOGGER.error(message);
                  
               throw new ActionException(this, message);
            }
         }
         return ret;
      }   
   }

An Action must extends the class ``BaseAction<XEO extends EventObject>``. Often it is better use directly a |GB| event (for example FileSystemEvent) as type parameter, so some cast operation could be avoided.

Another aspect is the action fault tolerance. Sometimes, if an error occurs during an action execution, we want to terminate the whole flow execution; some other times we want that the error could be skipped and continue to process the next event.
In order to handle this situation there is a property called ``failIgnored`` in the class *ActionConfiguration* (so every configurations inherit it). The meaning of this flag is to specify whether errors are tolerated during an action executions.
In order to handle in a standard way this flag the class *ActionExceptionHandler.java* (module gb-tools package *it.geosolutions.tool.errorhandling*) provide the static method *handleError(...)* so, calling this, the error could be handled depending on the failIgnore flag value.

GeneratorService
----------------

The Class #ACTION_NAME#GeneratorService.java is responsible for the runtime creation of the Action from its configuration.

Must implement the methods createAction() and canCreateAction().

a standard template is provided here::

   public class #ACTION_NAME#GeneratorService 
            extends BaseService 
            implements ActionService<EventObject, #ACTION_NAME#Configuration> {

      private final static Logger LOGGER = LoggerFactory.getLogger(#ACTION_NAME#GeneratorService.class);

            
      public #ACTION_NAME#GeneratorService(String id, String name, String description) {
         super(id, name, description);
      }

      public #ACTION_NAME#Action createAction(#ACTION_NAME#Configuration configuration) {
         try {
            return new #ACTION_NAME#Action(configuration);
         } catch (Exception e) {
            if (LOGGER.isInfoEnabled())
               LOGGER.info(e.getLocalizedMessage(), e);
            return null; // ?!? should throw
         }
      }

      public boolean canCreateAction(#ACTION_NAME#Configuration configuration) {
         if ( the input configuration is acceptable ) 
            return true;
         else {
            if (LOGGER.isWarnEnabled())
                  LOGGER.warn("Unable to create action: bad configuration (ADD DETAILS IF NEEDED)");
         
            return false;
         }
      }
   }

Spring
------

Since GeoBatch is a Spring based framework you have to add into::

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
	
Maven
-----

GeoBatch uses maven2 to handle module dependencies, To add the above action be sure to apply following steps.

Create the maven configuration file::

	touch ${GEOBATCH}/src/actions/gb-${ACTION_NAME}/pom.xml

Edit the pom.xml file setting project name and dependencies: 

.. sourcecode:: xml

	<?xml version="1.0" encoding="UTF-8"?>
	<!-- =======================================================================    
		Maven Project Configuration File                                   			GeoSolutions GeoBatch Project                                               
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

Edit the::

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


Now run the following mvn command from the GeoBatch source dir::

	cd ${GEOBATCH}/src/
	mvn eclipse:clean eclipse:eclipse -P${PROFILE}

Where:: 

	${PROFILE}
 
can be a list of profiles containing the ACTION's one and/or a master profile which include the desired modules.

If you are working with multiple version of the platform, be sure to use the *eclipse.addVersionToProjectName* flag which add version informations to the package. ::

	mvn eclipse:clean eclipse:eclipse -P${PROFILE} -Declipse.addVersionToProjectName=true
	
Temp directories usage
-----------------------

* ``DataDirHandler`` will handle the basic dir configurations, both the ``GEOBATCH_CONFIG_DIR`` and the ``GEOBATCH_TEMP_DIR``. It will take care of setting the default base temp dir if it's not defined. It provides methods to retrieve these two base directories.

* ``FileBasedFlowManager`` handles the optional override configurations at flow level. It provides the methods ``getFlowTempDir()`` and ``getFlowConfigDir()``, that will return the absolute current flow dirs, resolved with the optional override when needed.

* ``FileBasedEventConsumer`` handles the conf and temp dir for the Actions. It will resolve the optional overrideConfigDir at ActionConfiguration level, and will inject into the Actions their proper configDir and tempDir.

To get the *base* configuration dirs, use:

.. sourcecode:: java

   DataDirHandler ddh;
   [...]
   ddh.getBaseConfigDirectory();
   ddh.getBaseTempDirectory();

To get the Action's specific dirs, use:

.. sourcecode:: java

   BaseAction<EventObject> action;
   [...]
   action.getConfigDir();
   action.getTempDir();
   

Unit Testing
------------

After writing all the classes needed for the |GB| action they will be tested.
A way for test the action is, of course, write a flow configuration and run |GB|. 
A more quick way to run and test an action, usefull using testing framework like jUnit, is to simulate what the |GB| do at runtime.

So given an action called *ExampleAction* and a configuration called *ExampleConfiguration* below is shown how to run the Action simulating the event of a file added.

instantiate and setup the configuration::

	ExampleConfiguration config = new ExampleConfiguration("exampleID","exampleName","exampleConfiguration");
	config.setExampleProperty1("aValue");
	config.setExampleProperty2("anotherValue");
	
create the file event, this file represent the event that starts the action::
	
	File fileEvent = new File("/path/of/some/file")

instantiate the action providing the configuration created before::

	ExampleAction action = new ExampleAction(config);
	action.setTempDir(new File("/path/of/some/dir"));

instantiate the EventQueue and add an event::

	Queue<EventObject> queue = new LinkedList<EventObject>();
	queue.add(new FileSystemEvent(fileEvent,FileSystemEventType.FILE_ADDED));

run the action and check if an ActionException occurs::

	try {
		action.execute(queue);
	} catch (ActionException e) {
		fail(e.getLocalizedMessage());
	}
	
Using jUnit 4, copy all previous instructions into this method::

	@Test
	public void createUpdate() throws Exception {
		// implementation
	}

So with this test will be easy debug and check the outcome of an action without configure the whole flow.

For an explanation of how to write a flow configuration see the :ref:`flwCnfg` .


build_archetype.sh and war creation
-----------------------------------

**TO BE COMPLETED**

|GB| provide a useful tool for the automatic creation of the the templates shown before.
Into the root dir of |GB| sources directory there is the script ``build_archetype.sh`` and a directory called ``.build``.
The script generates, from the templates hold in ``.build`` a maven directory tree with all the 4 classes described.

To compile the project and generate the .war run the command::

   $ ~work/code/geobatch/src/application# mvn clean install
	
and the war will be copied under the local maven repo.


*Notes:*

Remember to set accordingly the editor formatter and the template of the code following this http://docs.geoserver.org/stable/en/developer/eclipse-guide/index.html guide.
Short How-TO:
Window -> Preferences -> Java -> Code Style:
-> Code Templates: e importate dal codice di geotools /build/eclipse/codetemplates.xml
-> Formatter: e importare dal codice di geotools /build/eclipse/formatter.xml
