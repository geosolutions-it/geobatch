package it.geosolutions.geobatch.geoserver.style;

import it.geosolutions.geobatch.annotations.Action;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.reload.GeoServerReloadConfiguration;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;

import java.io.File;
import java.util.Collection;
import java.util.EventObject;
import java.util.Queue;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Action(configurationClass=GeoServerStyleConfiguration.class)
public class GeoServerStyleAction extends BaseAction<EventObject> {

	protected final static Logger LOGGER = LoggerFactory
			.getLogger(GeoServerStyleAction.class);

	final public static String PASS = "pass";
	final public static String USER = "user";
	final public static String URL = "url";

	final GeoServerStyleConfiguration conf;

	public GeoServerStyleAction(GeoServerStyleConfiguration actionConfiguration) {
		super(actionConfiguration);
		conf = actionConfiguration;
	}

    @Override
	@CheckConfiguration
	public boolean checkConfiguration() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public Queue<EventObject> execute(Queue<EventObject> events)
			throws ActionException {

		listenerForwarder.started();
		listenerForwarder.setTask("Checking conf");
		String op = conf.getOperation();
		if (op == null || !(op.equalsIgnoreCase("UPDATE") || op.equalsIgnoreCase("REMOVE")
				|| op.equalsIgnoreCase("PUBLISH"))) {
			ActionException ae = new ActionException(this, "Bad operation: "
					+ op + " in configuration");
			listenerForwarder.failed(ae);
			throw ae;
		}

		final String pass = conf.getGeoserverPWD();
		final String user = conf.getGeoserverUID();
		final String url = conf.getGeoserverURL();
		GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(url,
				user, pass);
		// TODO check for GS
		
		listenerForwarder.completed();
		
		for (EventObject ev : events) {
			
			File sldFile = (File) ev.getSource(); //TODO check for input
			
			listenerForwarder.setTask("Running: " + op + " on "+sldFile);
			listenerForwarder.started();
			if (op.equalsIgnoreCase("UPDATE")) {
				publisher.updateStyle(sldFile,FilenameUtils.getBaseName(sldFile.getName()));
			} else if (op.equalsIgnoreCase("REMOVE")) {
				publisher.removeStyle(FilenameUtils.getBaseName(sldFile.getName()), true); //with purge
			} else if (op.equalsIgnoreCase("PUBLISH")) {
				publisher.publishStyle(sldFile,FilenameUtils.getBaseName(sldFile.getName()));
			}
			listenerForwarder.completed();
			
		}

		return events;
	}

}
