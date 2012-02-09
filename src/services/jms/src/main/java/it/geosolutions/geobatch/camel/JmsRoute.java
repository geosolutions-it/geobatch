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
package it.geosolutions.geobatch.camel;

import org.apache.camel.builder.RouteBuilder;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class JmsRoute extends RouteBuilder {
	
	private final String queueName;
	private int poolSize=1, maxPoolSize=10; 
	
	public JmsRoute(final String queueName){
		super();
		this.queueName=queueName;
	}

	@Override
	public void configure() throws Exception {
		
		from("activemq:queue:"+queueName+"?preserveMessageQos=true").
			threads(poolSize, maxPoolSize).
			doTry().
				process(new JMSFlowManager()).
			doCatch(java.lang.Exception.class).
			end();
	}

	/**
	 * @return the queueName
	 */
	public String getQueueName() {
		return queueName;
	}

	/**
	 * @return the poolSize
	 */
	public int getPoolSize() {
		return poolSize;
	}

	/**
	 * @param poolSize the poolSize to set
	 */
	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	/**
	 * @return the maxPoolSize
	 */
	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	/**
	 * @param maxPoolSize the maxPoolSize to set
	 */
	public void setMaxPoolSize(int maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

}
