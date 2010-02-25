.. geobatch documentation master file, created by
   sphinx-quickstart on Sat Jan 16 14:27:14 2010.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Welcome to GeoBatch documentation!
====================================

**GeoBatch Documentation Pages**.  Geobatch is an event-based geospatial aware batch processing system to ease the development, the deploy, and the management of jobs on streams of geospatial data. Geobatch provides basic components for the collection, processing and pubblication of data.

Flow Manager and Features
====================================
- A batch job in Geobatch is an XML configuration file called "flow".
- Each flow consists of three sections: a descriptive part, a second one which is dedicated to data streams monitoring (eventGeneratorConfiguration) and the final one which is devoted to recognition of particular files within a stream, its elaboration and final pubblication (eventConsumerConfiguration).
- A simple web application can be used to launch jobs, view the list, check the status, start, stop or dispose
- Flow can start automatically on application server startup.

.. sourcecode:: java

	package it.geosolutions.geobatch.shp2pg.configuration;

	import it.geosolutions.geobatch.catalog.impl.BaseService;
	import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
	import it.geosolutions.geobatch.flow.event.action.ActionService;

	import java.util.EventObject;

	/**
	 * @author GeoSolutions
	 * 
	 */
	public abstract class Shp2PgConfiguratorService<T extends EventObject, C extends ActionConfiguration>
	        extends BaseService implements ActionService<T, C> {

	    public Shp2PgConfiguratorService() {
	        super(true);
	    }

	    public boolean canCreateAction(C configuration) {
	        // XXX ImPLEMENT ME
	        return true;
	    }

	}

.. toctree::
   :maxdepth: 2


