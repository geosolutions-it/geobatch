.. |GB| replace:: *GeoBatch*
.. |demo| replace:: *geobatch-demo*
.. |GS| replace:: *GeoServer*

GeoTIFF Demo HOW-TO
====================

The |demo| project uses |GB| to convert a bulk of GeoTIFFs files to tiled GeoTIFFs with overviews and publish them on |GS|


Installing |demo|
-----------------

if you haven't already done, setup the ``GEOBATCH_CONFIG_DIR`` and make sure that you have setted the corresponding environment variable (see installation chapter).

Locate into |GB| deployment the folder WEB-INF/data/

Copy the folder geotiff and the file geotiff.xml and paste into your ``GEOBATCH_CONFIG_DIR``

`Download one or many .tiff fi les <https://github.com/geosolutions-it/geoserver-manager/tree/master/src/test/resources/testdata/time_geotiff>`_ for example usage.

.. note::	For a correct download for each file click on the link and in the following window click on raw button. 
	Remember to replace dot(.) with underscore(_).
	Please dont't download the file right clicking on the link and selecting save as. The downloaded file may be corrupt.
	
Create in a location of your choice ``output`` and ``backup`` directories::

  $ mkdir output_tiff
  $ mkdir output_backup

Edit the |demo| flow configuration under ``/GEOBATCH_CONFIG_DIR/geotiff.xml``. There, edit:

* ``geoserverURL``     pointing a running geoserver.

create a workspace named ``it_geosolutions``

Running
-------

#. Start tomcat via ``startup.sh``.
#. Go to http://localhost:8080/geobatch/flows.do You should see a stopped ``geotiff``. Start it.
#. Put one or many raster files into ``geotiff/in`` directory.
#. Check the instances tab to see how they run.
#. Access to geoserver and verify that a new GeoTIFF (or many) is published.

.. figure:: images/world_layer.jpg
   :align: center
   
you should see the workspace ``it_geosolutions`` with the layer ``world_200403_3x5400x2700``   
   
.. figure:: images/world.jpg
   :align: center

this is the layer you have published through geostore viewed in openlayers
