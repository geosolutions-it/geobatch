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
package it.geosolutions.geobatch.services.jmx;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

/**
 * Maps the remote action call status, uuid and environment. 
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class JMXConsumerManager implements Serializable,ConsumerManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8886665670245478248L;
	
	private final Map<String, String> configuration;
	
	// the consumer unique id
	private final String uuid;
	
	private final transient ServiceManager manager;
	// the consumer status
	private ConsumerStatus status;
	// switch (ask to the proxy / use cached values)
	private boolean disposed=false;
	// the listener collection
	private Collection<JMXProgressListener> listeners;
	
	public JMXConsumerManager(Map<String, String> configuration, ServiceManager manager) throws Exception {
		super();
		if (configuration==null)
			throw  new IllegalArgumentException("Unable to build the "+JMXConsumerManager.class+" using a null configuration");

		this.configuration=configuration;		
		
		this.manager=manager;
		
		this.uuid=manager.createConsumer(configuration);
	}

	@Override
	public Collection<JMXProgressListener> getListeners() {
		return this.getListeners(JMXProgressListener.class);
	}
	
	@Override
	public Collection<JMXProgressListener> getListeners(Class<? extends JMXProgressListener> clazz) {
		if (disposed){
			return listeners;
		}
		return (listeners=manager.getListeners(uuid,clazz));
	}
	
	/*protected */void addAll(Collection<JMXCumulatorListener> listeners){
		this.listeners.addAll(listeners);
	}

	@Override
	public String getUuid() {
		return uuid;
	}
	
	@Override
	public void dispose() throws Exception{
		if (!disposed){
				manager.disposeConsumer(uuid);
				disposed=true;
		}
	}

	@Override
	public Map<String, String> getConfiguration() {
		return MapUtils.unmodifiableMap(configuration);
	}

	@Override
	public ConsumerStatus getStatus() {
		if (disposed){
			return status;
		}
		return (status=manager.getStatus(uuid));
	}

	@Override
    public void run(Serializable event) throws Exception {
    	if (!disposed)
    		manager.runConsumer(uuid, event);
    	else
    		throw new IllegalStateException("Unable to run consumer on a disposed "+this.getClass());
    }
}
