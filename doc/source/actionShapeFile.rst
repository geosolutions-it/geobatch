Shapefile Action
================

The Shapefile Action is used to publish a single shapefile, or a collection of shapefiles, in GeoServer.

Input
-------

A collection of files, or a compressed zip file, containing one or more shapefiles.

*One* valid shapefile is composed of at least *three* actual files in the filesystem, with the same base name and the extensions `shp` (for geometries), `shx` (for spatial index) and `dbf` (for feature attributes).

If any of these is not provided, the shapefile will not be considered valid, and won't be published.


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
    * **geoserverURL**: The base URL of the GeoServer instance to use. Example: http://localhost:8080/geoserver
    * **geoserverUID**: Privileged GeoServer user. Example: "admin"
    * **geoserverPWD**: Password for the GeoServer user. Example: "geoserver"

* Datastore options:
    * **defaultNamespace**: The namespace (workspace) to create the store under. Will be created if not exists.
    * **storeName**: The datastore name.
    * **charset**: Character encoding for the `dbf` file. For example, "ISO-8859-1".
    * **createSpatialIndex**: Boolean. Enables the automatic creation of a spatial index.
    * **memoryMappedBuffer**: Boolean. Enables the use of memory mapped I/O.
    * **cacheAndReuseMemoryMaps**: Boolean.

* Layer publishing options:
    * **dataTransferMethod**: Can be ``DIRECT``, for file upload, or ``EXTERNAL``.
    * **wmsPath**: The location of the layer in the WMS capabilities layer tree.
    * **crs**: The layers' CRS.
    * **envelope**: Bounding box.
    * **defaultStyle**: Style to apply to the layers. Note this is only used in single shapefile publication.  For multiple shapefiles, styles will be assigned automatically by GeoServer. Future versions will handle more advanced styling assignation.
    * **styles**: ?

Configuration example:

.. sourcecode:: xml

  <GeoServerShapeActionConfiguration>
    <serviceID>ShapeFileGeoServerService</serviceID>
    <id>step1</id>
    <name>ShapeFileIngestion</name>
    <description>Get a pack of shapefiles and ingest them into a GeoServer instance.</description>
    <geoserverURL>http://localhost:8080/geoserver</geoserverURL>
    <geoserverUID>admin</geoserverUID>
    <geoserverPWD>geoserver</geoserverPWD>
    <dataTransferMethod>DIRECT</dataTransferMethod>
    <wmsPath>/</wmsPath>
    <defaultNamespace>it_geosolutions</defaultNamespace>
    <storeName>shapefile_plus</storeName>
    <crs>EPSG:4326</crs>
    <envelope/>
    <defaultStyle>polygon</defaultStyle>
    <styles/>
    <charset>UTF-8</charset>
    <createSpatialIndex>true</createSpatialIndex>
    <memoryMappedBuffer>true</memoryMappedBuffer>
    <cacheAndReuseMemoryMaps>true</cacheAndReuseMemoryMaps>
  </GeoServerShapeActionConfiguration>

