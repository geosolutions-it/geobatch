.. |GB| replace:: *GeoBatch*
.. |demo| replace:: *geobatch-demo*
.. |GS| replace:: *GeoServer*

ShapeFile Demo HOW-TO
====================

The |demo| project uses |GB| Get a pack of shapefiles (zipped) and ingest them into a |GS| instance.


Installing |demo|
-----------------

if you haven't already done, setup the ``GEOBATCH_CONFIG_DIR`` and make sure that you have setted the corresponding environment variable.

Locate into |GB| deployment the folder WEB-INF/data/

Copy the folder geotiff and the file geotiff.xml and paste into your ``GEOBATCH_CONFIG_DIR``

Edit the |demo| flow configuration under ``/GEOBATCH_CONFIG_DIR/geotiff.xml``. There, edit:

* ``geoserverURL``        pointing a running geoserver.
* ``defaultStyle``	      if you use `this <http://xxxxx>`_ shapefile change the value into ``point``.
* ``memoryMappedBuffer``  set ``true`` if you run geobatch on linux server ``false`` on windows (memoryMappedBuffer is not well supported by windows)

create a workspace named ``it_geosolutions``

Running
-------

#. Start tomcat via ``startup.sh``.
#. Go to http://localhost:8080/geobatch/flows.do You should see a stopped ``geotiff``. Start it.
#. Put a zip archive with valid shapefiles in ``geobatch_flow_tiff/in``. For each shape is necessary provide the .dbf .shp .shx files. using also a .prj file is strong recommended
#. Check the instances tab to see how they run.
#. Access to geoserver and verify that a new shapefile (or many) is published.

