.. |GB| replace:: **GeoBatch**

.. _`setupDvlpEnv`:

Setup the development environment
=================================


Tools needed
------------

#.	**git**
	
	The official |GB| source code repository `is hosted on GitHub <https://github.com/geosolutions-it/geobatch>`_ so a knowledge of the main *git* concepts and functionalities (basic workflow, branching and tagging) are needed in order to work with it.
	
	If you are new using *git* as scm have a look to `this online documentation <http://git-scm.com/doc>`_ .
	
	Have a look to `this guide <http://www.sbf5.com/~cduan/technical/git/>`_ for a quick overview of the main git concepts.

	
#.	**Maven**
	
	|GB| uses *maven* as build automation tool so also a knowledge of maven is needed.
	
	If you are new using *maven* have a look to `this complete documentation <http://www.sonatype.com/books/mvnref-book/reference/public-book.html>`_
	
	For a quick introduction of the main concepts read the `GeoServer Maven Guide <http://docs.geoserver.org/latest/en/developer/maven-guide/index.html>`_ .

	
#.	**Eclipse**
	
	In this Tutorial is used **Eclipse** as IDE and the **mvn plugin for eclipse** for creating the eclipse projects.
	
	Clearly you can use any other IDE or editor, but is suggested use something that have a good Maven integration like **Netbeans** or a good maven plugin for create the IDE projects from the Maven pom files.

	
	
Clone the project
-----------------

Cloning the project from **GitHub** is an easy task. In the rest of this guide assume that the work directory will be ``~/work/code/``.
Navigate to `GeoBatch *GitHub* Page <https://github.com/geosolutions-it/geobatch>`_ and copy the link of the remote repository:

.. figure:: images/urlRepo.png
   :width: 800

	A snippet of GeoBatch *GitHub* homepage, where the url of the repo is provided.

You can see three types of url, is suggested use the SSH one but you have to `setup the SSH keys <https://help.github.com/articles/generating-ssh-keys#platform-all>`_ before.

Copy the URL of the repo from *GitHub* and launch the ``git clone`` command::
	
	$ ~/work/code# git clone git@github.com:geosolutions-it/geobatch.git

when the cloning process is finished the directory ``/geobatch`` under :file:``~/work/code`` will be created.



Compile the project
-------------------

The compilation steps with the help of Maven are simple.

For compile the whole project with all modules (even those that are unsupported yet), downloading the dependencies sources and create the eclipse projects, navigate to ``~/work/code/geobatch/src`` and type::

	$ ~/work/code/geobatch/src# mvn clean install eclipse:clean eclipse:eclipse -Dall -DdownloadSources=true -DdownloadJavadocs=true

|GB| is developed in order to be modular so it uses the maven profiles for easily add and remove modules in compilation.

For example, in order to compile only the supported profiles run::

	$ ~/work/code/geobatch/src# mvn clean install eclipse:clean eclipse:eclipse -Pgeotiff,shapefile,task-executor,freemarker,scripting,commons -DdownloadSources=true -DdownloadJavadocs=true

Under ``~/work/code/geobatch/src`` there are two script build.bat and build.sh (the first for windows and the second for linux) that automatize the build command.

.. warning:: Don't run lazily the script without check the settings inside. Be sure that the script is configured with your needed compilation profile.

After the compilation is done open Eclipse, create a new **Java Working Set** (right click in package explorer new -> Java Working Set) Then right click on it and select Import -> Existing Projects into Workspace -> Browse -> select ~/work/code/geobatch/src -> OK .

All eclipse projects of |GB| modules will be imported into eclipse.

.. figure:: images/eclipseProjects.png
   :width: 600



Quick GeoBatch modules description
----------------------------------

In the previous picture is possible to see several eclipse projects that compose the whole |GB| project. Each Elipse project represent a |GB| maven module.

The projects with name starting with ``gb-action`` represent action implementation. In some cases the project implement just an action (f.e. ``gb-action-imagemosaic``), other times more than an action is implemented (f.e. ``gb-action-geotiff``).

The decision if for a specific task is better develop a single action or divide the task in more actions is leaved to the programmer, the only guidelines are:

#.	In a Single action module must reside only actions with a strong relation each other.
	
	For example all Actions that operates on *GeoTiff* files or that interact with *GeoServer*. Greater granularity allows more flexibility at time of write the flow configuration.

#.	Avoid complex and big "factotum" Actions

	Particularly avoid actions that perform different tasks. Concatenate different tasks is a role of the flow through flow-configuration, not of the Action.
	
The projects ``gb-core-model``, ``gb-core-impl``, ``gb-dao-xstream``, ``gb-fs-catalog``, ``gb-fsm-core``, ``gb-fsm-quartz`` are the core modules of geobatch, they models and implements the key concepts that are used by the actions and that transforms a set of actions and a flow-configuration into a running Flow.

The Projects ``gb-ftp-server`` and ``gb-users`` implements the |GB| embedded FTP-Server and the |GB| Users managements.

The Project ``gb-application`` build |GB| as a war and when that war is deployed on a WebContainer a web GUI for manage Flows and Users is avaiable due to the presence of the project ``gb-gui``.



Startup geobatch with embedded jetty
------------------------------------

The Project ``gb-application`` is also important for testing. The class ``it.geosolutions.geobatch.jetty.Start`` allow |GB| to be started directly from the IDE within **Jetty web container**

Is also possible start |GB| from the command line launching the command::

	$ ~work/code/geobatch/src/application# mvn jetty:run

After |GB| is started access to the web interface at the URL `http://localhost:8080/geobatch/ <http://localhost:8080/geobatch/>`_ and verify that the building process is terminated with success.


