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

package it.geosolutions.geobatch.settings.flow;

import it.geosolutions.geobatch.settings.GBSettings;

/**
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class FlowSettings extends GBSettings {
	
	public FlowSettings(){
		super("FLOW");
	}

	private static final long serialVersionUID = 7121137497699361776L;
	/**
     * maximum numbers of stored executed (see Consumer.getStatus()) consumers
     */
	private static final int DEFAULT_maxStoredConsumers=10;
    private int maxStoredConsumers=DEFAULT_maxStoredConsumers;
    
    
//	/**
//	 * autorun: this attribute is used to autorun a flow on startup.
//	 */
//    public static final boolean DEFAULT_autorun = true;
//	private boolean autorun = DEFAULT_autorun;

    private static final int DEFAULT_workQueueSize=100;
    private int workQueueSize=DEFAULT_workQueueSize;
    
    private static final int DEFAULT_corePoolSize=10;
    private int corePoolSize=DEFAULT_corePoolSize;
    
    
    private static final int DEFAULT_maximumPoolSize=30;
    private int maximumPoolSize=DEFAULT_maximumPoolSize;
    
    // secs
    private static final int DEFAULT_keepAliveTime=150;
    private int keepAliveTime=DEFAULT_keepAliveTime;
    
//	/**
//	 * do not remove ContextDirectory when consumer is disposed
//	 */
//    private static final boolean DEFAULT_keepContextDir=false;
//	private boolean keepContextDir = DEFAULT_keepContextDir;
//
//	/**
//	 * @return the keepContextDir
//	 */
//	public boolean isKeepContextDir() {
//		return keepContextDir;
//	}
//
//	/**
//	 * @param keepContextDir the keepContextDir to set
//	 */
//	public void setKeepContextDir(boolean keepContextDir) {
//		this.keepContextDir = keepContextDir;
//	}

    
	/**
	 * @return the maxStoredConsumers
	 */
	public int getMaxStoredConsumers() {
		return maxStoredConsumers;
	}
	/**
	 * @param maxStoredConsumers the maxStoredConsumers to set
	 */
	public void setMaxStoredConsumers(int maxStoredConsumers) {
		this.maxStoredConsumers = maxStoredConsumers;
	}
//	/**
//	 * @return the autorun
//	 */
//	public boolean isAutorun() {
//		return autorun;
//	}
//	/**
//	 * @param autorun the autorun to set
//	 */
//	public void setAutorun(boolean autorun) {
//		this.autorun = autorun;
//	}
	/**
	 * @return the workQueueSize
	 */
	public int getWorkQueueSize() {
		return workQueueSize;
	}
	/**
	 * @param workQueueSize the workQueueSize to set
	 */
	public void setWorkQueueSize(int workQueueSize) {
		this.workQueueSize = workQueueSize;
	}
	/**
	 * @return the corePoolSize
	 */
	public int getCorePoolSize() {
		return corePoolSize;
	}
	/**
	 * @param corePoolSize the corePoolSize to set
	 */
	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}
	/**
	 * @return the maximumPoolSize
	 */
	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}
	/**
	 * @param maximumPoolSize the maximumPoolSize to set
	 */
	public void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}
	/**
	 * @return the keepAliveTime
	 */
	public int getKeepAliveTime() {
		return keepAliveTime;
	}
	/**
	 * @param keepAliveTime the keepAliveTime to set
	 */
	public void setKeepAliveTime(int keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

}
