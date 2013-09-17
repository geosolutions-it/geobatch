ds2ds (Datastore-to-Datastore) Action
================

The ds2ds (DataStore to DataStore) action is used to copy a feature from a source GeoTools DataStore to an output GeoTools DataStore.

Any GeoTools DataStore (shapefiles, jdbc databases, etc.) can be used as a source or output.

The action can be used to append or replace existing data, a flag (purgeData) in configuration specifies the desired mode.

Feature typeName and attributes can be renamed, if needed. Attributes can also be projected.

A common xml format is used as input and output event. Moreover the same format can be used in the configuration file to describe (fully or partially) the source and output features.

Input
-----

The default input event is a file in XML format. The file contains the source DataStore connection parameters and other options used to describe the source.

An example of the xml input is:

.. sourcecode:: xml

	<feature>
		<!-- source datastore connection parameters -->
		<dataStore>            
		   <entry>
			  <string>dbtype</string>
			  <string>h2</string>
			</entry> 
			<entry>
			  <string>database</string>
			  <string>mem:source;DB_CLOSE_DELAY=-1</string>
			</entry>                 
		</dataStore>
		<!-- source coordinate system (optional) -->
		<crs>EPSG:4326</crs>
		<!-- source feature type name -->
		<typeName>source</typeName>
	</feature>

To simplify things we also support as an input:
 * shapefiles
 * compressed files (containing either an xml or a shapefile)

Configuration
---------------

Main element: ``ds2dsConfiguration``.

Will contain the following child elements:

* Identification parameters:
    * **serviceID**: Should be ``ShapeFileGeoServerService``.
    * **id**: An ID for this action.
    * **name**: A name for this action.
    * **description**: A description for this action.

* Source feature configuration (optional, is usually received on the input event):
    * **typeName**: typeName to read from the source DataStore
    * **crs**: coordinate system of the source feature (optional, usually read from the source)
    * **dataStore**: map of the DataStore connection parameters

* Output feature configuration (mandatory):
    * **typeName**: typeName to write on the output DataStore
    * **crs**: coordinate system of the output feature (optional, usually read from the source); a reprojection is NOT automatically done
    * **dataStore**: map of the DataStore connection parameters
	
* Options:
    * **ecqlFilter**: specify an ECQL filter to the source datastore. Just the features filtered will be copyed.
    * **purgeData**: remove existing data from the output feature, if a filter is specified remove just the features filtered. The default value is false.
    * **forcePurgeAllData**: remove ALL existing data from the output feature although a filter is specified. If this flag is set to TRUE the flag purgeData has no effect. The default value is false.
    * **attributeMappings**: attribute mappings (from output to source) for projection (see projectOnMappings) / renaming / transformation; a simple attribute name can be used a source, or an SpEl expression for attribute(s) transformation (e.g. #{MY_ATTR + 1})
    * **projectOnMappings**: if true only attribute present in attributeMappings are copied to the output feature
    * **ReprojectedCrs**: specify a CRS as EPSG code used to perform a reprojection from SourceFeature to OutputFeature. Note that this feature is affected by *Source feature* and *Output feature crs* attributes settings. If *Source-crs* is not null the projection is performed from that CRS to the projectedCRS without read CRS from feature. If *Output-crs* is not null the projection will not affected but the feature will be stored as *Output-crs* otherwise the *ReprojectedCrs* code will be used.

Configuration example:

.. sourcecode:: xml

    <Ds2dsConfiguration>
		<serviceID>Ds2dsGeneratorService</serviceID>
		<id>Ds2dsGeneratorService</id>
		<description>Ds2ds action</description>
		<name>Ds2dsConfiguration</name>
		
		<listenerConfigurations/>
		<failIgnored>false</failIgnored>
					
		<!-- Configures the output feature: mandatory -->      
		<outputFeature>
			<!-- feature typeName (schema): will be created if not
				 already present in output DataStore -->
			<typeName>OUTPUT</typeName> 
			<!-- Coordinate system EPSG code: force output feature crs,
				 if not defined the source crs is used -->
			<crs>EPSG:4326</crs>
			<!-- output GeoTools DataStore connection parameters:
				 an entry for each connection parameter  -->              
			<dataStore>                    
				<entry>
				  <string>dbtype</string>
				  <string>postgis</string>
				</entry>
				<entry>
				  <string>host</string>
				  <string>localhost</string>
				</entry>
				<entry>
				  <string>port</string>
				  <string>5432</string>
				</entry>
				<entry>
				  <string>database</string>
				  <string>postgres</string>
				</entry>
				<entry>
				  <string>user</string>
				  <string>postgres</string>
				</entry>
				<entry>
				  <string>passwd</string>
				  <string>postgres</string>
				</entry>                     
			</dataStore>
		</outputFeature>
		<!-- Configures the source feature: optional, usually the source feature
			 is received as an event. The sourceFeature can be used to fill missing
			 metadata (such as the coordinate system) if needed. -->
		<sourceFeature>   
			<!-- feature typeName (schema) to read from the source DataStore -->
			<typeName>SOURCE</typeName> 
			<!-- Coordinate system EPSG code: force input feature crs,
				 if not defined -->
			<crs>EPSG:4326</crs>
			<!-- source GeoTools DataStore connection parameters:
				 an entry for each connection parameter  -->             
			<dataStore>                    
				<entry>
				  <string>dbtype</string>
				  <string>postgis</string>
				</entry>
				<entry>
				  <string>host</string>
				  <string>localhost</string>
				</entry>
				<entry>
				  <string>port</string>
				  <string>5432</string>
				</entry>
				<entry>
				  <string>database</string>
				  <string>postgres</string>
				</entry>
				<entry>
				  <string>user</string>
				  <string>postgres</string>
				</entry>
				<entry>
				  <string>passwd</string>
				  <string>postgres</string>
				</entry>                     
			</dataStore>
		</sourceFeature>
		<!-- do a projection of the input feature using the attributeMappings  
			 property: only the attributes defined in mappings are copied to
			 the output feature -->
		<projectOnMappings>true</projectOnMappings>
		<!-- attribute mappings from output names to source names
			 permits attribute renaming  -->
		<attributeMappings>
			<entry>
			  <string>NEWNAME</string>
			  <string>OLDNAME</string>
			</entry>
            <entry>
			  <string>MY_ATTR</string>
			  <string>#{MY_ATTR + 1}</string>
			</entry>
		</attributeMappings>
		<!-- remove data in the output feature before importing the new one -->
		<purgeData>true</purgeData>
		<ecqlFilter>LAND_KM < 3000 OR STATE_NAME = 'California'</ecqlFilter>
			
	</Ds2dsConfiguration>

Output
------

The event is an XML file in the already described common format, describing the output feature produced.

An example of the output file is:

.. sourcecode:: xml

	<feature>
		<!-- output datastore connection parameters -->
		<dataStore>            
		   <entry>
			  <string>dbtype</string>
			  <string>h2</string>
			</entry> 
			<entry>
			  <string>database</string>
			  <string>mem:source;DB_CLOSE_DELAY=-1</string>
			</entry>                 
		</dataStore>
		<!-- output coordinate system (optional) -->
		<crs>EPSG:4326</crs>
		<!-- output feature type name -->
		<typeName>output</typeName>
	</feature>


