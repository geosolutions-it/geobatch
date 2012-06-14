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
#. Put some raster files in ``geotiff/in``.
#. Check the instances tab to see how they run.
#. Access to geoserver and verify that a new GeoTIFF (or many) is published.

