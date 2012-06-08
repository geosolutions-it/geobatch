.. |GB| replace:: *GeoBatch*

Installing and Running |GB|
===========================

|GB| is distributed as a standalone java web application archive (``war`` file), to be used within a Java servlet container.


Prerequisites: Java and Tomcat
------------------------------

|GB| requires `Oracle Java SE 6 <http://www.oracle.com/technetwork/java/javase/downloads/index.html>`_ or newer to be installed in your system, and a servlet container. |GB| has been tested with `Apache Tomcat 6 <http://tomcat.apache.org/download-60.cgi>`_ and its use is strongly recommended.


The configuration directory
---------------------------

The configuration directory is where all |GB| flow definitions and other configuration files are placed. |GB| comes with a default configuration directory with a collection of sample flow configurations, that will be located under ``WEB-INF/data`` in |GB|'s installation.

In production, it is recommended to copy the contents of the configuration directory outside the installation directory, and use the ``GEOBATCH_CONFIG_DIR`` environment variable to tell |GB| where to find it.

To set the ``GEOBATCH_CONFIG_DIR``, set it as one environment variable in your operating system, *or* run tomcat with ``-D`` option to define it as a java system property.


Windows
.........

Download tomcat from ` here http://tomcat.apache.org/download-60.cgi` and extract the archive into C:\\Program Files

Create the directory C:\\GEOBATCH_CONFIG

Create a file named setenv.bat in C:\\Program Files\\apache-tomcat-6.0.35\\bin\\

Open it and edit the file specifying the geobatch config dir ::

  set GEOBATCH_CONFIG_DIR=c:\GEOBATCH_CONFIG



Linux
.....

Download tomcat from ` here http://tomcat.apache.org/download-60.cgi` and extract the archive into /xxx

Create the directory /opt/GEOBATCH_CONFIG_DIR

Create a file named setenv.sh in xxx/apache-tomcat-6.0.35/bin/

Open it and edit the file specifying the geobatch config dir ::

To apply this value every time Tomcat is started, you can set the ``GEOBATCH_CONFIG_DIR`` as a Java system property. Edit the file ``bin/setclasspath.sh`` under the root of the Tomcat installation. Specify the GEOBATCH_CONFIG_DIR system property by setting the CATALINA_OPTS variable using the ``-D`` option::

  CATALINA_OPTS="-DGEOBATCH_CONFIG_DIR=/opt/GEOBATCH_CONFIG_DIR $CATALINA_OPTS"

  
Deploy and running
------------------------

Download `geobatch.war <demo.geo-solutions.it/share/github/geobatch/geobatch.war>`_ and copy it to the ``webapps`` directory in tomcat. Tomcat should unpack the web archive and automatically set up and run |GB|.


Configuration directory structure
.................................

The configuration directory must contain:

* ``catalog.xml``.
* ``settings`` directory.
* Flow configuration ``*.xml`` files.
* Action configuration directories, following the hierarchy explained below, unless specified otherwise in the configuration file.
* ``temp`` dir, unless explicitly specified otherwise (see `The temporal directory`_).

Each action needs a configuration dir. Its default location is ``GEOBATCH_CONFIG_DIR/FLOW_ID/ACTION_ID``. Custom locations can be specified inside the flow configuration files:

* At **flow level**, using the ``<overrideConfigDir>`` element. It will point to the directory containing all the configuration directories for all the actions in the flow. If the path specified in ``<overrideConfigDir>`` is relative, it will be placed under ``GEOBATCH_CONFIG_DIR``.

* At **action level**. Each action may override its own config dir location, using the ``<overrideConfigDir>`` element. If a relative path is given, it will be placed under its flow-level dir specification.


The temporary directory
----------------------

The place where |GB| Actions will create their temporary files during execution, if not specified otherwise, will be placed in ``temp`` dir under ``GEOBATCH_CONFIG_DIR``.

The ``GEOBATCH_TEMP_DIR`` environment/system variable can be used to specify another location, the same way as ``GEOBATCH_CONFIG_DIR``.

.. note:: Make sure that |GB| (the running tomcat user) will have write permisions into the temporary directory.


The logging system
------------------

|GB| uses `log4j <http://logging.apache.org/log4j/>`_ for logging, and by default is logging at ``INFO`` level, and output will be rolled into ``logs/geobatch.log``.

To change logging setup, edit ``WEB-INF/log4j.xml`` file. Please refer to `log4j's manual <http://logging.apache.org/log4j/1.2/manual.html>`_ for details.


Manteinance
-----------

|GB| will automatically remove the old temporal directories, but some old directories could remain undeleted in case of tomcat restart. Check occasionally for temporal directory size, and for log file sizes, and clean if necessary.

