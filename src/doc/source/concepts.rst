.. |GB| replace:: *GeoBatch*
.. |GS| replace:: *GeoServer*

Key concepts
============

|GB|'s basic idea is to apply a **chain of actions** triggered by custom defined events. The possible **event generators** include monitoring for new files added to a directory, or receiving files in the embedded FTP server. *Actions* range from geotransforming an input raster file, to creating overviews, or publishing data into a |GS| instance.


Event Generators
----------------

Event generators define the conditions under a particular batch process will be triggered.

|GB| supports file-based event generators. Each event generator will periodically poll a particular location for file change events (file or directory creation, deletion or modification). This monitoring can be performed locally over a filesystem directory or, using the embedded FTP server, allows for remotely managed data manipulation.

Filters can be set when defining an event generator, so they activate only for particular event types (for instance, subdirectory creation) or a particular file name or extension (using wildcards). Other parameters, such as the polling iterval, are defined in event generators too.


Actions
-------

Once the event conditions are met, |GB| triggers the associated action, or set of concatenated actions, which define the data manipulation processes.

Actions get a collection of events as input, and return a collection of new events as output, so complex processes can be constructed from a concatenation of atomic actions. Actions perform tasks, which provide the core functionalities in |GB|. There is a wide collection of action types available:

* *File-based resource management:*

  * **Collector**: Given a wildcard, selects files from a directory, searching recursively in subdirectories if needed.
  * **Copy**: Copies files to another directory.
  * **Move**: Moves files to another directory.
  * **Extract**: Extracts a zipped file into a destination.
  * **FTP**: Sets a client FTP connection.

* *Publishing in GeoServer*:

  * **ShapeFile**: Publish a collection of shapefiles as layers in GeoServer.
  * **GeotiffGeoServer**: Publish a collection of geotiff files as layers in GeoServer.
  * **ImageMosaic**: Builds and manages an imagemosaic in geoserver from a collection of raster images.
  * **GeoServerReload**: Reloads the configuration in a collection of GeoServer instances.
  * **GeoNetwork**: Metadata insertion in a GeoNetwork catalog.

* *Raster Processing*:

  * **GeotiffOverviewsEmbedder**: Adds overviews to a GeoTIFF image.
  * **GeotiffRetiler**: (re)creates a tiled GeoTIFF.
  * **TaskExecutor**: Action used to execute external tasks such as Gdal operation (and much more...)
  * **Xstream**: Action used to produce xml files from incoming event object or deserialize files to java object form incoming xml files using the xstream library
  * **Scripting**: Action used to run (groovy) scripts
  
* *Unsupported Actions*:
  
  * **FreeMarker**: Action used to produce ascii files using the freemarker library
  * **Shp2pg**: Loads a collection of shapefiles in a PostGIS database.

* *Unsupported Services*:

  * **Octave**: Service used to run multiple octave processes to run parallel octave scripts
  * **JMS**: Service used to run multiple actions via JMS on different pre configured flows
  * **JMX**: Service used to run multiple runtime configured actions via JMX

Listeners
---------

Listeners are used to get feedback about actions as their execution is in progress. For instance, they can be used to log the action steps and result state, or draw a progress bar in the user interface. The types of listeners are:

* **CumulatingProgress**: Used to send messages to the user graphical interface.
* **StatusProgress**: Used to monitor the progress status of individual actions.
* **LoggingProgress**: Used to log messages about the execution progress of actions.


Flows
-----

A flow defines the complete task and execution cycle, combining of all the above: it is composed by a collection of related event generators, actions and listeners.

Flows are defined in ``xml`` files which are called *flow configurations*. So, customizing |GB| to your needs will mean writing this configurations, with the help of this documentation.

