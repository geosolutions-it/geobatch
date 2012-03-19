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

E' un servizio che serve ad istanziare ProgressCumulatingListener(classe), il quale viene utilizzato dall' interfaccia grafica per mandare messaggi di stato all'interfaccia grafica,
e deve essere configurato a livello di consumer.

* *statusListenerService*

E' un servizio che serve ad istanziare ProgressStatusListener(classe), il quale servono per definire delle liste che vengono 
utilizzate dall'interfaccia grafica per monitorare lo stato delle singole azioni edi conseguenza devono essere utilizzate solo nella configurazione di un' azione.

* *loggingListenerService*

E' un servizio che serve ad istanziare ProgressLoggingListener(classe), viene utilizzata per dallle azioni e dal consumer per loggare eventi in progresso,
ad es.: 

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
   






