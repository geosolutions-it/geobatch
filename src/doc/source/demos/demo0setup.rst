.. _setup_demo:

Setup the Demo
==============

Into the demo zip file you can find tree folders:

* GEOBATCH_CONFIG_DIR - contains the flow configurations
* data - contains the data used for the demo
* geobatch - is the geobatch web application

Environment
-----------

Here is a list of recommended versions and programs to use with geobatch:

* Oracle JRE 6
* Tomcat 6 (not needed for the demo, will use jetty)
* GeoServer 2.x
* Gdal >1.8

Run with GeoServer
------------------

Most of our demo require a running geoserver instance on the 8080 port (which is the standard), so GeoBatch demo will run on the 8081 port (to change this modify the jetty.properties file).

* GeoBatch - http://localhost:8081/geobatch/ (with default user: admin, pass: admin)


* GeoServer - http://localhost:8080/geoserver/ (with default user: admin, pass: geoserver)

To change this, please check the flow configuration under ``/GEOBATCH_CONFIG_DIR/FLOW_NAME.xml``:

* ``geoserverURL``     The geoserver URL.
* ``geoserverPWD``     The geoserver password.
* ``geoserverUID``     The geoserver username.

Setup config dir
----------------

Before you can start geobatch (we will not reload configuration at runtime) you have to properly configure some absolute path into the flows configuration located under the GEOBATCH_CONFIG_DIR.

Since this is dependent from the flow you want to run, here you find a list of all the available demo, please enter the demo and configure the flow accordingly to the specification.

.. toctree::
   :glob:

   demo*

