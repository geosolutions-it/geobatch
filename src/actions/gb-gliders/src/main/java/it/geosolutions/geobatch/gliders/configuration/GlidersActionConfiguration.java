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



package it.geosolutions.geobatch.gliders.configuration;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;



public class GlidersActionConfiguration extends ActionConfiguration implements Configuration {

    protected GlidersActionConfiguration(String id, String name,
			String description, boolean dirty) {
		super(id, name, description, dirty);
		// TODO Auto-generated constructor stub
	}
    
    private String workingDirectory;

    private String dbPWD;

    private String dbUID;

    private String dbServerIp;
    
    private String dbPort;
    
    private String dbName;

    private String storeFilePrefix;

    private String configId;

    private String dbType;
    
    private String dbTableName;
    
    private Double simplyTollerance;

    public GlidersActionConfiguration() {
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

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }
    
    public String getDbTableName() {
        return dbTableName;
    }

    public void setDbTableName(String dbtablename) {
        this.dbTableName = dbtablename;
    }
    
    public double getSimplyTollerance(){
    	return simplyTollerance;
    }
    
    public void setSimplyTollerance(double simplyTollerance){
    	this.simplyTollerance = simplyTollerance;
    }

    @Override
    public ActionConfiguration clone() throws CloneNotSupportedException {
		final GlidersActionConfiguration configuration = 
			new GlidersActionConfiguration(super.getId(),super.getName(),super.getDescription(),super.isDirty());

		configuration.setDbPWD(dbPWD);
		configuration.setDbUID(dbUID);
        configuration.setDbServerIp(dbServerIp);
		configuration.setDbPort(dbPort);
        configuration.setDbName(dbName);
		configuration.setStoreFilePrefix(storeFilePrefix);
		configuration.setConfigId(configId);
		configuration.setServiceID(getServiceID());
		configuration.setDbType(dbType);
		configuration.setWorkingDirectory(workingDirectory);
		configuration.setDbTableName(dbTableName);
		configuration.setSimplyTollerance(simplyTollerance);
		return configuration;
    }



}
