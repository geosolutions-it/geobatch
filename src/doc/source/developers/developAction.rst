.. |GB| replace:: **GeoBatch**
.. |GS| replace:: **GeoServer**
.. |GH| replace:: *GitHub*

.. _`dvlpAction`:

Develop an action
====================

Starting from |GB| **1.4** an action is much simpler to develop than the previous versions. The need for developing boilerplate code or create spring context files has been removed.

Just create a POJO where unmarshall the configuration, create the action class extending *BaseAction.java*, annotate these two classes with some GeoBatch annotations and use the action inside a flow configuration.

In the next sections of this chapter will be described all the **naming convenction**, the **maven poms** to create or change, how to **develop unit tests** and of course which **annotations must be used** to create an action.

Naming conventions and other general guidelines
----------------------------------------------------

A best practice is to aggregate actions that are strictly related each other in a single maven module and create a custom maven build profile for that.
If different actions developed doesn't share any relation it is preferible use different maven modules for each action.

A maven module implementing one (or more) actions should be called ``gb-action-modulename``, where  **modulename** is a name specific to the action.

An action is composed of two mandatory classes that should follow this naming convention: Given an Action named **ExampleAction** the two classes shall be called 

* **ExampleActionAction.java** -   the Actions business logic.
* **ExampleActionConfiguration.java** - any configuration you need in the action.

These two classes (and any other utility class developed for the action) should be placed under a package called *it.geosolutions.geosolutions.modulename.exampleaction* 

Next paragraphs will show how this two classes should be implemented. Some code templates will be provided.

You can use these templates replacing the placeholder ``#ACTION_NAME#`` with the custom action name and implementing where the comments starts with TODO.
You'll need to fill in the imports, the package declaration and the license as well.

**Notes:**

Remember to set accordingly the editor formatter and the template of the code following this http://docs.geoserver.org/stable/en/developer/eclipse-guide/index.html guide.

**Quick How-TO**

*Window* -> *Preferences* -> *Java* -> *Code Style* :

-> *Code Templates* : import from geotools source code /build/eclipse/codetemplates.xml

-> *Formatter* : import from geotools source code /build/eclipse/formatter.xml

Develop an action: The Annotations
--------------------------------------------

As told before the *action configuration class* and the *action implementation class* must be annotated to be recognized by |GB| as an action.

Inside the module *gb-core-model* in the package *it.geosolutions.geobatch.annotations* can be found the annotation classes that must be used.

Action annotation
,,,,,,,,,,,,,,,,,,,,
 
The annotation **Action** indicates to |GB| that the annotated Class is an Action and will be execute running its method **execute()**. 

The usage of this annotation is **Mandatory** and must be used only if a class extends **BaseAction**.

The parameters taken by this annotation are:

* **configurationClass** (mandatory): Indicates the class that act as Action configuration. Accepts instances of **Class<? extends ActionConfiguration>** . This parameter is Mandatory because of is the only way to bind an action to its configuration.
* **configurationAlias** (optional): Indicates the alias to use for the configuration. Accepts instances of **String**. Remember that the action configuration is extracted from the flow configuration and it is unmarshalled into the related ActionConfiguration Bean using a java2XML binder (the |GB| default one is `xstream <http://xstream.codehaus.org/>`_ ) in order to avoid to write configuration using the full qualified name of the class you can specified an alias to keep the configuration more human-readable. This value is **Optional**, the Configuration Class name will be used by default.
* **aliases** (optional): Similar to the previous parameter but it is used for the configurations fields used. Accepts an array of Class. Each class found in this array will be registered with the ClassName instead of its FullQualifiedName.
* **implicitCollections** (optional): configuration attributes listed in this array will be marshalled inside a Collection without the need for be wrapped between tags in the XML configuration.

see this `xstream tutorial <http://xstream.codehaus.org/alias-tutorial.html>`_ for a better explanation of the **alias** and **implicitCollections** concepts but remeber that this is a |GB| astractions that allows to use this annotation and this system for simplify the XML flow configuration with all different java2XML binder.

CheckConfiguration annotation
,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

Indicates that the annotated Method contains the logic to check action configuration before action can be executed. 
This annotation should be used only in class annotated as {@link Action} and extending {@link BaseAction} and with a Boolean return type.
If the method returns **False** means that the system and configuration prerequisites to run the action aren't present so the action will not be executed.

Note that the usage of this annotation is optional.

Develop an action: write the source code
--------------------------------------------

After the introduction of the convenctions to follow and the annotations that must be used below are showed the templates for the **ActionConfiguration** and **Action** classes.

Develop the Configuration Class
,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

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

Action
,,,,,,,,,,,

The class #ACTION_NAME#Action.java holds the business logic of the action. The implementation of the ``execute()`` method is the main task for a |GB| action developer.

The template below shows a typical structure of the execute method that iterate on all the events intercepted.

The whole loop body is wrapped inside a ``try`` block so any Exception that isn't explicitally handled will be caught by the corresponding ``catch`` block and an ActionException will be thrown.

The template::

   @Action(configurationClass=#ACTION_NAME#Configuration.class)
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