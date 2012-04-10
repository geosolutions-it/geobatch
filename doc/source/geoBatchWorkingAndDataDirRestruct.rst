=================
Welcome to GeoBatch Working and Data dir restruct
=================

Abstract
===================================
This document describes the changes to be made to GeoBatch (branch 1.2.5, and master, from 1.3.0 on)
in order to accomplish the splitting between configuration directory and temp data directory. 

Purpose
===================================
The current dir setup, workingDir and dataDir, are poorly defined. 
The purpose of the refactoring is to have a clear distinction between fixed configuration files 
and runtime temporary files (files that can be removed once a GeoBatch flow instance has been run).

CONFIG DIR
===================================
	*Definition and setup*

* *GEOBATCH_CONFIG_DIR* is the basic configuration directory

In this directory there are:

	* catalog.xml
	* Setting directory
	* Flow configuration files
	* Default action configuration directories (organized in a hierarchy explained below)
	* Default temp dir, if not explicitly specified otherwise.

* *GEOBATCH_CONFIG_DIR* may be specified using an env var or using system setting

	Configuration files for actions inside a given flow are by default looked for in GEOBATCH_CONFIG_DIR/FLOW_ID/ACTION_ID
	configuration file may override the configuration dir with an absolute path.

* *A flow-level config dir* is simply the container for the config dirs of all of its actions. A FlowConfiguration may override its own default config dir, using the declaration <overrideConfigDir>. This override dir will be placed inside the GEOBATCH_CONFIG_DIR  if the path is relative. Existence of overridden configuration dirs is always checked.

* *An ActionConfiguration* may override its own default config dir, using the declaration <overrideConfigDir>. This override dir will be placed inside its FlowManager configuration dir if the path is relative. Existence of overridden configuration dirs is checked.

TEMP DIR
====================================
	*This directory is the place where temporary files will be created Temporary directories are organized in a hierarchycal way:*



*	* GEOBATCH_TEMP_DIR is the root directory (baseTempDir)

		* GEOBATCH_TEMP_DIR may be specified using an env var or using system setting
		* By default it's set as a /temp directory inside the GEOBATCH_CONFIG_DIR

* 	* Every flow has a container dir for all of its running instances (flowTempDir)

		* By default this directory is called like the flow ID and is located inside the baseTempDir
		* Can be overridden in the FlowConfiguration, either as an absolute dir or as a relative one. In the latter case, it will be rooted at GEOBATCH_TEMP_DIR

*	* Every running instance of a flow (the EventConsumer thread) has its own temp dir (flowInstanceTempDir)

		* By default the name of this dir is built using the timestamp of its instantiation, and is placed inside its related flowTempDir

*	* Every Action inside a running instance will have its own temp dir

		* By default, the dir name is built using the Action ordinal position in the flow, and its ID (e.g. “1_tiffRetile”), and is placed inside its related flowInstanceTempDir 
	
IMPLEMENTATION DETAIL
===================================

*	DataDirHandler will handle the basic dir configurations, both the GEOBATCH_CONFIG_DIR and the GEOBATCH_TEMP_DIR. It will take care of setting the default base temp dir if it's not defined. It provides methods to retrieve these two base directories. Please note that, as a backward compatibility temporal solution, if GEOBATCH_CONFIG_DIR is not set as environment var or system property, the obsolete GEOBATCH_DATA_DIR env var will be used instead.

*	FileBasedFlowManager handles the optional override configurations at flow level.  It provides the methods getFlowTempDir(), getFlowConfigDir(), that will return the absolute current flow dirs, resolved with the optional override when needed.

*	FileBasedEventConsumer handles the conf and temp dir for the Actions. It will resolve the optional overrideConfigDir at ActionConfiguration level, and will inject into the Actions their proper configDIr and tempDir.

OTHER GENERAL CONSIDERATION
===================================

*	Working dir and data dir names are now deprecated and no longer used. Please remove them in all configurations. As a backward compatibility temporal solution, workingDir fields are now actively ignored in the configuration files.

*	Name and descriptions are now required only at FlowConfiguration and ActionConfiguration level - i.e., only for those entities that are to be presented on the UI. It means that these fields are no longer needed (and actively ignored at the moment) in the EventGeneratorConfiguration and in the EventConsumerConfiguration.