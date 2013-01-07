.. |GB| replace:: **GeoBatch**
.. |GS| replace:: **GeoServer**
.. |GH| replace:: *GitHub*

.. _`dvlpAction`:

Develop an action
=================


Write the action classes and naming convenctions
------------------------------------------------

A module that implements one or more actions related each other, must be called **gb-action-** plus a **modulename** so ``gb-action-modulename``.

An action is composed of 4 mandatory classes that should follow this naming convenction: Given an Action Name **ExampleAction** the four class must be called 

#.	ExampleActionAliasRegistar.java 

#.	ExampleActionGeneratorService.java

#.	ExampleActionConfiguration.java

#.	ExampleActionAction.java

The 4 classes (and any other utility class developed for the action) must be reside under a package called *it.geosolutions.geosolutions.modulename.exampleaction* 

In the next paragraphs is shown how the four classes must be implemented. Are also provided some code templates, where are omissed the imports, the package declaration and the license.

The reader can use these templates replacing the placeholder #ACTION_NAME# with the custom action name and implementing where the comments starts with TODO .



Configuration
-------------

The class #ACTION_NAME#Configuration.java is the bean where the action configuration, extracted from the whole flow configuration, will be unmarshalled.

a standard template is provided here:: 

	public class #ACTION_NAME#Configuration extends ActionConfiguration implements Configuration {
		
		// TODO ADD YOUR CONFIGURATION MEMBERS
		
		public #ACTION_NAME#Configuration(String id, String name, String description) {
			super(id, name, description);
			// TODO INITIALIZE CONFIGURATION MEMBERS
		}
		
		@Override
		public #ACTION_NAME#Configuration clone(){
			final #ACTION_NAME#Configuration ret=(#ACTION_NAME#Configuration)super.clone();
			
			// TODO CLONE YOUR CONFIGURATION MEMBERS
		
			ret.setWorkingDirectory(this.getWorkingDirectory());
			ret.setServiceID(this.getServiceID());
			ret.setListenerConfigurations(ret.getListenerConfigurations());
			return ret;
		}
	}

There are 3 mandatory task to implement in this template, but they are very simple: Declare the configuration members, inizialize them in the constructor and clone them.



AliasRegistar
-------------

The class #ACTION_NAME#AliasRegistrar.java is responsible for settings the XStream aliases in order write a human readable Flow configuration.

a template is provided here::

	public class #ACTION_NAME#AliasRegistrar extends AliasRegistrar {

		public #ACTION_NAME#AliasRegistrar(AliasRegistry registry) {
			
			LOGGER.info(getClass().getSimpleName() + ": registering alias.");
			
			// Setting up the Alias for the root of the Configuration
			registry.putAlias("#ACTION_NAME#Configuration", #ACTION_NAME#Configuration.class);
			
			// TODO Add here other Aliases...
		}
	}

Note that without settings the aliases the flow configuration tags must be contains the full qualified name for each class used.

For a deeper documentation about XStream aliases see the official documentations and `this tutorial <http://xstream.codehaus.org/alias-tutorial.html>`_.



Action
------

The class #ACTION_NAME#Action.java holds the business logic of the action. The implementation of the execute method is the main task for a |GB| action developer.

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
							LOGGER.trace("#ACTION_NAME#Action.execute(): working on incoming event: "+ev.getSource());
						}
						// TODO: DO SOMETHING WITH THE INCOMING EVENT, ADD THE ACTION IMPLEMENTATION
						
						// add the event to the return
						ret.add(ev);
						
					} else {
						if (LOGGER.isErrorEnabled()) {
							LOGGER.error("#ACTION_NAME#Action.execute(): Encountered a NULL event: SKIPPING...");
						}
						continue;
					}
				} catch (Exception ioe) {
					final String message = "#ACTION_NAME#Action.execute(): Unable to produce the output: "
							+ ioe.getLocalizedMessage();
					if (LOGGER.isErrorEnabled())
						LOGGER.error(message);
					throw new ActionException(this, message);
				}
			}
			return ret;
		}   
	}

An Action must extends the class ``BaseAction<XEO extends EventObject>``. Often is better use directly a |GB| event (for example FileSystemEvent) as type parameter, so some cast operation could be avoided.

Another aspect is the action fault tollerance. Sometimes, if an error occurs during an action execution, we want to terminate the whole flow execution and other times we want that the error could be skipped and continue to process the next event.
In order to handle this situation there is a property called failIgnored in the class *ActionConfiguration* (so every configurations inherit it). The meaning of this flag is to specify if errors are tollerated during an action executions.
In order to handle in a standard way this flag the class *ActionExceptionHandler.java* (module gb-tools package *it.geosolutions.tool.errorhandling*) provide the static method *handleError(...)* so, calling this, the error could be handled depending on the failIgnore flag value.

GeneratorService
----------------

The Class #ACTION_NAME#GeneratorService.java is responsible for the runtime creation of the Action from its configuration.

Must implement the methods createAction() and canCreateAction().

a standard template is provided here::

	public class #ACTION_NAME#GeneratorService extends BaseService implements
			ActionService<EventObject, #ACTION_NAME#Configuration> {

		public #ACTION_NAME#GeneratorService(String id, String name, String description) {
			super(id, name, description);
		}

		private final static Logger LOGGER = LoggerFactory.getLogger(#ACTION_NAME#GeneratorService.class);

		public #ACTION_NAME#Action createAction(#ACTION_NAME#Configuration configuration) {
			try {
				return new #ACTION_NAME#Action(configuration);
			} catch (Exception e) {
				if (LOGGER.isInfoEnabled())
					LOGGER.info(e.getLocalizedMessage(), e);
				return null;
			}
		}

		public boolean canCreateAction(#ACTION_NAME#Configuration configuration) {
			try {
				// absolutize working dir
				String wd = Path.getAbsolutePath(configuration.getWorkingDirectory());
				if (wd != null) {
					configuration.setWorkingDirectory(wd);
					return true;
				} else {
					if (LOGGER.isWarnEnabled())
						LOGGER.warn("#ACTION_NAME#GeneratorService::canCreateAction(): "
								+ "unable to create action, it's not possible to get an absolute working dir.");
				}
			} catch (Throwable e) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error(e.getLocalizedMessage(), e);
			}
			return false;
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

**TO BE FINISH**

|GB| provide a usefull tool for the automatic creation of the the templates shown before.
Into the root dir of |GB| sources directory there is the script ``build_archetype.sh`` and a directory called ``.build``.
The script generates, from the templates holded in ``.build`` a maven directory tree with all 4 classes described.

For compile the project and generate the war run the command::

	$ ~work/code/geobatch/src/application# mvn clean install
	
and the war will be copyed under the local maven repo.
