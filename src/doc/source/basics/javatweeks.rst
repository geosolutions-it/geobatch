.. |GB| replace:: *GeoBatch*
.. |GS| replace:: *GeoServer*
.. |GBCD| replace:: **GB conf dir**
.. |GBTD| replace:: **GB tmp dir**

.. _`jtweeks`:

Environment Variables
=======================

In this section are listed the suggested environment variable to set in order to run |GB|, some of that are related to the java options other are related to geotools/geobatch.

Using tomcat the administrator should append each of this variable with its value to the ``JAVA_OPTS`` environment variable, tipically declared in the ``setenv.sh`` (or ``setenv.bat`` on windows systems) script.

-Xms
---------------

.. list-table::
   :widths: 10 30
   :header-rows: 0

   * - Description
     - The initial Heap size. Note that if Xms == Xmx the Heap size of the java process won't be resized at runtime. Usually could be a good choice for keep simple the application profiling process.
   * - Value
     - number of MegaBytes (m) to set
   * - Example
     - -Xms2048m *NOTE* that there isn't an equals (=) symbol between variable and its value

	 
-Xmx
------------------

.. list-table::
   :widths: 10 30
   :header-rows: 0

   * - Description
     - The maximum heap size. If you see a ``Java.out of Memory error: Heap space`` error on the logs this value must be increased or memory leaks are presents in some custom actions.
   * - Value
     - number of MegaBytes (m) to set. Typical values are difficult to identify: it depends on the number of flows/actions are executed at same time and which type of computation is performed by the flow. Usually a range between 512MB - 2048MB.
   * - Example
     - -Xmx2048m 

	 
-XX:MaxPermSize
--------------------------
	 
.. list-table::
   :widths: 10 30
   :header-rows: 0

   * - Description
     - The maximum permgen size.
   * - Value
     - number of MegaBytes (m) to set. 128 or 256 MB usually are values large enough for most actions.
   * - Example
     - -XX:MaxPermSize=256m

-server
------------------
	 
.. list-table::
   :widths: 10 30
   :header-rows: 0

   * - Description
     - Force the virtual machine to be executed in server mode and enable `JIT Compilation <http://www.oracle.com/technetwork/java/whitepaper-135217.html#3>`_. 
   * - Value
     - ( No values are needed, it's just a flag )
   * - Example
     - -server

-Duser.timezone
---------------------------
	 
.. list-table::
   :widths: 10 30
   :header-rows: 0

   * - Description
     - force the default time zone regardless the timezone setted on the machine
   * - Value
     - **GMT**
   * - Example
     - -Duser.timezone=GMT

-Dorg.geotools.referencing.forceXY
-----------------------------------------------
	 
.. list-table::
   :widths: 10 30
   :header-rows: 0

   * - Description
     - Set the axis order. See `geotools related page for more information <http://docs.geotools.org/latest/userguide/library/referencing/order.html>`_
   * - Value
     - **true** or **false**
   * - Example
     - -Dorg.geotools.referencing.forceXY=true	 

-DGEOBATCH_CONFIG_DIR
----------------------------------
	 
.. list-table::
   :widths: 10 30
   :header-rows: 0

   * - Description
     - Specify an external |GB| Config Directory see
   * - Value
     - An absolute path of the GEOBATCH_CONFIG_DIR see :ref:`datadir` section
   * - Example
     - -DGEOBATCH_CONFIG_DIR=/var/geobatch/data	 

-DGEOBATCH_TMP_DIR
-----------------------------
	 
.. list-table::
   :widths: 10 30
   :header-rows: 0

   * - Description
     - Specify an external |GB| Temp Directory see :ref:`datadir` section
   * - Value
     - An absolute path of the GEOBATCH_TMP_DIR
   * - Example
     - -DGEOBATCH_TMP_DIR=/var/geobatch/tmp