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
package it.geosolutions.geobatch.flow.event;

import java.util.EventListener;

/**
 * Listener interface for monitorable object.
 * 
 * <P>
 * This Listner is designed to have stateful information, such as current task running or current
 * progress percentage, so it should be bound to one object only.
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public interface IProgressListener extends EventListener {

    /**
     * The process has been started
     */
    void started();

    /**
     * The process has just advanced a bit.
     * 
     * @see #getProgress()
     * @see #getTask()
     */
    void progressing();

    /**
     * The process has been paused
     */
    void paused();

    /**
     * The process has been resumed
     */
    void resumed();

    /**
     * The process has successfully ended.
     */
    void completed();

    /**
     * The process has ended with an error
     */
    void failed(Throwable exception);

    /**
     * The process has been terminated
     */
    void terminated();

    /**
     * Get a progress indicator, between 0.0 and 100.0
     */
    float getProgress();

    /**
     * Get the name of the current task.
     */
    String getTask();

    /**
     * Used by the notifier.
     */
    void setProgress(float progress);

    /**
     * Used by the notifier.
     */
    void setTask(String currentTask);

}
