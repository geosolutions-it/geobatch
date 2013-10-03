.. |GB| replace:: *GeoBatch*
.. |GS| replace:: *GeoServer*
.. |GH| replace:: *GitHub*

.. _`consumer`:

The Consumer
=================

In the previous section has been introduced the |GB| *Flow* concept as a composition of *Actions*, *EventGenerators* and *Listeners* defined through a *Flow Configuration*.

The |GB| core engine reads the *Flow Configuration* and listen for events. When an event is thrown a **Consumer** (more exactly an **EventConsumer**) is launched.

The |GB| **Consumer** basically is a **flow instance**: a Flow configuration defines a **Class** of a chained process. When an event triggers the execution of that chained process (Flow) an **Instance** of the Flow is responsible to execute the process.

Generally speaking a geobatch environment is usually configured with *n* flows and in a certain time may be running *m* consumers.

Consumer tasks
------------------

A |GB| consumer is responsible to 

* Create and handle the **Flow Temp directories**, that is the FileSystem location where the flow execution stores its files

* Run the actions according with the action chain specified by the flow configuration

The |GB| :ref:`restinterface` exposes sevices to obtain information about the **configured Flows** and the **Running consumers**.

The status of a consumer can be viewed also from the |GB| GUI see :ref:`use` section.






