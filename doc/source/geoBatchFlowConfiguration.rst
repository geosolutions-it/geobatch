.. geobatch documentation master file, created by
   sphinx-quickstart on Sat Jan 16 14:27:14 2010.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

**Flow Configuration**
====================================



*Flow* 
------------------------------------
**The listeners configuration:**

* Each listener is referred into the previous explained components using the node:
.. sourcecode:: xml

    <listenerId>NAME</listenerId>
.. toctree::
   :maxdepth: 2

* which have its counterpart into this list into the node:
.. sourcecode:: xml

    <id>NAME</id>
.. toctree::
   :maxdepth: 2

* The node:

.. sourcecode:: xml

    <serviceID>...</serviceID>

.. toctree::
   :maxdepth: 2

Represents an alias id for the class to use and (actually)
can be:

* *cumulatingListenerService*


It is a service that is used to instantiate ProgressCumulatingListener (class), which is used by' graphical interface to send status messages to the graphical interface, 
and must be configured at the level of consumer.


* *statusListenerService*

It is a service that is used to instantiate ProgressStatusListener (class), which serve to define lists that are
graphical interface used to monitor the status of individual actions accordingly edition should be used only in the configuration of an 'action.


* *loggingListenerService*

It is a service that is used to instantiate ProgressLoggingListener (class), is used to dallle actions and by the consumer for logging events in progress,

for example: 

1. *Consumer started*
2. *Action started*
3. *Action concluded*

.. sourcecode:: xml

	<ListenerConfigurations>

	 <CumulatingProgressListener>
	  <serviceID>cumulatingListenerService</serviceID>
	  <id>ConsumerLogger0</id>
	 </CumulatingProgressListener>

	 <StatusProgressListener>
	  <serviceID>statusListenerService</serviceID>
	  <id>ActionListener0</id>
	 </StatusProgressListener>
			
	 <LoggingProgressListener>
	  <serviceID>loggingListenerService</serviceID>
	  <id>ActionListener1</id>

	  <loggerName>ActionListener1</loggerName>
	 </LoggingProgressListener>

	 <LoggingProgressListener>
	  <serviceID>loggingListenerService</serviceID>
	  <id>ConsumerLogger0</id>

	  <loggerName>ConsumerLogger0</loggerName>
	 </LoggingProgressListener>

	</ListenerConfigurations>

.. toctree::
   :maxdepth: 2
   






