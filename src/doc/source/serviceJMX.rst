JMX Service
===========

*The JMX service leverage on the following technologies:*

1. Spring JMX:

 http://www.springsource.org/


To activate this module use the '''jmx''' profile into the maven command::
	
	mvn clean install -Pjmx


Introduction
------------

GeoBatch supports multiple flow configuration which are stored into the GEOBATCH_CONFIG_DIR as static XML.
The new interface is developed to help users to configure on the fly complex flows,
potentially, each jmx call can configure and asynchronously start a new consumer.

How to use
----------

Compile GeoBatch including profile 'jmx' and all the profile you may want to include (depending on which action do you want to use).

.. sourcecode:: xml
	# mvn clean install -Pjmx


Run GeoBatch setting JAVA_OPTS as following:
.. sourcecode:: xml
	IP=x.x.x.x
	GEOBATCH_DATA_DIR="/opt/gb_data_dir/"
	JAVA_OPTS="-Xmx1024m /
		-DGEOBATCH_CONFIG_DIR=${GEOBATCH_CONFIG_DIR} /
		-Djava.rmi.server.hostname=${IP} /
		-Dcom.sun.management.jmxremote /
		-Dcom.sun.management.jmxremote.port=1099 /
		-Dcom.sun.management.jmxremote.authenticate=false /
		-Dcom.sun.management.jmxremote.ssl=false"


Setup the firewall accepting connection on the 1099 port:

.. sourcecode:: xml
	# iptables -L
	# iptables -A INPUT -i eth0 -p tcp --dport 1099 -j ACCEPT
	Check the differences:
	# iptables -L

Set the java policy to accept connection on that port:

.. sourcecode:: xml
	# nano ${JAVA_HOME}/jre/lib/security/java.policy
	
.. sourcecode:: xml
	grant codeBase "jar:file:${java.home}/jre/lib/rt.jar!/-" {
	permission java.net.SocketPermission "localhost:4446", "connect,read,write";
	};

The main service configuration
------------------------------

Once geobatch (compiled with the jmx profile) is started it creates the default service configuration into the GEOBATCH_CONFIG_DIR.
It will be something like the following:

.. sourcecode:: xml
		<?xml version="1.0" encoding="UTF-8"?>
		<FlowConfiguration>
			
			<corePoolSize>5</corePoolSize>
			<maximumPoolSize>10</maximumPoolSize>
			<keepAliveTime>3600</keepAliveTime> <!--seconds-->
			<workQueueSize>100</workQueueSize>
			
			<maxStoredConsumers>100</maxStoredConsumers>
			
			<workingDirectory>JMX_FLOW_MANAGER/</workingDirectory>
			
			<!-- Keep following settings -->
			<keepConsumers>true</keepConsumers>
			<autorun>true</autorun>
			<id>JMX_FLOW_MANAGER</id>
			
		</FlowConfiguration>

Note that you can also configure it manually if you want.

The client
----------

GeoBatch declares public interfaces and give you a complete set of classes to easyly create your own JMX client, take a look to the jmx development documentation for details.

source:src/services/jmx/src/main/java/it/geosolutions/geobatch/services/jmx

A simple client
---------------

