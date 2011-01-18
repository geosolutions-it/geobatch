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
package it.geosolutions.filesystemmonitor.monitor.thread;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Alessio
 * @deprecated This interface will be removed soon
 */
public abstract class AbstractPausableThread extends Thread {

    protected final static Logger LOGGER = Logger
            .getLogger("it.geosolutions.filesystemmonitor.monitor.thread");

    public AbstractPausableThread(String name) {
        super(name);
    }

    /**
     * Indicates whether or not this thread is running.
     * 
     * @uml.property name="isPaused"
     */
    protected volatile boolean isPaused = false;

    /**
     * Indicates that we are requesting a pause.
     * 
     * @uml.property name="pauseRequested"
     */
    protected volatile boolean pauseRequested;

    /**
     * Indicates whether or not this thread is running.
     * 
     * @uml.property name="isRunning"
     */
    protected volatile boolean isRunning = false;

    /**
     * Indicates if a termination was requested to this thread.
     * 
     * @uml.property name="terminationRequested"
     */
    protected volatile boolean terminationRequested = false;

    /**
     * @return
     * @uml.property name="isPaused"
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * @return
     * @uml.property name="pauseRequested"
     */
    public boolean isPauseRequested() {

        return this.pauseRequested;

    }

    public void requestPause() {
        pauseRequested = true;

    }

    /**
     * @param isPaused
     * @uml.property name="isPaused"
     */
    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    /**
     * @param pauseRequested
     * @uml.property name="pauseRequested"
     */
    public void setPauseRequested(boolean pauseRequested) {
        this.pauseRequested = pauseRequested;
    }

    public void start() {
        if (isPaused)
            synchronized (this) {
                setPauseRequested(false);
                notify();

            }
        else if (!isRunning)
            super.start();
    }

    /**
     * @return
     * @uml.property name="isRunning"
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * @param isRunning
     * @uml.property name="isRunning"
     */
    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    /**
     * Checks whether any request has been received asking the working thread stop monitoring file
     * system changes.
     * 
     * @return True if at least one request has been received.
     * @uml.property name="terminationRequested"
     */
    public boolean isTerminationRequested() {

        return this.terminationRequested;

    }

    /**
     * Requests the worker thread to stop monitoring file system changes.
     */
    public void requestTermination() {

        this.terminationRequested = true;
    }

    /**
     * @param terminationRequested
     * @uml.property name="terminationRequested"
     */
    public void setTerminationRequested(boolean terminationRequested) {
        this.terminationRequested = terminationRequested;
    }

    public abstract boolean execute() throws Exception;

    public abstract void dispose();

    public void run() {
        isRunning = true;

        while (true) {
            if (isTerminationRequested()) {
                break;
            }

            try {
                if (!execute())
                    break;
            } catch (Exception e1) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
                break;
            }
            synchronized (this) {
                try {
                    while (isPauseRequested()) {
                        pauseRequested = false;
                        isPaused = true;
                        this.wait();
                        if (!pauseRequested) {
                            break;
                        }

                    }

                } catch (InterruptedException e) {
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
                    break;
                }

            }
        }
        isRunning = false;
    }

    public void reset() {
        pauseRequested = false;
        isPaused = false;
        isRunning = false;
        terminationRequested = false;
    }

}
