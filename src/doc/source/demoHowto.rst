.. |GB| replace:: *GeoBatch*
.. |demo| replace:: *geobatch-demo*

Demo setup HOW-TO
=================

The |demo| project uses |GB| and *GDAL* to convert a bulk of raster files from almost any format to tiled GeoTIFFs with overviews.

Allowed input formats are those `supported by GDAL <http://www.gdal.org/formats_list.html>`_.


Installing Tomcat
-----------------

Download tomcat from: http://tomcat.apache.org/download-60.cgi

Uncompress it::

  $ tar -xvf apache-tomcat-6.0.35.tar.gz


Installing GDAL
---------------

Download latest FWTools from: http://home.gdal.org/fwtools/

Uncompress it::

  $ tar -xvf FWTools-linux-x86_64-3.1.0.tar.gz -C /opt/

Run installation script::

  $ cd /opt/FWTools-linux-x86_64-3.1.0/
  $ ./install.sh
  $ chmod +x fwtools*

Add the FWTools enviroment to the tomcat installation.
Edit ``bin/startup.sh`` and add a call to ``fwtools_env.sh``:

 . /opt/FWTools-linux-x86_64-3.1.0/fwtools_env.sh


Installing |GB|
---------------

Download |GB| from http://demo.geo-solutions.it/share/github/geobatch/geobatch.war and copy it into ``webapps``, under the tomcat installation.

Start tomcat via ``startup.sh`` and see it running in:

  http://localhost:8080/geobatch/

Installing |demo|
-----------------

Download |demo| zip from: https://github.com/geosolutions-it/geobatch-demo

Unzip it::

  $ unzip geosolutions-it-geobatch-demo-0dd29a9.zip

Create ``output`` and ``backup`` directories::

  $ mkdir output_tiff
  $ mkdir output_backup

Edit the |demo| flow configuration under ``geosolutions-it-geobatch-demo-0dd29a9/GEOBATCH_CONFIG_DIR/geobatch_flow_tiff.xml``. There, edit:

* ``outputDirName`` pointing to your ``output_tiff`` directory (absolute path).
* ``output_backup`` pointing to your ``output_backup`` directory (absolute path).
* ``translateExecutable`` pointing to ``/opt/FWTools-linux-x86_64-3.1.0/bin/gdal_translate``
* ``overviewExecutable`` pointing to ``/opt/FWTools-linux-x86_64-3.1.0/bin/gdaladdo``.

Finally, set the ``GEOBATCH_CONFIG_DIR`` variable in ``startup.sh``::

  export GEOBATCH_CONFIG_DIR=[...]/geosolutions-it-geobatch-demo-0dd29a9/GEOBATCH_CONFIG_DIR

Running
-------

#. Start tomcat via ``startup.sh``.
#. Go to http://localhost:8080/geobatch/flows.do You should see a stopped ``geobatch_flow_tiff``. Start it.
#. Put some raster files in ``geobatch_flow_tiff/in``.
#. Check the instances tab to see how they run.
#. Get the processed files in your ``output_tiff`` directory. In case of failure, recover the original files and check the error cause from ``output_backup``.

