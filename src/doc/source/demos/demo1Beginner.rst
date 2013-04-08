.. |GB| replace:: *GeoBatch*
.. |demo| replace:: *geobatch-demo*
.. |GS| replace:: *GeoServer*

Geotiff preprocessing and publishing in GeoServer
==================

Thise |demo| uses |GB| to preprocess GeoTIFFs files with tiling and overviews and then it publishes them on |GS|

The demo assumes you have a running instance of |GS| to which you intend to publish your data. It also assumes you are using the standalone version of GeoBatch (which is ok for personale use 
but not for production use!).


Installing |demo|
-----------------

if you haven't already done, setup the ``GEOBATCH_CONFIG_DIR`` and make sure that you have setted the corresponding environment variable.

Locate into |GB| deployment the folder WEB-INF/data/

Copy the folder geotiff and the file geotiff.xml and paste them into your ``GEOBATCH_CONFIG_DIR``

Create ``output`` and ``backup`` directories::

  $ mkdir output_tiff
  $ mkdir output_backup

Edit the |demo| flow configuration under ``/GEOBATCH_CONFIG_DIR/geotiff.xml``. There, edit:

* ``outputDirName``    pointing to your ``output_tiff`` directory (absolute path).
* ``output_backup``    pointing to your ``output_backup`` directory (absolute path).
* ``geoserverURL``     pointing a running |GS| instance.

create a workspace named ``it_geosolutions``

Running
-------

#. Start tomcat via ``startup.sh``.
#. Go to http://localhost:8080/geobatch/flows.do You should see a stopped ``geotiff``. Start it.
#. Put some raster files in ``geotiff/in``.
#. Check the instances tab to see how they run.
#. Get the processed files in your ``output_tiff`` directory. In case of failure, recover the original files and check the error cause from ``output_backup``.

