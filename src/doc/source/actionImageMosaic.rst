Image Mosaic Action
===================

The ImageMosaic Action can be used to publish or update an `ImageMosaic <http://docs.geoserver.org/stable/en/user/tutorials/image_mosaic_plugin/imagemosaic.html>`_ up to an instance of the `GeoServer <http://docs.geoserver.org/2.0.0/user/>`_. It is fundamentally based on the GeoTools `ImageMosaic <http://docs.geoserver.org/stable/en/user/tutorials/image_mosaic_plugin/imagemosaic.html>`_ plugin and makes use of a series of REST calls to perform remote GeoServer update and queries via the GeoServer-Manager library.


Input
-----

* A writeable directory full of with readable geotiff files that we may want to be ingested automatically in an index

OR

* An **ImageMosaicCommand** file to drive the ingestion process, which would look like the following:

.. sourcecode:: xml

	<ImageMosaic>
	  <!-- Base directory for the mosaic -->
	  <base>/path/to/destination/layer/</base>
	  
	  <!-- Files to add or remove from the index  -->
	  <add>/path/of/file/to/add/geoN.tif</add>
	  <add>/path/of/file/to/add/geo.tif</add>
	  <del>/path/of/file/to/delete/geo.tif</del>
	  <del>/path/of/file/to/delete/geoM.tif</del>
	  <!-- physically delete the granules from disk 
	  after removing them from the index -->
	  <deleteGranules>false</deleteGranules>
	  <!-- if and where to backp up granules when 
	  removing them. It must be an absolute path -->
	  <backupDirectory>Path_to_backup_directory</backupDirectory>
	  
	  <!-- ImageMosaicAction Configuration parameters -->	   
	  <backgroundValue>-9999</backgroundValue>
	  <outputTransparentColor></outputTransparentColor>
	  <inputTransparentColor></inputTransparentColor>
	  <allowMultithreading>true</allowMultithreading>
	  <useJaiImageRead>false</useJaiImageRead>
	  <tileSizeH>256</tileSizeH>
	  <tileSizeW>256</tileSizeW>

	  <!-- should be set to true if the upload adds 
	    granules having BBox greater then the previous one -->
	  <reloadStore>false</reloadStore>

	  <!--NONE, REPROJECT_TO_DECLARED, FORCE_DECLARED-->
	  <projectionPolicy>NONE</projectionPolicy>
	  <styles/>
	  
	  <!-- do we want to perform a Reset on GeoServer at the end
	  of the process or not.  default to True.-->
	  <finalReset>false</finalReset>
	  
	  <!-- Delay in Seconds to apply when moving files around when NFS is involved. Must be >=0-->
	  <NFSCopyWait>10</NFSCopyWait>
	  
	  ...
	  <datastorePropertiesPath>imagemosaic_work/config/datastore.properties</datastorePropertiesPath>

	  <!-- METADATA -->
	  <!-- TIME Dimension -->
	  <timeDimEnabled>false</timeDimEnabled>
	  <!-- LIST, CONTINUOUS_INTERVAL, DISCRETE_INTERVAL -->
	  <timePresentationMode>LIST</timePresentationMode>
	  <timeRegex>[0-9]{8}T[0-9]{9}Z(\?!.\*[0-9]{8}T[0-9]{9}Z.\*)</timeRegex>

	  <!-- ELEVATION Dimension -->
	  <elevDimEnabled>false</elevDimEnabled>
	  <!-- LIST, CONTINUOUS_INTERVAL, DISCRETE_INTERVAL -->	  
	  <elevationPresentationMode>LIST</elevationPresentationMode>
	  <elevationRegex><![CDATA[(?<=_)(\\d{4}\\.\\d{3})(?=_)]]></elevationRegex>

	  ...
	</ImageMosaic>

ImageMosaicCommand can be used to *override* (more on this later) the parameters defined statically inside the action configuration.

If you use shapefile as datastore and you need time support (with hours minutes and seconds) be shure to set: ::

	-Dorg.geotools.shapefile.datetime=true 

into the: :: 

	JAVA_OPTS 

of the *GeoServer* instance which may build the mosaic.

Along the sameline, make sure to use the following java switch to make sure the TimeZone is GMT and avoid problems with parsing/encoding dates: ::

  -Duser.timezone=GMT

Output
---------

* A file (queue) representing the output layer (if successfully configured)

* A null object (if something goes wrong)

Referring to the below Flow Chart Image you can see:

1. Using the ImageMosaicCommand you can perform complex operation on the layer:


  * Creation

  * Update (adding or removing images)


2. using the directory as input is acceptable only if we are creating the ImageMosaic in GeoServer, during updates we shall use an ImageMosaicCommand XML file as input.

The Flow Chart
--------------

.. figure:: images/ImageMosaicAction.jpg
   :alt: ImageMosaicActions FlowChart
   :align: center
   
   ImageMosaicActions FlowChart
   
References
----------- 

