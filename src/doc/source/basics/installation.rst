.. |GB| replace:: *GeoBatch*

Installing and Running 
===========================

|GB| is distributed as a java web application archive (``war`` file), to be used within a Java servlet container.


Prerequisites: Java and Tomcat
------------------------------

|GB| requires `Oracle Java SE 6 <http://www.oracle.com/technetwork/java/javase/downloads/index.html>`_ to be installed in your system, and a servlet container. |GB| has been tested mostly with Jetty and with `Apache Tomcat 6 <http://tomcat.apache.org/download-60.cgi>`_ whichh is strongly recommended for production usage.


The configuration directory
---------------------------

The configuration directory is where all |GB| flow definitions and other configuration files are placed. |GB| comes with a default configuration directory with a collection of sample flow configurations, that will be located under ``WEB-INF/data`` in |GB|'s installation.

In production, it is recommended to copy the contents of the configuration directory outside the installation directory, and use the ``GEOBATCH_CONFIG_DIR`` environment variable to tell |GB| where to find it.

If you set the ``GEOBATCH_CONFIG_DIR`` the configuration into ``WEB-INF/data`` will be discarded.

To set the ``GEOBATCH_CONFIG_DIR``, set it as one environment variable in your operating system, *or* run tomcat with ``-D`` option to define it as a java system property.

  
Deploy and running
------------------------

Download `geobatch.war <http://geobatch.geo-solutions.it/download/latest/geobatch.war>`_ and copy it to the ``webapps`` directory in tomcat. Tomcat should unpack the web archive and automatically set up and run |GB|.


Configuration directory structure
.................................

The configuration directory must contain these files/directory:

* ``catalog.xml``

This file is Mandatory and isn't created automatically at |GB| startup. You have to create it. 
Below there is a basic example:

.. code-block:: xml

	<?xml version="1.0" encoding="UTF-8"?>
	<CatalogConfiguration>
		<id>catalog</id>
		<description>descriptionFileBasedCatalogConfiguration</description>
		<name>nameFileBasedCatalogConfiguration</name>
	</CatalogConfiguration>

* ``settings`` directory, if doesn't exist this directory is autogenerated at startup.

* Flow configuration ``*.xml`` files.

* Action configuration directories, following the hierarchy explained below, unless specified otherwise in the configuration file.

* ``temp`` dir, unless explicitly specified otherwise (see `The temporary directory`_) this directory is autogenerated at startup.

Each action needs a configuration dir. Its default location is ``GEOBATCH_CONFIG_DIR/FLOW_ID/ACTION_ID``. Custom locations can be specified inside the flow configuration files:

* At **flow level**, using the ``<overrideConfigDir>`` element. It will point to the directory containing all the configuration directories for all the actions in the flow. If the path specified in ``<overrideConfigDir>`` is relative, it will be placed under ``GEOBATCH_CONFIG_DIR``.

* At **action level**. Each action may override its own config dir location, using the ``<overrideConfigDir>`` element. If a relative path is given, it will be placed under its flow-level dir specification.


The temporary directory
-----------------------

The place where |GB| Actions will create their temporary files during execution, if not specified otherwise, will be under ``temp`` dir within ``GEOBATCH_CONFIG_DIR``.

The ``GEOBATCH_TEMP_DIR`` environment/system variable can be used to specify another location, the same way as ``GEOBATCH_CONFIG_DIR``.

.. note:: Make sure that |GB| (the running tomcat user) will have write permisions into the temporary directory.


Configuring Users database
--------------------------

|GB| uses by default an embedded SQL database to store Users and Ftp Accounts Credentials and their custom configurations.

|GB| Users and Ftp Account are stored in two distinct database. The DBMS Engine is `H2 <http://www.h2database.com/html/main.html>`_ .

By default |GB| create the temporary file for persist the database into the **settings/database** dir located in the **geobatch config dir**.

The usage of this default setting is good also for Production environment, the important thing is remember to setup the geobatch config dir outside geobatch deployment.

However if you want to specifing another location for storing the DB you can create and configure the gb_database.properties file. Here is an example::

	dataSource-gb-users.jdbcUrl=jdbc:h2:[absolute_path]/gbusers
	dataSource-gb-ftp-server.jdbcUrl=jdbc:h2:[absolute_path]/ftpusers

In the properties file will must be specified two location, one for each database; using the previous template you have only to substitute [absolute_path] with the absolute path where you want save the data.

After |GB| startup 4 files will be created: ftpusers.h2.db, ftpusers.lock.db, gbusers.h2.db, gbusers.lock.db .

Where can I create database.properties file?
............................................

There are 3 possible location where you can put the file:
	
1) In a custom path specified setting the env variable DATABASE_CONFIG_FILE after run |GB| .

2) In the home of the user running |GB|.

The order of this list isn't random, in fact it indicate the priority of this location: if you put two different version of database.properties in path 1) and 2) the file in path 1) will be override that one in 2).

Remember that if an error occurred in this process, |GB| try to store the db into the TMP dir of the user.

The logging system
------------------

For specifing any custom path for save the geobatch.log set the environment variable GEOBATCH_LOG.

For example::

	-DGEOBATCH_LOG=/var/geobatchconfig/logs

|GB| uses `log4j <http://logging.apache.org/log4j/>`_ for logging, and by default is logging at ``INFO`` level, and output will be rolled into ``logs/geobatch.log``.

To change logging setup, edit ``WEB-INF/log4j.xml`` file. Please refer to `log4j's manual <http://logging.apache.org/log4j/1.2/manual.html>`_ for details.

Maintenance
-----------

|GB| will automatically remove the old temporal directories, but some old directories could remain undeleted in case of unexpected tomcat restart. Check occasionally for temporal directory size, and for log file sizes, and clean it if necessary.
