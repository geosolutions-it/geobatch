/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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



package it.geosolutions.geobatch.ctd.configuration;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;


/**
 * 
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 * 
 */
public class CTDActionConfiguration extends ActionConfiguration implements Configuration {

    protected CTDActionConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
	}
    
    private String workingDirectory;

    private String dbPWD;

    private String dbUID;

    private String dbServerIp;
    
    private String dbPort;
    
    private String dbName;

    private String storeFilePrefix;

    private String configId;
    

    public CTDActionConfiguration() {
        super();
    }

    /**
     * @return the workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @param workingDirectory
     *            the workingDirectory to set
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getDbPWD() {
        return dbPWD;
    }

    public void setDbPWD(String dbPWD) {
        this.dbPWD = dbPWD;
    }

    public String getDbUID() {
        return dbUID;
    }

    public void setDbUID(String dbUID) {
        this.dbUID = dbUID;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }
    
    public String getDbServerIp() {
        return dbServerIp;
    }

    public void setDbServerIp(String dbServerIp) {
        this.dbServerIp = dbServerIp;
    }
    
    public String getDbPort() {
        return dbPort;
    }

    public void setDbPort(String dbPort) {
        this.dbPort = dbPort;
    }

    public String getStoreFilePrefix() {
        return storeFilePrefix;
    }

    public void setStoreFilePrefix(String storeFilePrefix) {
        this.storeFilePrefix = storeFilePrefix;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    @Override
    public ActionConfiguration clone() throws CloneNotSupportedException {
		final CTDActionConfiguration configuration = 
			new CTDActionConfiguration(super.getId(),super.getName(),super.getDescription(),super.isDirty());

		configuration.setDbPWD(dbPWD);
		configuration.setDbUID(dbUID);
        configuration.setDbServerIp(dbServerIp);
		configuration.setDbPort(dbPort);
        configuration.setDbName(dbName);
		configuration.setStoreFilePrefix(storeFilePrefix);
		configuration.setConfigId(configId);
		configuration.setServiceID(getServiceID());
		configuration.setWorkingDirectory(workingDirectory);
		return configuration;
    }



}
