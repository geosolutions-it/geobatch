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

`Download the shapefile <https://github.com/geosolutions-it/geoserver-manager/tree/master/src/test/resources/testdata/shapefile>`_ (all three files) and compress them in a .zip archive called ``cities``

(OPTIONAL) You can add to that archive also other shapefile, for each shape is necessary provide at most the .dbf .shp .prj files.

Edit the |demo| flow configuration under ``/GEOBATCH_CONFIG_DIR/geotiff.xml``. There, edit:

* ``geoserverURL``        pointing a running geoserver.
* ``defaultStyle``	      with the shapefile linked above set ``point``.
* ``memoryMappedBuffer``  set ``true`` if you run geobatch on linux server ``false`` on windows (memoryMappedBuffer is not well supported by windows)
* ``defaultNamespace``	  set ``it_geosolutions``


Running
-------

#. Start tomcat via ``startup.sh``.
#. Go to http://localhost:8080/geobatch/flows.do You should see a stopped ``geotiff``. Start it.
#. Put the zip archive in ``geobatch_flow_tiff/in``.
#. Check the instances tab to see how they run.
#. Access to geoserver and verify that a new shapefile (or many) is published.

.. figure:: images/cities_layer.jpg
   :align: center
   
you should see the workspace ``it_geosolutions`` with the layer ``cities``   
   
.. figure:: images/cities_point.jpg
   :align: center

this is the layer you have published through geostore viewed in openlayers