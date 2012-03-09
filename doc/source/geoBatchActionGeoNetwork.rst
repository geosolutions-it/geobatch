
**GeoNetwork Action**
=======================================================================

The GeoNetwork Action is used to integrate with a GeoNetwork instance.

*Insert operation*
-----------------------------------------------------------------------

The GeoNetwork Action allows you to insert a new metadata into the catalog and set the related privileges.

*Update and delete operation*
-----------------------------------------------------------------------

At the moment the only available operation is about inserting a new metadata entry in the catalog.
Next operations to be implemented will be the ''delete'' and the ''update'' operations.

*Consumed events*
-----------------------------------------------------------------------

The GeoNetwork Action consumes a FileSystemEvent.

The event should reference an XML file, containing either (see the *onlyMetadataInput* parameter):

* a valid metadata file, or

* a full GeoNetwork request for the metadata.insert 


*Configuration*
------------------------------------------------------------------------

The Action needs some data to connect to the GeoNetwork instance:

* geonetworkServiceURL: 

	* GeoNetwork's URL: :: 
			
			http://localhost:8080/geonetwork

* loginUsername: credential to auth in GeoNetwork

* loginPassword: credential to auth in GeoNetwork

Then some info to describe the kind of input file: ::

 onlyMetadataInput: true | false
   
* true: the file is a pure metadata info: it will be enclosed in a proper request to the service metadata.insert

* false: the file is a full request to be sent to the service metadata.insert

The *request* packet to be sent to GeoNetwork also contains some more *meta-metadata* that describe how the metadata has to be stored and categorized.

If *onlyMetadataInput* is *true*, then these parameters have to be specified in the Action configuration.

Such params are described in http://geonetwork-opensource.org/manuals/2.6.3/developer/xml_services/metadata_xml_services.html#insert-metadata-metadata-insert documentation of the *metadata.insert* operation:

 * group (mandatory): Owner group identifier for metadata

 * isTemplate: indicates if the metadata content is a new template or not. Default value: “n” *(not handled by this Action)*

 * title: Metadata title. Only required if isTemplate = “y” *(not handled by this Action)*

 * category (mandatory): Metadata category 
	
	Use ::
		
		_none_ 

	value to don’t assign any category

 * styleSheet (mandatory): Stylesheet name to transform the metadata before inserting in the catalog 

	Use :: 
		
		_none_ 
	
	value to don’t apply any stylesheet

 * validate: Indicates if the metadata should be validated before inserting in the catalog 

	Values :: 
		
		on, off (default)

A metadata added this way will only be visible to the user who inserted it (i.e. the user identified by the loginUsername previously defined).
 
In order to have other users access this metadata entry, you may want to define the access privileges to the various geonetwork groups. 

You may define a set of operations for each group separately, as described in http://geonetwork-opensource.org/manuals/2.6.3/developer/xml_services/metadata_xml_services.html#metadata-administration-services documentation of the *metadata.admin* operation. 

You'll have to associate to the groupId the set of operations allowed to the group. 

Each operation privilege is assigned a single digit: ::

  0: view
  1: download
  2: editing
  3: notify
  4: dynamic
  5: featured

Each set of privileges is simply indentified with the set of related digits; for instance "02" means view+edit.


*Sample configuration snippet* 
-------------------------------------------------------------------------- 
::


	<GeoNetwork>
	   <serviceID>GeonetworkGeneratorService</serviceID>
	   
	   ... other configuration fixed elements...

	   <!-- geonetwork action's specific params -->
	   <geonetworkServiceURL>http://localhost:8080/geonetwork</geonetworkServiceURL>
	   <loginUsername>admin</loginUsername>
	   <loginPassword>admin</loginPassword>
	   <onlyMetadataInput>true</onlyMetadataInput>

	   <!-- parameters required by geonetwork, in case onlyMetadataInput==true -->
	   <group>1</group>
	   <category>_none_</category>
	   <styleSheet>_none_</styleSheet>
	   <validate>false</validate>

	   <!-- optional privileges -->
	   <privileges>
			<grant> <!-- group 42 can do anything -->
				<group>42</group>
				<ops>012345</ops>
			</grant>
			<grant> <!-- only view and download allowed for group 1999 -->
				<group>1999</group>
				<ops>01</ops>
			</grant>
	   </privileges>

	</GeoNetwork>  


*Other notes*
 
	* Before implementing the logic for updating metadata, a change should be made in GeoNetwork in order to handle the *version* parameter properly.


*References*
 
	* http://geonetwork-opensource.org/ GeoNetwork opensource project page 

	* http://geonetwork-opensource.org/manuals/2.6.3/developer/xml_services/metadata_xml_services.html#insert-metadata-metadata-insert Metadata insert operation
