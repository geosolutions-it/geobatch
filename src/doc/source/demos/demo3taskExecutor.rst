.. |GB| replace:: *GeoBatch*
.. |demo| replace:: *geobatch-demo*

Preprocessing GeoTiff files with the TaskExecutor 
=================================================

The |demo| project uses |GB| and *GDAL* to convert a bulk of raster files from almost any format to tiled GeoTIFFs with overviews.

Allowed input formats are those `supported by GDAL <http://www.gdal.org/formats_list.html>`_.


NOTE: To be able to run this flow you need to download and install GDAL.

Installing |demo|
-----------------

Download |demo| zip from: https://github.com/geosolutions-it/geobatch-demo/archive/demo-v1.0.0.zip

Unzip it::

  $ unzip demo-*.zip

Create ``output`` and ``backup`` directories::

  $ mkdir output_tiff
  $ mkdir output_backup

Edit the |demo| flow configuration under ``${geobatch-demo}/GEOBATCH_CONFIG_DIR/geobatch_flow_tiff.xml``. There, edit:

* ``outputDirName`` pointing to your ``output_tiff`` directory (absolute path).
* ``output_backup`` pointing to your ``output_backup`` directory (absolute path).
* ``translateExecutable`` pointing to ``${GDAL_HOME}/bin/gdal_translate``
* ``overviewExecutable`` pointing to ``${GDAL_HOME}/bin/gdaladdo``.

Finally, set the ``GEOBATCH_CONFIG_DIR`` variable in ``startup.sh``::

  export GEOBATCH_CONFIG_DIR=${geobatch-demo}/GEOBATCH_CONFIG_DIR

Running
-------

#. Start tomcat via ``startup.sh``.
#. Go to http://localhost:8080/geobatch/flows.do You should see a stopped ``geobatch_flow_tiff``. Start it.
#. Put some raster files in ``geobatch_flow_tiff/in``.
#. Check the instances tab to see how they run.
#. Get the processed files in your ``output_tiff`` directory. In case of failure, recover the original files and check the error cause from ``output_backup``.

