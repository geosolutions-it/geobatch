/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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
package it.geosolutions.geobatch.flow.event.action;

import it.geosolutions.geobatch.catalog.impl.BaseDescriptable;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.FlowManager;
import it.geosolutions.geobatch.flow.Job;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 * @author Emanuele Tajariol <etj AT geo-solutions DOT it>, GeoSolutions S.A.S.
 * @author (r2) Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version r1<br>
 *          r2 - on: 26 Aug 2011<br>
 * 
 * @param <XEO> Kind of EventObject to be eXecuted
 */
public abstract class BaseAction<XEO extends EventObject> 
    extends BaseDescriptable
    implements Action<XEO>, Job {

    private final static Logger LOGGER = LoggerFactory.getLogger(BaseAction.class);

    /**
     * the context where action is running in...<br>
     * this is initialized by the FlowManager
     */
    private String runningContext;

    /**
     * Directory where temp files can be stored. It should be automatically
     * cleaned up by the GB engine when an Action ends successfully.<br>
     * This should be initialized by the FlowManager <br/>
     */
    private File tempDir;
    
    /**
     * Directory where configuration files are be stored.
     * It can be set as:<br>
     * <ul>
     * <li>If it is relative: GEOBATCH_CONFIG_DIR/flowConfigDir/actionConfigDir</li>
     * <li>If it is null: GEOBATCH_CONFIG_DIR/flowConfigDir/actionid</li>
     * <li>If it is absolute: /actionConfigDir</li>
     * </ul><br>
     * To see which values flowConfigDir can be set to take a look here {@link FlowManager#getConfigDir()} 
     */
    private File configDir;

    final protected ProgressListenerForwarder listenerForwarder;

    protected boolean failIgnored = false;

    private ActionConfiguration configuration;
    
    public BaseAction(String id, String name, String description) {
        super(id, name, description);
        listenerForwarder = new ProgressListenerForwarder(this);
        failIgnored = false;
    }

    public BaseAction(ActionConfiguration actionConfiguration) {
        super(actionConfiguration.getId(), 
                actionConfiguration.getName(),
                actionConfiguration.getDescription());

        this.configuration = actionConfiguration;
        listenerForwarder = new ProgressListenerForwarder(this);
        failIgnored = actionConfiguration.isFailIgnored();
    }

    /**
     * @return the runningContext
     */
    public String getRunningContext() {
        return runningContext;
    }

    /**
     * @param runningContext the runningContext to set
     */
    public void setRunningContext(String runningContext) {
        this.runningContext = runningContext;
    }

    public File getTempDir() {
        return tempDir;
    }

    public void setTempDir(File tempDir) {
        this.tempDir = tempDir;
    }

    /**
     * @return the configDir
     */
    public final File getConfigDir() {
        return configDir;
    }

    /**
     * @param configDir the configDir to set
     */
    public final void setConfigDir(File configDir) {
        this.configDir = configDir;
    }

    
    public <T extends ActionConfiguration> T getConfiguration() {
        return (T)configuration; // TODO T should be set at class level
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
            LOGGER.info("Pause(" + sub + ") request for " + getClass().getSimpleName());
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

    public void removeListener(IProgressListener listener) {
        this.listenerForwarder.removeListener(listener);
    }

    public void addListener(IProgressListener listener) {
        this.listenerForwarder.addListener(listener);
    }

    public Collection<IProgressListener> getListeners() {
        return this.listenerForwarder.getListeners();
    }

    public Collection<IProgressListener> getListeners(Class clazz) {
        final Collection<IProgressListener> ret=new ArrayList<IProgressListener>();

        for (IProgressListener ipl : getListeners()) {
            if (clazz.isAssignableFrom(ipl.getClass())){
                ret.add(ipl);
            }
        }

        return ret;
    }
}
