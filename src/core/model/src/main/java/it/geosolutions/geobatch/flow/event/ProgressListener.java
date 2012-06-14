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
package it.geosolutions.geobatch.flow.event;

import java.io.Serializable;

import it.geosolutions.geobatch.catalog.Identifiable;
import it.geosolutions.geobatch.configuration.event.listener.ProgressListenerConfiguration;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 * @author (r2) Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public abstract class ProgressListener implements IProgressListener, Serializable{

    
    private Identifiable owner;
    
    
    protected ProgressListenerConfiguration configuration;

    
    private String currentTask = "TASK";

    
    private float progress = 0;

    protected ProgressListener(Identifiable caller) {
        this.owner=caller;
        this.configuration = null;
    }
    
    protected ProgressListener(ProgressListenerConfiguration configuration, Identifiable caller) {
        this.owner=caller;
        this.configuration = configuration;
    }

    /**
     * owner must be set
     */
    @SuppressWarnings("unused")
    private ProgressListener() {
    }
    
    public Identifiable getOwner(){
        return owner;
    }

    /**
     * @return
     */
    public float getProgress() {
        return progress;
    }

    public String getTask() {
        return currentTask;
    }

    /**
     * Used by the notifier.
     */
    public void setTask(String currentTask) {
        this.currentTask = currentTask;
    }

    /**
     * Used by the notifier.
     */
    public void setProgress(float progress) {
        this.progress = progress;
    }
}
