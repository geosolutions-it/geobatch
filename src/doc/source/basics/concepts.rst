.. |GB| replace:: *GeoBatch*
.. |GS| replace:: *GeoServer*

.. _`keyConcepts`:

Key concepts
============

|GB|'s basic idea is to perform a **chain of actions** triggered by custom defined events. The possible **event generators** include monitoring for new files added to a directory, or receiving files in the embedded FTP server. *Actions* range from geotransforming an input raster file, to creating overviews, or publishing data into a |GS| instance.

Flow
-----

A Flow is the key concept of |GB|; it defines the chain of actions to be performed, how to trigger its execution, how to throttle the number of parallel executions and so on: it is composed by a collection of event generators, actions and listeners.

Flows are defined in ``xml`` files which are called *flow configurations*. So, customizing |GB| to your needs will mean writing this configurations, with the help of this documentation.


Event Generators
----------------

Event generators define the conditions under which a particular batch process will be triggered.

|GB| supports file-based event generators. Each event generator will periodically poll a particular location for file change events (file or directory creation, deletion or modification). This monitoring can be performed locally over a filesystem directory or, using the embedded FTP server, which allows for pushing data directly into GeoBatch using FTP commands.

Filters can be set when defining an event generator, so it activates only for particular event types (for instance, subdirectory creation) or a particular file name or extension (using wildcards). Other parameters, such as the polling iterval, are defined in event generators too.


Actions
-------

Once the event conditions are met, |GB| triggers the associated flow of action, or set of concatenated actions, which define the data manipulation processes.

Actions get a collection of events as input, and return a collection of new events as output, so complex processes can be constructed from a concatenation of atomic actions. Actions perform tasks, which provide the core functionalities in |GB|. 

There is a **wide collection of action types available** to automate several type of different tasks as *File-based resource management* , *GeoServer resources Publishing* , *raster processing* and *data migration between geotools Datastores* .

We tend to **keep actions as simple as possible** in order to account for reusability in different flows. When developing a new actions a developer should first make sure the same goal cannot be achieved with a combination of the available actions and if not it should decompose the processing into atomic steps that can either be coded easily and/or can reuse existing actions anyway.
  

Listeners
---------

Listeners are used to get feedback about actions as their execution is in progress. For instance, they can be used to log the action steps and result state, or draw a progress bar in the user interface. The types of listeners are:

* **CumulatingProgress**: Used to send messages to the user graphical interface.
* **StatusProgress**: Used to monitor the progress status of individual actions.
* **LoggingProgress**: Used to log messages about the execution progress of actions.


FLowChart of a Sample Flow - GeoTiff preprocessing and pubslishing in GeoServer
--------------------------------------------------------------------------------

Let us now briefly introduce the flowchart of a simple flow that would:

  * trigger when a new geotiff file is placed in a certain directory
  * retile the geotiff file
  * add proper overviews
  * publish the reprocessed geotiff in GeoServer
  
The flow would contains:
 
  * an event generator to monitor a certain directory for incoming geotiff files
  * an actions to retile the input geotiff
  * an action to add overviews to the retiled geotiff
  * an actions to publish the geotiff to a GeoServer instance
  
The configuration directory
---------------------------

The configuration directory is where all |GB| flow definitions and other configuration files are placed. |GB| comes with a default configuration directory with a collection of sample flow configurations, that will be located under ``WEB-INF/data`` in |GB|'s installation.