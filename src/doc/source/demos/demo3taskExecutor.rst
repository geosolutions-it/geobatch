.. |GB| replace:: *GeoBatch*
.. |demo| replace:: *geobatch-demo*

Preprocessing GeoTiff files with the TaskExecutor 
=================================================

The |demo| project uses |GB| and *GDAL* to convert a bulk of raster files from almost any format to tiled GeoTIFFs with overviews.

Allowed input formats are those `supported by GDAL <http://www.gdal.org/formats_list.html>`_.


NOTE: To be able to run this flow you need to download and install GDAL.

Setup the |demo| flow
---------------------

if you haven't already done, please read the demo setup :doc:`here <./demo0setup>`.

Edit the |demo| flow configuration under ``GEOBATCH_CONFIG_DIR/raster_preprocess.xml``:

* ``outputDirName`` pointing to your ``output_tiff`` directory (absolute path).
* ``output_backup`` pointing to your ``output_backup`` directory (absolute path).
* ``translateExecutable`` pointing to ``${GDAL_HOME}/bin/gdal_translate``
* ``overviewExecutable`` pointing to ``${GDAL_HOME}/bin/gdaladdo``.

Create ``output`` and ``backup`` directories::

  $ mkdir output_tiff
  $ mkdir output_backup

Running
-------

#. Start tomcat via ``startup.sh``.
#. Go to http://localhost:8081/geobatch/flows.do You should see a stopped ``geobatch_flow_tiff``. Start it.
#. Put some raster files in ``raster_preprocess/in``.
#. Check the instances tab to see how they run.
#. Get the processed files in your ``output_tiff`` directory. In case of failure, recover the original files and check the error cause from ``output_backup``.


Cleaning
--------

To completely remove the output (needed if you want to run the same test) remember to:

* Remove the content of the outputDirName
* Remove the content of the output_backup
