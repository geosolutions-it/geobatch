.. |GB| replace:: *GeoBatch*

Installing and Running 
===========================

|GB| is distributed as a java web application archive (``war`` file), to be used within a Java servlet container.


Prerequisites: Java and Tomcat
------------------------------

|GB| requires a Java Development Kit version 6 (jdk6) and a Java Servlet container installed in the system.

|GB| should works with all jdk6 from different vendors but the suggested one is the **Oracle Java SE 6**.

.. warning::
	
	Usually in most Linux distributions an **OpenJDK** is installed by default. Although |GB| is compatible as well the performances are lower.
	Download and install **Java SE Development Kit 6u45** from the `Java archive pages <http://www.oracle.com/technetwork/java/javasebusiness/downloads/java-archive-downloads-javase6-419409.html>`_

|GB| has been tested mostly with Jetty and with `Apache Tomcat 6 <http://tomcat.apache.org/download-60.cgi>`_ which is strongly recommended for production usage.


Deploy and running with Tomcat
-------------------------------

Download `geobatch.war <http://geobatch.geo-solutions.it/download/latest/geobatch.war>`_ and copy it to the ``webapps`` directory in tomcat. Tomcat should unpack the web archive and automatically set up and run |GB|.


The configuration directory
----------------------------

The configuration directory is where all |GB| flow definitions and other configuration files are placed. |GB| comes with a default configuration directory with a collection of sample flow configurations, that will be located under ``WEB-INF/data`` in |GB|'s installation.

In production deployment, it is **STRONGLY** recommended to copy the contents of the configuration directory outside the installation directory, and use the ``GEOBATCH_CONFIG_DIR`` environment variable to tell |GB| where to find it.

See the :ref:`datadir` section for a complete description of the |GB| config directories and their management.


The Users Databases
--------------------

|GB| uses an embedded database to store user credentials and custom flows and FTP configurations. The locations of these databases can be customized. However any |GB| administrator should be aware of this resources location.

See the :ref:`databases` section to read about the details of Database management.


Upgrade |GB| (Tomcat)
-----------------------

In order to upgrade |GB| to a newer version:

* Stop the running Tomcat instance
* Remove the previous .war package and the extracted folder inside ``webapps`` folder.
* Perform a backup of the GeoBatch config dir
* Clean the Tomcat ``work`` and ``temp`` directories 
* Restart Tomcat
