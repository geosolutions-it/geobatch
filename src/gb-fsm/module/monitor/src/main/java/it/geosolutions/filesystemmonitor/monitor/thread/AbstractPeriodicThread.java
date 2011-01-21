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

/**
 * This is the timer thread which is executed every n milliseconds according to the setting of the
 * file monitor. It investigates the file in question and notify listeners if changed.
 * 
 * @author SImone Giannecchini
 * @deprecated This interface will be removed soon
 */
public abstract class AbstractPeriodicThread extends AbstractPausableThread {

    /**
     * Interval at which we will check the file system.
     * @uml.property  name="pollingInterval"
     */
    protected long pollingInterval = 10;

    public AbstractPeriodicThread(String name, final long pollingInterval) {
        super(name);
        this.pollingInterval = pollingInterval;

    }

    /**
     * @return
     * @uml.property name="pollingInterval"
     */
    public long getPollingInterval() {
        return pollingInterval;
    }

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

            try {
                while (isPauseRequested()) {
                    pauseRequested = false;
                    isPaused = true;
                    synchronized (this) {
                        this.wait();
                    }

                    if (!pauseRequested)
                        break;

                }
                synchronized (this) {
                    this.wait(pollingInterval);
                }
            } catch (InterruptedException e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
                break;
            }

        }
        isRunning = false;
    }

}