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

package it.geosolutions.geobatch.flow.event.listeners.status;

import it.geosolutions.geobatch.catalog.Identifiable;
import it.geosolutions.geobatch.flow.event.ProgressListener;

/**
 * Remember the state of the event firer.
 * 
 * @author ETj <etj at geo-solutions.it>
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class StatusProgressListener extends ProgressListener {

    protected boolean started = false;

    protected boolean paused = false;

    protected boolean failed = false;

    protected boolean completed = false;

    protected boolean terminated = false;

    protected Throwable failException = null;

    public StatusProgressListener(StatusProgressListenerConfiguration configuration,
            Identifiable owner) {
        super(configuration, owner);
    }

    public void started() {
        started = true;
    }

    /**
     * @return
     * @uml.property name="started"
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * This event should trigger some refresh on interactive displays, but we don't need it
     */
    public void progressing() {
    }

    public void paused() {
        paused = true;
    }

    /**
     * @return
     */
    public boolean isPaused() {
        return paused;
    }

    public void resumed() {
        paused = false;
    }

    public void completed() {
        completed = true;
    }

    /**
     * @return
     */
    public boolean isCompleted() {
        return completed;
    }

    public void failed(Throwable exception) {
        failed = true;
        failException = exception;
    }

    /**
     * @return
     */
    public boolean isFailed() {
        return failed;
    }

    /**
     * @return
     */
    public Throwable getFailException() {
        return failException;
    }

    public void terminated() {
        terminated = true;
    }

    /**
     * @return
     */
    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("Last task: '").append(getTask()).append("' ");
        sb.append(" at ").append(getProgress()).append("% ");
        if (started)
            sb.append("started ");
        if (paused)
            sb.append("paused ");
        if (completed)
            sb.append("completed");
        if (failed)
            sb.append("failed (").append(failException.getMessage()).append(")");
        if (terminated)
            sb.append("terminated");
        sb.append(']');

        return sb.toString();
    }
}
