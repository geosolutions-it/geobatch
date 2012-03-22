


=================
Welcome to GeoBatch Users documentation Pages!
=================

Introduction
====================================
In order to properly setup a flow into GeoBatch you must configure basically 4 nodes into your flow.xml and are:

</EventConsumerConfiguration>
=====================================

.. sourcecode:: xml
 
	<EventConsumerConfiguration>
	
			<id>id</id>
			<description>here a description</description>
			<name>path_to_your_flow</name>
			
			<workingDirectory>directory where your flow produce his outputs/</workingDirectory>
			<performBackup>false</performBackup>
		
			
<ScriptingConfiguration>
=====================================

.. sourcecode:: xml

			**<ScriptingConfiguration>
				<id>ScriptingService</id>
				<description>Groovy Script</description>
				<name>name of your GroovyScript</name>
				
				<listenerId>GeoTIFFStatusActionLogger0</listenerId>
				<listenerId>GeoTIFFActionLogger0</listenerId>
						
				<workingDirectory>relative_or_absolute_path_to_your_flow_/</workingDirectory>
				
				<serviceID>ScriptingService</serviceID>

				<language>groovy or other</language>
				<scriptFile>path/to/your/groovyScript.groovy</scriptFile>
				<properties>	  
					  <!-- Where to put output data -->
					  <entry>
						<string>outputDirName</string>
						<string>path\to\your\outputDir\GEOBATCH_DATA_DIR\flow\out</string>
					  </entry>
					  <entry>
						<!-- Where to put a backup copy -->
						<string>backupDirName</string>
						<string>path\to\your\backupDir\GEOBATCH_DATA_DIR\flow\backup</string>
					  </entry>
					  
					  
					  <!-- xml template for translate-->
					  <entry>
						<string>translateTemplateName</string>
						<!--relative to the working dir-->
						<string>config/gdal_translate.xml</string>
					  </entry>
					  <entry>
						<string>defaultScriptName</string>
						<string>config/gdal_translate.xml</string>
					  </entry>
					  <!--entry>
						<string>translateErrorFile</string>
						<string>your_flow/config/translate_errorlog.txt</string>
					  </entry-->
					  <entry>
						<string>translateExecutable</string>
						<string>/path/to/yourExecutable/gdal_translate</string>
					  </entry>
					  <entry>
						<string>translateXslName</string>
						<string>config/gdal_translate.xsl</string>
					  </entry>
					  
					  <!-- xml template for overview -->
					  <entry>
						<string>overviewTemplateName</string>
						<!--relative to the working dir-->
						<string>config/gdaladdo.xml</string>
					  </entry>
					  <!--entry>
						<string>overviewErrorFile</string>
						<string>your_flow/config/overview_errorlog.txt</string>
					  </entry-->
					  <entry>
						<string>overviewExecutable</string>
						<string>/path/to/yourExecutable/gdaladdo</string>
					  </entry>
					  <entry>
						<string>overviewXslName</string>
						<string>config/gdaladdo.xsl</string>
					  </entry>
				</properties>
			</ScriptingConfiguration>

<GeoServerActionConfiguration>
=====================================

.. sourcecode:: xml

			<!-- in this case the first action is to publish directly into GeoServer -->
			<GeoServerActionConfiguration>
				<serviceID>GeotiffGeoServerService</serviceID>
				<id>geotiff</id>
				<description>Action to ingest GeoTIFF on GeoServer</description>
				<name>geotiff action</name>

				<listenerId>GeoTIFFStatusActionLogger0</listenerId>
				<listenerId>GeoTIFFActionLogger0</listenerId>

				<workingDirectory>relative_or_absolute_path_to_your_flow_/</workingDirectory>
				
				<!-- cordinate reference system -->
				<crs>EPSG:4326</crs>

				<envelope/>
				<dataTransferMethod>EXTERNAL</dataTransferMethod>
				<geoserverPWD>geoserver</geoserverPWD>
				<geoserverUID>admin</geoserverUID>
				<geoserverURL>http://localhost:9090/geoserver</geoserverURL>
				<defaultNamespace>it.geosolutions</defaultNamespace>

				<wmsPath>/</wmsPath>
				<defaultStyle>raster</defaultStyle>
				<styles />
			**</GeoServerActionConfiguration>

	**</EventConsumerConfiguration>


<EventGeneratorConfiguration>
====================================

.. sourcecode:: xml

	**<EventGeneratorConfiguration>
		<wildCard>*.*</wildCard>
		<watchDirectory>path/whereFlow/isInListening/in</watchDirectory>
		<osType>OS_UNDEFINED</osType>
		<eventType>FILE_ADDED</eventType>

                  <!-- CRON: every 30 secs -->
                  <interval>*/10 * * * * ?</interval>

		<id>id</id>
		<serviceID>fsEventGeneratorService</serviceID>
		<description>description</description>
		<name>nameOfyourFlow</name>
	**</EventGeneratorConfiguration>