* `ImageIO-EXT <http://java.net/projects/imageio-ext>`_
* `Coverage <http://docs.geotools.org/latest/userguide/guide/library/coverage/index.html>`_
* `ImageMosaic <http://docs.geoserver.org/stable/en/user/tutorials/image_mosaic_plugin/imagemosaic.html>`_



ImageMosaicAction Configuration
--------------------------
The configuration for the ImageMosaicAction allows to specify the basic parameters for the actions plus various parameters for that are specific to the construction of the ImageMosaic itself in GeoServer.

ImageMosaicCommand
@@@@@@@@@@@@@@@@@@@@@

We are now going to describe the ImageMosaicCommand which represents one of the possible inputs for the ImageMosaicActions, since, as mentioned above we can either pass a directory to it 
or an XML file containing the XML serialization of an ImageMosaicCommand.

Leveraging on the ImageMosaicCommand we can the fly override the ImageMosaicActions configuration statically defined inside the flow configuration in order to add or delete granules from an existing mosaic or to create a new mosaic. 

Inputs
@@@@@@@

The set of granules to add/remove d the entire mosaic and the base directory:

.. sourcecode:: xml

	  <base>/path/to/destination/layer/</base>
	  <add>/path/of/file/to/add/geoN.tif</add>
	  <add>/path/of/file/to/add/geo.tif</add>
	  <del>/path/of/file/to/delete/geo.tif</del>
	  <del>/path/of/file/to/delete/geoM.tif</del>

Where 'base' represents the target directory to place the mosaic (if you are creating it); If you are updating a mosaic the 'base' folder may exists so all the files in that directory will be used to create the mosaic.

The 'add' and 'del' file list is used to add or remove granules from an existing (or during creation for the add) of an imagemosaic.

The target Geoserver
@@@@@@@@@@@@@@@@@@@@
You can control the GeoServer to work against via the following parameters.

.. sourcecode:: XML

			<geoserverPWD>geoserver</geoserverPWD>
			<geoserverUID>admin</geoserverUID>
			<geoserverURL>http://localhost:8080/geoserver</geoserverURL>


The options used to configure the coverageStore/resource/layer
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

