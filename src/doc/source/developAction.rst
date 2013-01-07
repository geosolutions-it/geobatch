.. |GB| replace:: **GeoBatch**
.. |GS| replace:: **GeoServer**
.. |GH| replace:: *GitHub*

.. _`dvlpAction`:

Develop an action
=================


Class naming conventions
------------------------------------------------

It is recommended to have one action per maven module, unless the actions are strictly related one another.

A maven module implementing one (or more) actions should be called ``gb-action-modulename``, where  **modulename** is a name specific to the action.

An action is composed of 4 mandatory classes that should follow this naming convention: Given an Action named **ExampleAction** the four classes shall be called 

* **ExampleActionAction.java** -   the Actions business logic.
* **ExampleActionConfiguration.java** - any configuration you need in the action.
* **ExampleActionGeneratorService.java** - a Service that creates an ExampleActionAction using a ExampleActionConfiguration
* **ExampleActionAliasRegistrar.java** - a bean used to register the GeneratorService into the Spring context.


The 4 classes (and any other utility class developed for the action) must be placed under a package called *it.geosolutions.geosolutions.modulename.exampleaction* 

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
