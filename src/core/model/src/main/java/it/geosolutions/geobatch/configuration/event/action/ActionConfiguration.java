/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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

import it.geosolutions.geobatch.catalog.impl.BaseDescriptableConfiguration;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @todo take a look to 
 * @see FileBasedCatalogConfiguration
 * 
 * @author Simone Giannecchini
 * @author Emanuele Tajariol <etj AT geo-solutions DOT it>, GeoSolutions S.A.S.
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public abstract class ActionConfiguration
    extends BaseDescriptableConfiguration {


    private List<String> listenerIds = null;

    
    private List<ProgressListenerConfiguration> listenerConfigurations = new ArrayList<ProgressListenerConfiguration>();

    
    private boolean failIgnored = false;
    
    /**
     * Action configuration directory
     */
    private File overrideConfigDir;


//    public ActionConfiguration() {
//        super();
//    }
    
    public ActionConfiguration(String id, String name, String description) {
        super(id, name, description);
    }
    
    public File getOverrideConfigDir() {
        return overrideConfigDir;
    }

    public void setOverrideConfigDir(File overrideConfigDir) {
        this.overrideConfigDir = overrideConfigDir;
    }

    /**
     * @deprecated config dir is injected in the action. n the configuration you may specify only an override directory
     */
    public File getConfigDir() {
        return overrideConfigDir;
    }

    /**
     * Setter for the configuration directory.<br/>
     * Note that when this setter is called, {@link #workingDirectory} is set as well.
     *
     * @param  configDir the directory where the Action can find its static configuration files.
     * @deprecated config dir is injected in the action. n the configuration you may specify only an override directory
     */
    public void setConfigDir(File configDir) {
        this.overrideConfigDir = configDir;
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

        bc.listenerIds = listenerIds == null ? 
                new ArrayList<String>() : 
                new ArrayList<String>(listenerIds);

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
