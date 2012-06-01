.. |GB| replace:: *GeoBatch*

Environment Setup
=================

Use the following Java System Properties to indicate to |GB| where to look for the configuration files, and where to place the temporary files during processing.

Run GeoBatc with the ``-D`` parameter to define the system properties. For instance::

-DGEOBATCH_CONFIG_DIR="/home/geobatch/config" -DGEOBATCH_TEMP_DIR="/temp"


``GEOBATCH_CONFIG_DIR``
-----------------------

This is the directory where all configuration files are placed. This property is **mandatory**.

This directory must contain:

* ``catalog.xml``.
* ``settings`` directory.
* Flow configuration ``*.xml`` files.
* Action configuration directories, following the hierarchy explained below, unless specified otherwise in the configuration file.
* ``temp`` dir, unless explicitly specified otherwise (see `GEOBATCH_TEMP_DIR`_).

Each action needs a configuration dir. Its default location is ``GEOBATCH_CONFIG_DIR/FLOW_ID/ACTION_ID``. Custom locations can be specified in the flow configuration files:

* At **flow level**, using the ``<overrideConfigDir>`` element. It will point to the directory containing all the configuration directories for all the actions in the flow. If the path specified in ``<overrideConfigDir>`` is relative, it will be placed under ``GEOBATCH_CONFIG_DIR``.

* At **action level**. Each action may override its own config dir location, using the ``<overrideConfigDir>`` element. If a relative path is given, it will be placed under its flow-level dir specification.


``GEOBATCH_TEMP_DIR``
---------------------

This is the place where |GB| Actions will create their temporary files during execution. If not specified, the temp dir will be placed under ``GEOBATCH_CONFIG_DIR``.

Each Action instance needs to use a separate subdirectory under the base temp dir. The |GB| Engine will manage its creation, as explained below. In case an Action is instantiated manually and not through |GB| Engine, you will need to manage the subdirectory creation manually.

Temporal directories creation
.............................

Subdirectory creation under ``GEOBATCH_CONFIG_DIR`` is automatically managed by |GB|, according to this pattern:

* Each flow wil have a separate temp dir for all of its running instances (the ``flowTempDir``):

  * By default this directory is called like the flow ID and is located under ``GEOBATCH_TEMP_DIR``.
  * Can be overridden in the FlowConfiguration, either as an absolute dir or as a relative one. In the latter case, it will be located under ``GEOBATCH_TEMP_DIR``.

* Every running instance of a flow has its own temp dir (the ``flowInstanceTempDir``):

  * By default, the name of this dir is built using the timestamp of its instantiation, and is placed inside its related ``flowTempDir``.

* Finally, every Action inside a running flow instance will have its own temp dir:

  * By default, the dir name is built using the Action ordinal position in the flow, and its ID (e.g. ``1_tiffRetile``), and is placed inside its related ``flowInstanceTempDir``.


Implementation details
----------------------

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
