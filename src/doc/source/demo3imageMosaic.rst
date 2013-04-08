.. |GB| replace:: *GeoBatch*
.. |demo| replace:: *geobatch-demo*
.. |GS| replace:: *GeoServer*
.. |IMD| replace:: ImageMosaic DEMO

|IM| Action Demonstration
=========================

|IMD| project uses |GB| to create an Image Mosaic on |GS|. 

As explained in the paragraph Image Mosaic Action you can chose from 2 types of input: An ImageMosaic command or a directory with some geotiff inside.
You can choose also if the spatial index will stored on a DBMS or a shapefile.

For this |demo| we'll use a **Mosaic Command** and we use a **shapefile** for store the spatial indexes.


Installing |demo|
-----------------

if you haven't already done, setup the ``GEOBATCH_CONFIG_DIR`` and make sure that you have setted the corresponding environment variable (see installation chapter).

Locate into |GB| deployment the folder WEB-INF/data/

Copy the folder imagemosaic and the file imagemosaic.xml and paste into your ``GEOBATCH_CONFIG_DIR``

`Download two (or many) .tiff files <https://github.com/geosolutions-it/geoserver-manager/tree/master/src/test/resources/testdata/time_geotiff>`_ for example usage.

.. note::	For a correct download for each file click on the link and in the following window click on raw button. 
	Remember to replace dot(.) with underscore(_).
	Please dont't download the file right clicking on the link and selecting save as. The downloaded file may be corrupt.

Rename the files world_2012_0001.000.tif and world_2013_0001.000.tif.
	
Save the downloaded tiff files where you want, for example /var/imagemosaic/tiff.

Then let's make the Image Mosaic Command: create a new file called cmd.xml and copy and paste in a new file the xml below.

Change the values in the brackets with your custom values:

* {PATH_TO_TIFF_FILE} *the path where are stored the tif files with wich you want to create the mosaic*
* {YOUR_IMAGE_MOSAIC_LOCATION} *the directory where will be created the mosaic*
* {IMAGEMOSAIC_DIR_BACKUP} *a directory for granules backup*

.. sourcecode:: xml
		
	<ImageMosaic>
  	  <!-- Base directory for the mosaic -->
  	  <base>{YOUR_IMAGE_MOSAIC_LOCATION}</base>

  	  <!-- Files to add or remove from the index  -->
	  <add>{PATH_TO_TIFF_FILE}\world_2012_0001.000.tif</add>
	  <add>{PATH_TO_TIFF_FILE}\world_2013_0001.000.tif</add>
	  <!-- physically delete the granules from disk
	  after removing them from the index -->
	  <deleteGranules>false</deleteGranules>
	  <!-- if and where to backup up granules when removing them. It must be an absolute path -->
	  <backupDirectory>{IMAGEMOSAIC_DIR_BACKUP}</backupDirectory>

	  <!-- ImageMosaicAction Configuration parameters -->
	  <backgroundValue>-9999</backgroundValue>
	  <outputTransparentColor></outputTransparentColor>
	  <inputTransparentColor></inputTransparentColor>
	  <allowMultithreading>true</allowMultithreading>
	  <useJaiImageRead>false</useJaiImageRead>
	  <tileSizeH>256</tileSizeH>
	  <tileSizeW>256</tileSizeW>
	  <!--NONE, REPROJECT_TO_DECLARED, FORCE_DECLARED-->
	  <projectionPolicy>NONE</projectionPolicy>
	  <styles/>

	  <!-- do we want to perform a Reset on GeoServer at the end of the process or not.  default to True.-->
	  <finalReset>false</finalReset>

	  <!-- Delay in Seconds to apply when moving files around when NFS is involved. Must be >=0-->
	  <NFSCopyWait>10</NFSCopyWait>

	  <ignoreGeoServer>false</ignoreGeoServer>

	  <timeRegex>[0-9]{4}</timeRegex>
	  <elevationRegex><![CDATA[(?<=_)(\\d{4}\\.\\d{3})]]></elevationRegex>
  

	</ImageMosaic>
	
As you can see some value contained in the command are contained also in the flow configuration (imagemosaic.xml) .

In that case those contained in the commad will override the values specified in the flow configurations. 

Only if a value is NOT specified into the command the action will take that contained in flow configuration.

For example the value for *timeregex* is different. 

The regex is much simple as that contained in the flow config. 

The time specified in the file name (only the year) is simpler than that used in ImageMosaicAction Documentation so we have to use a different regex.


Running
-------

#. Start tomcat via ``startup.sh``.
#. Go to http://localhost:8080/geobatch/flows.do You should see a started Image Mosaic Flow.
#. Copy the ImageMosaic  command and paste it into``imagemosaic/in`` directory under your GEOBATCH CONFIG DIR.
#. Check the instances tab to see how they run.
#. Access to geoserver and verify that a new ImageMosaic Store is published.
#. Open the ImageMosaic location that you have configured with <base> property into the command. 
You must see the tiff granules that compose the mosaic, 
the files elevationregex.properties and timeregex.properties that contain the regex specified into command, 
3 file .dbf, .shp, .shx that are the shapefile and a file called sample_image.

.. figure:: images/mosaic_path_full.jpg
   :align: center