To understand most of the following parameters please read the official GeoServer documentation ( http://docs.geoserver.org/latest/en/user/tutorials/image_mosaic_plugin/imagemosaic.html )

.. sourcecode:: xml

	  <backgroundValue>-9999</backgroundValue>
	  <outputTransparentColor></outputTransparentColor>
	  <inputTransparentColor></inputTransparentColor>
	  <allowMultithreading>true</allowMultithreading>
	  <useJaiImageRead>false</useJaiImageRead>
	  <tileSizeH>256</tileSizeH>
	  <tileSizeW>256</tileSizeW>		
	  <!--NONE, REPROJECT_TO_DECLARED, FORCE_DECLARED-->
	  <projectionPolicy>NONE</projectionPolicy>
	  ...
	  <styles/>

  
Since GeoServer 2.2.x Elevation and Time metadata settings are supported:

.. sourcecode:: xml

	  <!-- METADATA -->

	  <!-- TIME -->
	  <timeDimEnabled>false</timeDimEnabled>
	  <!-- LIST, CONTINUOUS_INTERVAL, DISCRETE_INTERVAL -->
	  <timePresentationMode>LIST</timePresentationMode>
	  <timeRegex>[0-9]{8}T[0-9]{9}Z(\?!.\*[0-9]{8}T[0-9]{9}Z.\*)</timeRegex>

	  <!-- ELEVATION -->
	  <elevDimEnabled>false</elevDimEnabled>
	  <elevationPresentationMode>LIST</elevationPresentationMode>
	  <elevationRegex><![CDATA[(?<=_)(\\d{4}\\.\\d{3})(?=_)]]></elevationRegex>


Essentially the above settings enables the metadata (time and/or elevation) support on the store you are going to create on the target GeoServer.

.. sourcecode:: xml

	  <timeDimEnabled>true</timeDimEnabled>
	  ...
	  <elevDimEnabled>true</elevDimEnabled>
	  ...
  
The *presentation mode* sets the representation of the metadata:

For example for elevation::

  LIST:
    0, 1, 10, 
  CONTINUOUS_INTERVAL:
    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
  DISCRETE_INTERVAL:
    0:1:10
    starts from 0 with step 1 ends at 10
  
The *regex* (elevation and time) are used by the geotools imagemosaic plugin to parse the file name and recognize the elevation and time. Each file of the mosaic infacts should be named following a convention (as specified into these regex).
For example using:

.. sourcecode:: xml

	  <timeRegex>[0-9]{8}T[0-9]{9}Z(\?!.\*[0-9]{8}T[0-9]{9}Z.\*)</timeRegex>
	  ...
	  <elevationRegex><![CDATA[(?<=_)(\\d{4}\\.\\d{3})(?=_)]]></elevationRegex>

Your mosaic should contains files named as following::

	  FILENAME_20121231T235959_0001.000.tif

Which represents a granule with date 2012-12-31 23:59:59 and elevation 1.0.

Minimal giode to using the datastore.properties file
@@@@@@@@@@@@

The datastore.properties is a properties file which is used by the ImageMosaic plugin in GeoTools (hence GeoServer) in order to place the mosac index in  a GeoTools datastore, which means not only a Shapefile (for which it is not needed as the ImageMosaic by default creates an ImageMosaci inside the mosaic root directory) but also a Postgis table, an H2 database and so on.

Here a complete example with all the acceptable options for Postgis. The files contains a first KVP whose key is **SPI** and whose values is the name of the SPI service (with package name) for the kind of datastore we want to use from GeoTools. The others KVPs are taken from the datastore connection parameters. Notice that for the datastores that support it we can also connect to an external JNDI datasource (see below).

.. sourcecode:: xml

	  <datastorePropertiesPath>imagemosaic/config/datastore.properties</datastorePropertiesPath>

*datastore.properties*::

	#String
	# database type
	dbtype=

	#String
	# host
	host=

	#Integer
	# database server port
	port=

	#String
	# database
	database=

	#String
	# schema
	schema=
		
	#String
	# user name to login as
	user=

	#String
	# password used to login
	passwd=

	#String
	# namespace prefix
	namespace=

	#DataSource
	# data source
	Data\ Source=

	#Integer
	# maximum number of open connections
	# Default 10
	max\ connections=

	#Integer
	# minimum number of pooled connection
	# default 1
	min\ connections=

	#Boolean
	# check connection is alive before using it
	# Default Boolean.FALSE
	validate\ connections=

	#Integer
	# Number of records read with each iteraction with the dbms.
	# Defatul 1000
	fetch\ size=
	 
	#Integer
	# number of seconds the connection pool will wait before 
	# timing out attempting to get a new connection
	# Default 20 seconds
	Connection\ timeout=

	#String
	# The optional table containing primary key structure and
	# sequence associations. Can be expressed as 'schema.name'
	# or just 'name'.
	Primary\ key\ metadata\ table=

	#Integer 
	# Maximum number of prepared statements kept open and
	# cached for each connection in the pool. 
	# Set to 0 to have unbounded caching, to -1 to disable caching.
	# Default 50.
	Max\ open\ prepared\ statements=

	#boolean 
	# Expose primary key columns as attributes of the feature type
	# defatul false.
	Expose\ primary\ keys=

Using PostGis
같같같같같같같같

If you are using the PostGis (PostgreSQL) API:
In addition to the above options you can use the below one.
Note the 'SPI' key can substitute the 'dbtype'::

	#######################
	# PostgreSQL specific #
	#dbtype=postgis
	SPI=org.geotools.data.postgis.PostgisNGDataStoreFactory
	#Boolean
	# perform only primary filter on bbox
	# Default Boolean.TRUE
	Loose\ bbox=true

	#Boolean
	# use the spatial index information to quickly get an estimate of the data bounds
	# Default Boolean.TRUE
	Estimated\ extends=false

	#Boolean
	# use prepared statements
	#Default Boolean.FALSE
	preparedStatements=false

Using JNDI on PostGis
같같같같같같같같같같�

If you are using the API for the JNDI::

	#################
	# JNDI specific #
	#dbtype=
	SPI=org.geotools.data.postgis.PostgisNGJNDIDataStoreFactory
	#String
	# JNDI data source
	# Default "java:comp/env/"+"jdbc/mydatabase"
	jndiReferenceName=

	#Boolean
	# perform only primary filter on bbox
	# Default Boolean.TRUE
	Loose\ bbox=true

	#Boolean
	# use prepared statements
	#Default Boolean.FALSE
	preparedStatements=false


How the ImageMosaicAction works
-------------------------------

If you pass an ImageMosaicCommand (IMC) to the imagemosaic action (you can also pass a queue of IMCs), geobatch will proceed with the following steps:

1. check for the 'base' directory (if not exists create it)
2. copy all the files in the 'add' list to the base folder
3. remove all the files in the 'del' list from the base folder (if the mosaic exists otherwise this step is skipped with warning)
4. If the mosaic does not exists:

 - copy a datastore.properties to the base directory (which tells to the GeoTools ImageMosaic pluging where to store/update the datastore)
 - create the indexer.properties into the base directory (which tells to the GeoTools ImageMosaic pluging how the read and handle mosaic metadata such as time and elevation)
 - create the timeregex.properites into the base directory (the time regex)
 - create the elevationregex.properites into the base directory (the elevation regex)
 - Using GeoServer Manager create the ImageMosaic on the target geoserver using specified (from IMC or flow configuration) options

5. If the mosaic exists:

 - connect to the target datastore using the datastore.properties from the base dir (if itsn't present the ImageMosaic uses a shape file, in this case the action will exit with error)
 - Generate a query to select granules to remove
 - Remove the selected granules from the datastore
 - Generate a query to update the granules to add to the datastore
 - Add the granules to the datastore
 - if <reloadStore>true</reloadStore> reload the store on geoserver this should be set to true if the upload adds granules having BBox greater then the previous one.

