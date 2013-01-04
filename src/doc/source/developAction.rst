.. |GB| replace:: **GeoBatch**
.. |GS| replace:: **GeoServer**
.. |GH| replace:: *GitHub*

Develop an Action HOW-TO
========================

This tutorial explain all step needed in order to develop, test and run a new |GB|'s action from scratch. 

The first chapter is focused on the needed tools and steps necessary for setup the development environment.

The second chapter assumes that the project is already well configured in the IDE and is focused on the programming stuff needed.


1. Setup the development environment
------------------------------------


Tools needed
````````````

#.	**git**
	
	The official |GB| repository `is hosted on |GH| <https://github.com/geosolutions-it/geobatch>`_ so a knowledge of the main *git* concepts and functionalities (basic workflow, branching and tagging) are needed in order to work with it.
	If you are new using *git* as scm have a look to `this online documentation <http://git-scm.com/doc>`_ .
	Have a look to `this guide <http://www.sbf5.com/~cduan/technical/git/>`_ for an overview of the main git concepts

	
#.	**Maven**
	
	|GB| uses *maven* as build automation tool so also a knowledge of maven is needed.
	If you are new using *maven* have a look to `this complete documentation <http://www.sonatype.com/books/mvnref-book/reference/public-book.html>`_
	For a quick introduction of the main concepts read the `|GS| Maven Guide <http://docs.geoserver.org/latest/en/developer/maven-guide/index.html>`_ .

	
#.	**Eclipse**
	
	In this Tutorial is used Eclipse as IDE and the mvn plugin for eclipse for creating the eclipse projects.
	Clearly you can use any other IDE or editor, is suggested use something that have a full Maven integration like *Netbeans* or a good maven plugin for create the project.

	
Clone the project
`````````````````

Cloning the project with git is an easy task. In the rest of this guide assume that the work directory will be ``~/work/code/``.
Navigate to `|GB| |GH| Page <https://github.com/geosolutions-it/geobatch>`_ and copy the link of the remote repository:

.. figure:: images/urlRepo.png

	*A snippet of |GB| |GH| page, where the url of the repo is provided*

You can see three types of url, is suggested use the SSH one but you have to `setup the SSH keys<https://help.github.com/articles/generating-ssh-keys#platform-all>`_ before.

Copy the URL of the repo from |GH| and launch the git clone command::
	
	$~/work/code# git clone git@github.com:geosolutions-it/geobatch.git

when the cloning process is finished the directory ``/geobatch`` under :file:``~/work/code`` is created.


Compile the project
```````````````````

The compilation steps with the help of Maven are simple.

For compile the whole project, with all modules (even those that are unsupported yet), downloading the dependencies sources and create the eclipse projects, navigate to ``~/work/code/geobatch/src`` and type::

	$~/work/code/geobatch/src# mvn clean install eclipse:clean eclipse:eclipse -Dall -DdownloadSources=true -DdownloadJavadocs=true

|GB| is developed in order to be modular so it uses the maven profiles for easily add and remove modules in compilation.

For Compiling only the supported profiles run::

	$~/work/code/geobatch/src# mvn clean install eclipse:clean eclipse:eclipse -Pgeotiff,shapefile,task-executor,freemarker,scripting,commons -DdownloadSources=true -DdownloadJavadocs=true

Under ``~/work/code/geobatch/src`` there are two script build.bat and build.sh (the first for windows and the second for linux) that automatize the build string.

.. warning:: Don't run lazilly the script without see what option are setted inside. Be sure that the script is configured with your needed compilation profile.

After the compilation is done open Eclipse, create a new **Java Working Set** (right click in package explorer new -> Java Working Set) Then right click on it and select Import -> Existing Projects into Workspace -> Browse -> select ~/work/code/geobatch/src -> OK .

All eclipse projects of |GB| modules will be imported into eclipse.

.. figure:: images/eclipseProjects.png


Quick |GB| modules description
``````````````````````````````

In the previous picture is possible to see several eclipse projects that compose |GB|. Each Elipse project represent a |GB| maven module.

The projects with the name that start with ``gb-action`` represent action implementation. In some cases the project implement just an action (f.e. ``gb-action-imagemosaic``), other times more than an action is implemented (f.e. ``gb-action-geotiff``).

The decision if for a specific task is better develop a single action or divide the task in more actions is leaved to the programmer, the the only guidelines are:

#.	In a Single action module must reside only actions with a strong relation each other
	
	For example all Actions that operates on GeoTiff file or that interact with |GS|. It allows more flexibility at time of write the flow configuration.

#.	Avoid complex and big "factotum" Actions

	Particularly if inside an Action are performed different task. That's the role of the flow, not of the Action.
	
The projects ``gb-core-model``, ``gb-core-impl``, ``gb-dao-xstream``, ``gb-fs-catalog``, ``gb-fsm-core``, ``gb-fsm-quartz`` are the core of geobatch, they models and implements the key concepts that are used by the actions and that transforms a set of action and a flow-configuration into a running Flow.

The Projects ``gb-ftp-server`` and ``gb-users`` implements the |GB| embedded FTP-Server and the |GB| Users managements.

The Project ``gb-application`` build |GB| as a war and when that war is deployed on a WebContainer a web GUI for manage Flows and Users is avaiable due to the presence of the projects ``gb-gui``.


Startup geobatch with embedded jetty
````````````````````````````````````

The Project ``gb-application`` is also important for testing. The class ``it.geosolutions.geobatch.jetty.Start`` allow |GB| to be started directly from the IDE within **Jetty web container**

Is possible also start |GB| from the command line with the command::

	$~work/code/geobatch/src/application# mvn jetty:run

After |GB| is started access to the web interface at the URL *http://localhost:8080/geobatch/* and verify that the building process has terminated with success.



2. Develop an action
--------------------


Add a new module Vs create new project
``````````````````````````````````````


Write the classes and naming Convenctions
`````````````````````````````````````````

A module that implements one or more actions related each other, must be called *gb-action-* + **modulename** so ``gb-action-modulename``.

An action is composed of 4 mandatory classes that should follow this naming convenction: Given an Action Name **ExampleAction** the four class must be called 

#.	ExampleAction*AliasRegistar*.java 

#.	ExampleAction*GeneratorService*.java

#.	ExampleAction*Configuration*.java

#.	ExampleAction*Action*.java

If a module implements more than an action, the 4 class for each action must be reside under a package called *it.geosolutions.geosolutions.modulename.exampleaction* 

In the next paragraphs are shows how the four classes must be implemented. Is also provide a code template where are omissed the imports, the package declaration and the license.

The reader can use these templates replacing the placeholder #ACTION_NAME# with the custom action name and implementing where the comments starts with TODO .


Configuration
`````````````````````

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
`````````````

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

For a deep documentation about XStream aliases see the official documentations and `this tutorial <http://xstream.codehaus.org/alias-tutorial.html>`_.


Action
``````

The class #ACTION_NAME#Action.java holds the business logic of the action and the implementation of the execute method is the main task for the |GB| action developer.

The template below shows a typical structure of the execute method that iterate on all the events intercepted.

The whole loop body is wrapped inside a ``try`` block so any Exception that isn't explicitally handled will be caught by the ``catch`` block and an ActionException will be thrown.

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
						// TODO DO SOMETHING WITH THE INCOMING EVENT, ADD THE ACTION IMPLEMENTATION
						
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


GeneratorService
````````````````

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


Write a sample configuration for the Action
```````````````````````````````````````````


Unit Testing
````````````


create a war with the new Action
````````````````````````````````