.. |GB| replace:: *GeoBatch*
.. |GS| replace:: *GeoServer*
.. |GH| replace:: *GitHub*

.. _`restinterface`:

Rest Interface Overview
=========================

The |GB| rest interface allows third-part client softwares to start, stop, pause and get execution informations from a running |GB| flow.

The |GB| **GUI** can be used to perform a monitoring of the flows execution and for users management by an user or an administrator. 

The |GB| **REST interface** expose the GUI functionality through a programmable API. It allows developers to control |GB| from external applications. In this way is possible couple different flows executions or embedd the monitoring task triggering external events inside external applications.

.. warning::
	
	In order to build |GB| with REST support the maven profile ``-Prest`` must be activated.

For an high level introduction to the REST architectures see the `related wikipedia page <http://en.wikipedia.org/wiki/Representational_state_transfer>`_

The Web Service framework used to create the REST resources is `Apache CXF <http://cxf.apache.org/>`_.

Maven modules
---------------------------

The |GB| maven modules related to REST implementation are:

* **gb-rest-api** The annotated interface of the services
* **gb-rest-impl** The API implementation
* **gb-rest-client** A useful java client to interacts with the exposed services. This componebt should be imported as dependency into the client software that wants to interacts with |GB| via REST calls. 
* **gb-rest-test** A module used for a mock-testing the services: load a web application that expose the API interface with a dummy implementation.

Access to the Interface
---------------------------

The base url for acces to all services is::
	
	http://<host>:<port>/<geobatch_instancename>/rest

for example::

	http://localhost:8081/geobatch/rest
	
In order to Invoke a service one of the following **Operation** URL must be concatenated to the base URL. 

REST Operations
-------------------

There are 2 main categories of Operations: **Flow Operations** and **Consumer Operations**. 

Next 2 sections will describe the interface of each flow listing HTTP **HTTP Method** to use to invoke them, the **path**, the **Return type** and the **HTTP Error code** returned.

.. note::

	The sequence used to list all operation in the next sections isn't random: often in order to invoke the *n-* operation an information provided by the *n-i-* operation is required.

Flows Operations
------------------

List all Flows
,,,,,,,,,,,,,,,,

Returns a List of flow that are available on the geobatch running instance. Each flow is described by a flowId, flowName and flowDescription.

.. list-table::
   :widths: 20 20 20 20
   :header-rows: 1

   * - Method
     - Path
     - Return type
     - Errors
   * - GET 
     - ``/flows``
     - APPLICATION_XML
     - 500
	 
Get all info about a Flow
,,,,,,,,,,,,,,,,,,,,,,,,,,,,

Returns all the useful informations about a flow.

.. list-table::
   :widths: 20 20 20 20
   :header-rows: 1

   * - Method
     - Path
     - Return type
     - Errors
   * - GET
     - ``/flows/{flowid}``
     - APPLICATION_XML
     - 404, 500	 
	 
Run a Flow uploading a file
,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

Starts a flow copying the data provided into the watch dir of the flow.

.. list-table::
   :widths: 20 20 20 20
   :header-rows: 1

   * - Method
     - Path
     - Return type
     - Errors
   * - POST
     - ``/flows/{flowid}/run``
     - TEXT_PLAIN
     - 400, 500	 

Run a Flow providing a file list
,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

Starts a flow using the file list provided, the info param contains one or more filepaths that will be used to create the FileSystemEvents.

Since many actions are used to delete or manipulate the input files, the listed files will be copied in a work directory and the process will be performed on the copied files.
To prevent filename clash when copying all the files, all the file should have different names.
Input files will not be renamed since the filename could have some extra info encoded.

.. list-table::
   :widths: 20 20 20 20
   :header-rows: 1

   * - Method
     - Path
     - Return type
     - Errors
   * - POST
     - ``/flows/{flowid}/runlocal``
     - TEXT_PLAIN
     - 400, 500
	 
Get flow consumers
,,,,,,,,,,,,,,,,,,,,,

Returns all the consumers (with any state) for a given flow. Each consumer is described by its status, uuid and startDate attributes.


.. list-table::
   :widths: 20 20 20 20
   :header-rows: 1

   * - Method
     - Path
     - Return type
     - Errors
   * - GET
     - ``/flows/{flowId}/consumers``
     - APPLICATION_XML
     - 404, 500
	 
	 
	 
	 
	 
	 
	 
Consumer Operations
--------------------

Consumer Status
,,,,,,,,,,,,,,,,

Returns all the useful informations about a consumer.

.. list-table::
   :widths: 20 20 20 20
   :header-rows: 1

   * - Method
     - Path
     - Return type
     - Errors
   * - GET
     - ``/consumers/{consumerid}/status``
     - APPLICATION_XML
     - 404
	 
Consumer Log
,,,,,,,,,,,,,,

Returns the status log of the consumer.

.. list-table::
   :widths: 20 20 20 20
   :header-rows: 1

   * - Method
     - Path
     - Return type
     - Errors
   * - GET
     - ``/consumers/{consumerid}/log``
     - TEXT_PLAIN
     - 404
	 
Consumer pause
,,,,,,,,,,,,,,

Pause a running consumer.

.. list-table::
   :widths: 20 20 20 20
   :header-rows: 1

   * - Method
     - Path
     - Return type
     - Errors
   * - PUT
     - ``/consumers/{consumerid}/pause``
     - void
     - 404
	 
Consumer Resume
,,,,,,,,,,,,,,,,,

Resume a paused consumer.

.. list-table::
   :widths: 20 20 20 20
   :header-rows: 1

   * - Method
     - Path
     - Return type
     - Errors
   * - PUT
     - ``/consumers/{consumerid}/resume``
     - void
     - 404
	 
Consumer Deletion
,,,,,,,,,,,,,,,,,,,

Delete a consumer if it isn't in an active state.

.. list-table::
   :widths: 20 20 20 20
   :header-rows: 1

   * - Method
     - Path
     - Return type
     - Errors
   * - PUT
     - ``/consumers/{consumerid}/clean``
     - void
     - 404, 400