JMS Service
===========

*The JMS service leverage on the following technologies:*

1. Spring:

 http://www.springsource.org/


2. Apache Camel:

 http://camel.apache.org/ 

3. Apache ActiveMQ:
	
 http://activemq.apache.org/ 


To activate this module use the '''jms''' profile into the maven command::
	
	mvn clean install -P[dao.xstream,...],jms

or set the::
	
	-Dall flag


Introduction
------------------------------------------------------------

The GeoBatch JMS service add JMS capabilities to the GeoBatch platform.

It's essentially a way to activate, (sending something like an event), and retrieve results from an active flow into !GeoBatch using a JMS bean message.


How to use
------------------------------------------------------------


A caller should know:

* the '''target URI''' where to send the ObjectMessage

* the name of an '''ACTIVE''' target '''Flow''' on the running !GeoBatch instance

* the '''arguments''' to pass to that flow


The expected '''ObjectMessage''' to send on the JMS queue is a '''JMSFlowRequest''' bean:


.. sourcecode:: java

	public class JMSFlowRequest implements Serializable {

		private String flowId;
		private List<String> files;
		...
	}




The '''flowId''' is the name of the target flow.
The '''List of files''' is a list of absolute paths which will be converted into a list events ('''FileSystemEvent''') and passed to the first action of that flow.

The response will be send to the temporary queue (which should be set into the JMS message header as ReplyTo).

'''NOTE:''' Take a look to the [source:/src/gb-services/gb-jms/src/test/java/it/geosolutions/geobatch/camel/test/JMSClient.java call()] method into the test package to understand how to perform a correct request.

The response consists into an ObjectMessage which will be a '''JMSFlowResponse''' bean.

This bean contains:

* Enum status member coded as '''JMSFlowStatus'''
* (optionally) a list of messages (if the returned status is JMSFlowStatus.FAILURE).

The '''JMSFlowStatus''':


.. sourcecode:: java

	public enum JMSFlowStatus {
			SUCCESS,
			FAILURE
	}


Looking at the '''JMSFlowResponse''' bean:


.. sourcecode:: java

	public class JMSFlowResponse implements Serializable {
		
		private JMSFlowStatus status;
		private List<String> responses;
		...
	}

Settings
--------------------------------------------------------------

This is the '''jms.properties''' file::

	# the camel JMS component configuration
	pooledConnectionFactory.maxConnections=50
	pooledConnectionFactory.maximumActive=10
	jmsConfig.concurrentConsumers=10
	jmsConnectionFactory.brokerURL=tcp://localhost:61612

	# Camel Route configuration
	# JMS queue name
	JmsRoute.queueName=fileSevice
	# consumer thread pool
	JmsRoute.poolSize=3
	JmsRoute.maxPoolSize=10

	# Broker and connectors
	broker.deleteAllMessagesOnStartup=true
	# (should be the same of the camel jms component)
	broker.transportConnectorURIs[0]=tcp://localhost:61612

Tests
---------------------------------------------------------------

Take a look to the:: 

	source:/src/gb-services/gb-jms/src/test/java/it/geosolutions/geobatch/camel/test/JMSClient.java 

main() method into the test package.
