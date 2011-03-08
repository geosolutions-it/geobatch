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

package it.geosolutions.geobatch.flow.event.listeners.logger;

import it.geosolutions.geobatch.catalog.Identifiable;
import it.geosolutions.geobatch.flow.event.ProgressListener;

import java.util.logging.Logger;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class LoggingProgressListener extends ProgressListener {

    public LoggingProgressListener(LoggingProgressListenerConfiguration configuration, Identifiable owner) {
        super(configuration,owner);
    }
    
    public LoggingProgressListenerConfiguration getConfig(){
        return (LoggingProgressListenerConfiguration) configuration;
    }

    private Logger getLogger() {
        return Logger.getLogger(getConfig().getLoggerName());
    }

    public void started() {
        getLogger().info("Started [" + getOwner().getName() + "]");
    }

    public void progressing() {
        getLogger()
                .info("Progressing " + getProgress() + "% -- " + getTask() + " [" + getOwner().getName() + "]");
    }

    public void paused() {
        getLogger().info("Paused " + getProgress() + "% -- " + getTask() + " [" + getOwner().getName() + "]");
    }

    public void resumed() {
        getLogger().info("Resumed " + getProgress() + "% -- " + getTask() + " [" + getOwner().getName() + "]");
    }

    public void completed() {
        getLogger().info("Completed [" + getOwner().getName() + "]");
    }

    public void failed(Throwable exception) {
        getLogger().info("Failed " + getProgress() + "% -- " + getTask() + " [" + getOwner().getName() + "]");
    }

    public void terminated() {
        getLogger().info("Terminated " + getProgress() + "% -- " + getTask() + " [" + getOwner().getName() + "]");
    }

}
