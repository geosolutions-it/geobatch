.. geobatch documentation master file, created by
   sphinx-quickstart on Sat Jan 16 14:27:14 2010.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

- In GeoBatch there are two fundamentals concepts:
====================================

1) GEOBATCH ConfigDir
------------------------------------

This Directory is the context where all configuration files are defined properly, as for example 
a template freemarker, Normally is located into flowDirConfiguration, the configuration directory contains files
static and not changeable. At implementation level this directory is set into ActionConfiguration.


2) GEOBATCH TempDir (hold WorkingDir)
------------------------------------

This directory is the context where GeoBatch is working and where instances of Action can create its temporary files. 
Several Instances of the same action should have tempDir distinct. The management of these tempDir (creation, removal or any archiving) 
is delegated to the Engine GeoBatch.
In case an Action is instantiated manually and not by the engine of GeoBatch, 
you will be required to manage the tempDir manually.


 

----------------------------------------------------------------


At implementation level, the method:

.. sourcecode:: java
	
	//is deprecated
	getWorkingDir();


and now is the same:
 
.. sourcecode:: java
	
	getConfigDir();


the

.. sourcecode:: java

	getWorkingDir();

will be modified in:

.. sourcecode:: java
		
	cfg.getConfigDir();
	
	// or in

	action.getTempDir();

	//depending on the case.

	

.. toctree::
   :maxdepth: 5
