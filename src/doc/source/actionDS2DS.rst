Shapefile Action
================

The Shapefile Action is used to publish a single shapefile, or a collection of shapefiles, in GeoServer.

Input
-----

The source and the destination of the copy can be any GeoTools datastore (shapefiles, jdbc databases, etc.).

The action should be used for appending or replacing existing data, so a flag is used in configuration to specify the desired mode.

The input event should be a file (in XML format) specifying the DataStore connection parameters.

To simplify things we will also support database files (directly or as a zip) as input for some datastore types (such as shapefiles), recognizing if the file is in a known format (e.g. zip, shp) or an xml and proceed accordingly.

Finally the action should be chainable (now it's not), so we need to produce an output event. This event will be a file (an XML file), similar to the one used as an input event, with data of the output feature produced.


Actions
---------

* If the specified ``defaultNamespace`` does not exist, it will be created.
* Create a `Shapefile datastore <http://docs.geoserver.org/stable/en/user/data/shapefile.html>`_ with the specified Datastore options.
* Upload and publish each shapefile as a layer, using the specified layer publishing options.


Output
--------

The same collection of files provided as input.


Configuration
---------------

Main element: ``GeoServerShapeActionConfiguration``.

Will contain the following child elements:

* Identification parameters:
    * **serviceID**: Should be ``ShapeFileGeoServerService``.
    * **id**: An ID for this action.
    * **name**: A name for this action.
    * **description**: A description for this action.

* GeoServer connection parameters:
    * **host**: The base URL of the GeoServer instance to use. Example: http://localhost:8080/geoserver
    * **user**: Privileged GeoServer user. Example: "admin"
    * **passwd**: Password for the GeoServer user. Example: "geoserver"

* Datastore options:
    * **dbtype**: 
    * **host**: The datastore name.
    * ...

* Options:
    * **sourceCRS**
    * **destinationCRS** Some simple processing should be configurable too:
    * **output** feature renaming
    * **attributes** projection / renaming

Configuration example:

.. sourcecode:: xml

  <Ds2dsConfiguration>
      <serviceID>Ds2dsGeneratorService</serviceID>
      <id>Ds2dsGeneratorService</id>
      <description>Ds2ds action</description>
      <name>Ds2dsConfiguration</name>
      
      <listenerConfigurations/>
      <failIgnored>false</failIgnored>
      <purgeData>true</purgeData>
		  
      <outputFeature>                
	  <dataStore>                    
	      <entry>
		<string>dbtype</string>
		<string>postgis</string>
	      </entry>
	      <entry>
		<string>host</string>
		<string>localhost</string>
	      </entry>
	      <entry>
		<string>port</string>
		<string>5432</string>
	      </entry>
	      <entry>
		<string>database</string>
		<string>postgres</string>
	      </entry>
	      <entry>
		<string>user</string>
		<string>postgres</string>
	      </entry>
	      <entry>
		<string>passwd</string>
		<string>postgres</string>
	      </entry>                     
	  </dataStore>
      </outputFeature>
      <projectOnMappings>false</projectOnMappings>
      <attributeMappings/>
	  
  </Ds2dsConfiguration>