.. sourcecode:: java
	/*
	 *  GeoBatch - Open Source geospatial batch processing system
	 *  http://geobatch.codehaus.org/
	 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
	 *  http://www.geo-solutions.it
	 *
	 *  GPLv3 + Classpath exception
	 *
	 *  This program is free software: you can redistribute it and/or modify
	 *  it under the terms of the GNU General Public License as published by
	 *  the Free Software Foundation, either version 3 of the License, or
	 *  (at your option) any later version.
	 *
	 *  This program is distributed in the hope that it will be useful,
	 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
	 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	 *  GNU General Public License for more details.
	 *
	 *  You should have received a copy of the GNU General Public License
	 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
	 */
	package it.geosolutions.geobatch.client;

	import it.geosolutions.geobatch.services.jmx.ConsumerManager;
	import it.geosolutions.geobatch.services.jmx.JMXClientUtils;
	import it.geosolutions.geobatch.services.jmx.JMXCumulatorListener;
	import it.geosolutions.geobatch.services.jmx.JMXTaskRunner;

	import java.io.File;
	import java.io.FileOutputStream;
	import java.util.ArrayList;
	import java.util.List;
	import java.util.Map;

	import org.apache.commons.io.IOUtils;
	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;

	import com.thoughtworks.xstream.XStream;

	/**
	 * 
	 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
	 * 
	 */
	public class GBJMXOrchestrator {

		private final static Logger LOGGER = LoggerFactory.getLogger(GBJMXOrchestrator.class);

		/**
		 * USAGE:<br>
		 * java it.geosolutions.geobatch.services.jmx.MainJMXClientUtils /PATH/TO/FILE.properties<br>
		 * where FILE.properties is the command property file<br>
		 * 
		 * @param argv a String[0] containing the path of the environment used to run the action on GeoBatch
		 * @throws Exception
		 */
		public static void main(String[] argv) throws Exception {
			
			if (argv.length < 1) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Unable to run without a property file.");
				}
				System.exit(1);
			}
			final String path = argv[0];
			File envFile = new File(path);
			if (!envFile.isFile()) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Unable to run without a property file, check the path: " + path);
				}
				System.exit(1);
			}

			// building the environment
			final Map<String, String> commonEnv = JMXClientUtils.loadEnv(argv[0]);

			JMXTaskRunner<ConsumerManager> runner = new ConsumerRunner(commonEnv);
			
			final List<ConsumerManager> retSuccess = new ArrayList<ConsumerManager>();
			final List<ConsumerManager> retFail = new ArrayList<ConsumerManager>();

			// run tasks remotely
			runner.run(retSuccess, retFail);

			if (argv.length == 3) {
				final XStream xstream = new XStream();
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(new File(argv[1]));
					xstream.toXML(retSuccess, fos);
				} catch (Exception e) {
					LOGGER.error(e.getLocalizedMessage(), e);
				} finally {
					IOUtils.closeQuietly(fos);
				}
				try {
					fos = new FileOutputStream(new File(argv[2]));
					xstream.toXML(retFail, fos);
				} catch (Exception e) {
					LOGGER.error(e.getLocalizedMessage(), e);
				} finally {
					IOUtils.closeQuietly(fos);
				}
			} else {
				for (ConsumerManager c:retSuccess){
					LOGGER.info("Succesfully completed consumer: "+c.getUuid()+" with status "+c.getStatus());
					for (JMXCumulatorListener l:c.getListeners(JMXCumulatorListener.class)){
						LOGGER.info("Messages: ");
						int i=0;
						for (String message:l.getMessages())
							LOGGER.info("Message_"+i+": "+message);
					}
				}
				for (ConsumerManager c:retFail){
					LOGGER.info("Failure for consumer: "+c.getUuid()+" with status "+c.getStatus());
					for (JMXCumulatorListener l:c.getListeners(JMXCumulatorListener.class)){
						LOGGER.info("Messages: ");
						int i=0;
						for (String message:l.getMessages())
							LOGGER.info("Message_"+i+": "+message);
					}
				}
			}
			System.exit(0);
		}
	}

The '''ConsumerRunner''' class extends the '''JMXTaskRunner<ConsumerManager>''' abstract class to run the tasks.
In the following example we use a CSV file located on the GeoBatch server to define the '''final List<Map<String, String>> consumerConfiguration''' which is essentially a list of action configuration(s) used to run a consumer instance.
The result is that for each CSV row of the file we run  a different flow (using a '''JMXAsynchConsumer''').
Note that each flow can run different actions with differents configurations.

