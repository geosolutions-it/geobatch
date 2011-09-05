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
package it.geosolutions.geobatch.flow.event.action;

import it.geosolutions.geobatch.catalog.impl.BaseIdentifiable;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.Job;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.ProgressListener;
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;

import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 * @author (r2) Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version r1<br>
 *          r2 - on: 26 Aug 2011<br>
 * 
 * @param <XEO>
 *            Kind of EventObject to be eXecuted
 */
public abstract class BaseAction<XEO extends EventObject> extends
		BaseIdentifiable implements Action<XEO>, Job {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(BaseAction.class);

	/**
	 * the context where action is running in...<br>
	 * this is initialized by the FlowManager
	 */
	private String runningContext;

	/**
	 * @return the runningContext
	 */
	public String getRunningContext() {
		return runningContext;
	}

	/**
	 * @param runningContext
	 *            the runningContext to set
	 */
	public void setRunningContext(String runningContext) {
		this.runningContext = runningContext;
	}

	final protected ProgressListenerForwarder listenerForwarder;

	protected boolean failIgnored = false;

	public BaseAction(String id, String name, String description) {
		super(id, name, description);
		listenerForwarder = new ProgressListenerForwarder(this);
		failIgnored = false;
	}

	public BaseAction(ActionConfiguration actionConfiguration) {
		super(actionConfiguration.getId(), actionConfiguration.getName(),
				actionConfiguration.getDescription());
		listenerForwarder = new ProgressListenerForwarder(this);
		failIgnored = actionConfiguration.isFailIgnored();
	}

	public void destroy() {
	}

	public boolean isPaused() {
		return false;
	}

	public boolean pause() {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("Pause request for " + getClass().getSimpleName());
		return false; // pause has not been honoured
	}

	public boolean pause(boolean sub) {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("Pause(" + sub + ") request for "
					+ getClass().getSimpleName());
		return false; // pause has not been honoured
	}

	public void resume() {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("Resuming " + getClass().getSimpleName());
	}

	/**
	 * @return
	 */
	public boolean isFailIgnored() {
		return failIgnored;
	}

	/**
	 * @param failIgnored
	 */
	public void setFailIgnored(boolean failIgnored) {
		this.failIgnored = failIgnored;
	}

	public void removeListener(ProgressListener listener) {
		this.listenerForwarder.removeListener(listener);
	}

	public void addListener(ProgressListener listener) {
		this.listenerForwarder.addListener(listener);
	}

	public <PL extends IProgressListener> PL getProgressListener(Class<PL> clazz) {
		for (IProgressListener ipl : listenerForwarder.getListeners()) {
			if (clazz.isAssignableFrom(ipl.getClass()))
				return (PL) ipl;
		}
		return null;
	}
}
