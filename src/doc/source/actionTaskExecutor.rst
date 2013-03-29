Task Executor Action
====================

The Task Executor Action is used to execute processes on the GeoBatch system using ant task.

Input
-----

An xml file containing the options to pass to the executor.

Actions
-------

* The passed options are used to build to command line using an associated XSLT (provided by the configuration).
* The executor run the configured process in an external process
* The stdout and the stderr are sent to a configured log file

Output
------

Can be configured so it can be a predefinited file or the same xml as the input.

Configuration
-------------

Main element: ``TaskExecutorConfiguration``.

Will contain the following child elements:

* Identification parameters:
    * **serviceID**: Should be ``TaskExecutorService``.
    * **id**: An ID for this action.
    * **name**: A name for this action.
    * **description**: A description for this action.

* Main options:
    * **errorFile**: String matching the error file name, can be relative to the config dir or absolute.
    * **timeOut**: The timeout of the process
    * **executable**: The executable to run
    * **outputName**: The optional file name in output
    * **defaultScript**: a default script
    * **variables**: a map to set environment variables to add to the process env
    * **xsl**: The xsl to transform the input command

Configuration example:

.. sourcecode:: xml

  <TaskExecutorConfiguration>
      <serviceID>TaskExecutorService</serviceID>
      <errorFile>config/errorlog.txt</errorFile>
      <timeOut>1200000</timeOut> <!-- milliseconds -->
      <executable>D:\work\programs\Java\jdk1.5.0_17\bin\gdaladdo.exe</executable>
      <defaultScript>config/gdaladdo.xml</defaultScript>
      <outputName>*.tif</outputName>
      <variables>
		<entry>
		  <string>GDAL_DATA</string>
		  <string>C:/Python26/DLLs/gdalwin32-1.6/data</string>
		</entry>
		<entry>
		  <string>PATH</string>
		  <string>D:/work/programs/Java/jdk1.5.0_17/bin</string>
		</entry>
      </variables>
      <xsl>config/gdaladdo.xsl</xsl>
      <id>gdaladdo</id>
      <description>Flow to run gdaladdo operations</description>
      <name>gdaladdo flow</name>
  </TaskExecutorConfiguration>

Configuration xsl example:

.. sourcecode:: xml
 
  <?xml version="1.0" encoding="ISO-8859-1"?>
  <xsl:stylesheet version="1.0" 
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      xmlns:tsk="http://www.geo-solutions.it/tsk">
      <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>
      <xsl:template match="r">
	  <xsl:value-of select="concat(' -r ', .)"/>
	  <xsl:apply-templates select="@*|node()"/>
      </xsl:template>
      
      <xsl:template match="ro">
	  <xsl:value-of select="concat(' -ro')"/>
	  <xsl:apply-templates select="@*|node()"/>
      </xsl:template>
      
      <xsl:template match="clean">
	  <xsl:value-of select="concat(' -clean ', .)"/>
	  <xsl:apply-templates select="@*|node()"/>
      </xsl:template>
      
      <xsl:template match="@*|node()">
	  <xsl:apply-templates select="@*|node()"/>
      </xsl:template>
      
      <xsl:template match="filename">
	  <xsl:value-of select="concat(' ', .)"/>
	  <xsl:apply-templates select="@*|node()"/>
      </xsl:template>
      
      <xsl:template match="levels">
	  <xsl:value-of select="concat(' ', .)"/>
	  <xsl:apply-templates select="@*|node()"/>
      </xsl:template>
      
  </xsl:stylesheet>