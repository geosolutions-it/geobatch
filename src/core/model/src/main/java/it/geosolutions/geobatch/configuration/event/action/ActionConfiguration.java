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

package it.geosolutions.geobatch.configuration.event.action;

import it.geosolutions.geobatch.catalog.impl.BaseConfiguration;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * @todo take a look to 
 * @see FileBasedCatalogConfiguration
 * 
 * @author Simone Giannecchini
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public abstract class ActionConfiguration extends BaseConfiguration implements Cloneable {

    
    private List<String> listenerIds = null;

    
    private List<ProgressListenerConfiguration> listenerConfigurations = new ArrayList<ProgressListenerConfiguration>();

    
    private boolean failIgnored = false;
    
    /**
     * workingDirectory: this attribute represents the configuring directory for this flow. 
     * It can be relative to the catalog.xml directory or absolute. Attention: the configuring 
     * directory should be different from the one containing the configuration files.
     */
    private String workingDirectory;

    /**
     * Getter for the workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }
    
    /**
     * Setter for the workingDirectory.
     * @param  workingDirectory
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public ActionConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

    /**
     * Tells if an exception in this Actions should break the entire flow. <BR> Defaults to false.
     *  <P> Some somehow "minor" actions would not break the logical flow, for instance a remote file deletion via FTP.
     * @return  true if an error in this Actions should not stop the whole flow.
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

    public List<String> getListenerIds() {
        return listenerIds;
    }

    protected void setListenerId(List<String> listenerIds) {
        this.listenerIds = listenerIds;
    }

    public void addListenerConfiguration(ProgressListenerConfiguration plc) {
        listenerConfigurations.add(plc);
    }

    public List<ProgressListenerConfiguration> getListenerConfigurations() {
        return listenerConfigurations;
    }

    public void setListenerConfigurations(List<ProgressListenerConfiguration> listenerConfigurations) {
        if (listenerConfigurations == null)
            throw new NullPointerException("Can't set listenerConfig list to null");
        this.listenerConfigurations = listenerConfigurations;
    }

    @Override
    public ActionConfiguration clone() {
        ActionConfiguration bc = (ActionConfiguration) super.clone();
        
        setWorkingDirectory(bc.getWorkingDirectory());

        setFailIgnored(bc.isFailIgnored());

        bc.listenerIds = listenerIds == null ? new ArrayList<String>() : new ArrayList<String>(
                listenerIds);

        bc.listenerConfigurations = new ArrayList<ProgressListenerConfiguration>();
        if (listenerConfigurations != null) {
            for (ProgressListenerConfiguration plc : listenerConfigurations) {
                bc.listenerConfigurations.add(plc); // CHECKME: shall we clone
                // the configs?
            }
        }
        return bc;
    }

}
