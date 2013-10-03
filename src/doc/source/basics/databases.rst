.. |GB| replace:: *GeoBatch*
.. |GS| replace:: *GeoServer*
.. |GBCD| replace:: **GB conf dir**
.. |GBTD| replace:: **GB tmp dir**

.. _`databases`:

Configuring Users database
=============================

|GB| uses by default an embedded SQL database to store Users and Ftp Accounts Credentials and their custom configurations.

|GB| Users and Ftp Account are stored in two distinct database. The DBMS Engine is `H2 <http://www.h2database.com/html/main.html>`_ .

By default |GB| create the temporary file for persist the database into the **settings/database** dir located in the **geobatch config dir**.

The usage of this default setting is a **good choice also for Production environments**, the important thing is remember to setup the geobatch config dir outside geobatch deployment.

However if you want to specifing another location for storing the DB you can create and configure the gb_database.properties file. Here is an example::

	dataSource-gb-users.jdbcUrl=jdbc:h2:[absolute_path]/gbusers
	dataSource-gb-ftp-server.jdbcUrl=jdbc:h2:[absolute_path]/ftpusers

In the properties file will must be specified two location, one for each database; using the previous template you have only to substitute [absolute_path] with the absolute path where you want save the data.

After |GB| startup 4 files will be created: ftpusers.h2.db, ftpusers.lock.db, gbusers.h2.db, gbusers.lock.db .

Where can I create database.properties file?
---------------------------------------------

There are 3 possible location where you can put the file:
	
1) In a custom path specified setting the env variable DATABASE_CONFIG_FILE after run |GB| .

2) In the home of the user running |GB|.

The order of this list isn't random, in fact it indicate the priority of this location: if you put two different version of database.properties in path 1) and 2) the file in path 1) will be override that one in 2).

Remember that if an error occurred in this process, |GB| try to store the db into the TMP dir of the user.