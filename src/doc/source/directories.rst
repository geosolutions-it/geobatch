.. |GB| replace:: *GeoBatch*

Configuration and temporal directory structures
===============================================

Temporal directory contents
...........................

Each Action instance needs to use a separate subdirectory under the base temp dir. The |GB| Engine will manage its creation, as explained below. In case an Action is instantiated manually and not through |GB| Engine, you will need to manage the subdirectory creation manually.

Subdirectory creation under ``GEOBATCH_CONFIG_DIR`` is automatically managed by |GB|, according to this pattern:

* Each flow wil have a separate temp dir for all of its running instances (the ``flowTempDir``):

  * By default this directory is called like the flow ID and is located under ``GEOBATCH_TEMP_DIR``.
  * Can be overridden in the FlowConfiguration, either as an absolute dir or as a relative one. In the latter case, it will be located under ``GEOBATCH_TEMP_DIR``.

* Every running instance of a flow has its own temp dir (the ``flowInstanceTempDir``):

  * By default, the name of this dir is built using the timestamp of its instantiation, and is placed inside its related ``flowTempDir``.

* Finally, every Action inside a running flow instance will have its own temp dir:

  * By default, the dir name is built using the Action ordinal position in the flow, and its ID (e.g. ``1_tiffRetile``), and is placed inside its related ``flowInstanceTempDir``.


Use in the code
...............

* ``DataDirHandler`` will handle the basic dir configurations, both the ``GEOBATCH_CONFIG_DIR`` and the ``GEOBATCH_TEMP_DIR``. It will take care of setting the default base temp dir if it's not defined. It provides methods to retrieve these two base directories.

* ``FileBasedFlowManager`` handles the optional override configurations at flow level. It provides the methods ``getFlowTempDir()`` and ``getFlowConfigDir()``, that will return the absolute current flow dirs, resolved with the optional override when needed.

* ``FileBasedEventConsumer`` handles the conf and temp dir for the Actions. It will resolve the optional overrideConfigDir at ActionConfiguration level, and will inject into the Actions their proper configDir and tempDir.

To get the *base* configuration dirs, use:

.. sourcecode:: java

   DataDirHandler ddh;
   [...]
   ddh.getBaseConfigDirectory();
   ddh.getBaseTempDirectory();

To get the Action's specific dirs, use:

.. sourcecode:: java

   BaseAction<EventObject> action;
   [...]
   action.getConfigDir();
   action.getTempDir();