.. sourcecode:: java
	package it.geosolutions.geobatch.client;

	import it.geosolutions.geobatch.services.jmx.ConsumerManager;
	import it.geosolutions.geobatch.services.jmx.JMXAsynchConsumer;
	import it.geosolutions.geobatch.services.jmx.JMXClientUtils;
	import it.geosolutions.geobatch.services.jmx.JMXTaskRunner;
	import it.geosolutions.tools.commons.generics.IntegerCaster;
	import it.geosolutions.tools.commons.generics.SetComparator;
	import it.geosolutions.tools.commons.reader.CSVReader;

	import java.io.File;
	import java.io.IOException;
	import java.util.Iterator;
	import java.util.List;
	import java.util.Map;
	import java.util.Set;
	import java.util.concurrent.CompletionService;
	import java.util.concurrent.RejectedExecutionException;

	public class ConsumerRunner extends JMXTaskRunner<ConsumerManager> {

		private long delay;
		private final Set<Object[]> data;
		private final Map<String,String> commonEnv;

		public final static String CSV_FILE_KEY="CSV";
		
		public ConsumerRunner(final Map<String,String> commonEnv) throws Exception {
			super();
			this.commonEnv=commonEnv;
			this.delay=JMXClientUtils.parsePollingDelay(commonEnv);
			final String csvFileName=commonEnv.get(CSV_FILE_KEY);
			if (csvFileName==null)
				throw new IllegalArgumentException("Unable to locate the "+CSV_FILE_KEY+" matching the CSV file name path");
			
			final File csv=new File(csvFileName); 
			
			data=CSVReader.readCsv(LOGGER, csv, ",", new SetComparator<Integer>(new IntegerCaster(), StatusMapper.keyIndex), false, false);
		}
		
		@Override
		public int runTasks(CompletionService<ConsumerManager> cs) throws Exception {

			final Iterator<Object[]> it = data.iterator();
			int size = 0; // number of submitted tasks
			while (it.hasNext()) {

				// change config using naming convention (from the file name)
				final List<Map<String, String>> consumerConfiguration = StatusMapper.configureFlow(
						LOGGER, it.next(), commonEnv);
				if (consumerConfiguration==null){
					if (LOGGER.isDebugEnabled())
						LOGGER.error("Unable to parse the configuration");
					continue;
				}
				try {
					// submit the job
					cs.submit(new JMXAsynchConsumer(jmxConnector, serviceManager,
							consumerConfiguration, delay));

					// work queue size
					++size;

				} catch (RejectedExecutionException e) {
					if (LOGGER.isDebugEnabled())
						LOGGER.error(e.getLocalizedMessage(), e);
					else
						LOGGER.error(e.getLocalizedMessage());
				} catch (Exception e) {
					if (LOGGER.isDebugEnabled())
						LOGGER.error(e.getLocalizedMessage(), e);
					else
						LOGGER.error(e.getLocalizedMessage());
				}
			}
			return size;
			
		}
		
		@Override
		public void run(final List<ConsumerManager> retSuccess,
				final List<ConsumerManager> retFail) throws Exception{
			
			run(this,commonEnv,retSuccess,retFail);
			
			// TODO WRITE INTO DATA
		}
	}


Compile the JMX client:
.. sourcecode:: xml
	$ mvn clean install

Copy the security.policy file into the config dir:
.. sourcecode:: xml
	# cp security.policy /my/app/config/

Add the security.policy to the JVM configuration into the config/sos.ini file:
.. sourcecode:: xml
	-Djava.security.policy=security.policy

Copy the jmx.property file to the client config dir:

.. sourcecode:: xml
	# cp jmx.properties /my/app/config/
	
Edit it to point to the GeoBatch url/port

Setup a new consumer
--------------------

To setup a new consumer on the client side you could use a property file:

.. sourcecode:: xml
	# remote JMX server url
	gb_jmx_url=localhost
	# remote JMX server port
	gb_jmx_port=1099
	# bean name which implements ActionManager interface
	JMXActionManager=JMXActionManager

The above parameters are mandatory to enstablish a connection to the target GeoBatch

Example
-------

The following is a complete consumer flow configuration which simply run a Collector Action (see commons actions).

.. sourcecode:: xml
	# remote JMX server url
	gb_jmx_url=localhost
	# remote JMX server port
	gb_jmx_port=1099
	# bean name which implements ActionManager interface
	JMXServiceManager=JMXServiceManager

	PROCESS_DELAY=2

	SERVICE_ID=CollectorGeneratorService
	INPUT=./
	wildcard=*.*
	deep=3
