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
package it.geosolutions.filesystemmonitor.neutral.monitorpolling;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;
import it.geosolutions.geobatch.tools.file.IOUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

public class GBFileSystemMonitorJob implements StatefulJob {
    private final static Logger LOGGER = LoggerFactory.getLogger(GBFileSystemMonitorJob.class);

    // protected static final String ROOT_PATH_KEY=FileSystemMonitorSPI.SOURCE_KEY;
    // protected static final String WILDCARD_KEY=FileSystemMonitorSPI.WILDCARD_KEY;
    // protected static final String EVENT_TYPE_KEY=FileSystemMonitorSPI.TYPE_KEY;

    protected static final String OBSERVER_KEY = "OBSERVER";

    protected static final String EVENT_NOTIFIER_KEY = "EVENT_NOTIFIER";

    // KEY time to wait to get the lock
    protected static final String WAITING_LOCK_TIME_KEY = "WAITING_LOCK_TIME";
    // VALUE time to wait to get the lock
    protected static final long WAITING_LOCK_TIME_DEFAULT = IOUtils.MAX_WAITING_TIME_FOR_LOCK; // milliseconds

    /**
     * Define a policy. Refer to the Quartz Exception Handler documentation to define a new policy
     * 
     * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
     */
    private enum ExcPolicy { // Exceptions Policy
        
        IMMEDIATELY
        // throw immediately
    }; // Exceptions Policy

    /*
     * The lock to synchronize multiple calls to this job instance
     */
    
    private Lock lock = new ReentrantLock();

    /**
     * This is called by the JobDetail constructor, you'll never should call this constructor by
     * hand.
     */
    public GBFileSystemMonitorJob() {
    }

    /**
     * Decide the Exception policy for this job
     * 
     * @param message
     *            - The message to use handling the exception
     * @param policy
     *            an ExcPolicy selecting the desired policy to apply
     * @return a JobExecutionException or null depending on the selected policy
     * @throws JobExecutionException
     *             can throw the JobExecutionException depending on the selected policy
     */
    private static JobExecutionException exceptionPolicy(ExcPolicy policy, String message,
            Throwable cause) throws JobExecutionException {
        JobExecutionException jee;

        if (cause != null)
            jee = new JobExecutionException(message, cause);
        else
            jee = new JobExecutionException(message);

        switch (policy) {
        case IMMEDIATELY:
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            jee.refireImmediately();
            break;
        // TODO OTHER POLICY HERE
        default:
            if (LOGGER.isErrorEnabled())
                LOGGER.error(message);
            jee.refireImmediately();
            break;
        }
        return jee;
    }

    /**
     * Commodity to call the default exception policy
     * 
     * @param message
     *            - The message to use handling the exception
     * @return a JobExecutionException or null depending on the default policy
     * @throws JobExecutionException
     *             can throw the JobExecutionException depending on the default policy
     */
    private static JobExecutionException exceptionPolicy(String message)
            throws JobExecutionException {
        return exceptionPolicy(null, message, null);
    }

    /**
     * Commodity to call the default exception policy
     * 
     * @param message
     *            - The message to use handling the exception
     * @return a JobExecutionException or null depending on the default policy
     * @throws JobExecutionException
     *             can throw the JobExecutionException depending on the default policy
     */
    private static JobExecutionException exceptionPolicy(ExcPolicy policy, String message)
            throws JobExecutionException {
        return exceptionPolicy(policy, message, null);
    }

    /**
     * Try to obtain the observer from a previous stored job
     * 
     * @param jdm
     * @return
     * @throws JobExecutionException
     */
    private static FileAlterationObserver getObserver(JobDataMap jdm) throws JobExecutionException {
        Object obj = null;
        if ((obj = jdm.get(OBSERVER_KEY)) != null) {
            if (obj instanceof FileAlterationObserver)
                return (FileAlterationObserver) obj;
            else {
                throw exceptionPolicy(ExcPolicy.IMMEDIATELY,
                        "Unable to get previously generated observer!");
            }
        } else
            return null;

    }

    /**
     * Try to build the Observer using informations stored into the JobDataMap
     * 
     * @note this method is not sync
     * @param jdm
     * @return
     * @throws JobExecutionException
     */
    private static FileAlterationObserver buildObserver(JobDataMap jdm)
            throws JobExecutionException {

        FileAlterationObserver observer = null;
        GBEventNotifier notifier = null;
        // first time build
        try {
            File directory = new File(jdm.getString(FileSystemMonitorSPI.SOURCE_KEY));

            observer = new FileAlterationObserver(directory, new WildcardFileFilter(
                    jdm.getString(FileSystemMonitorSPI.WILDCARD_KEY)));

            notifier = (GBEventNotifier) jdm.get(EVENT_NOTIFIER_KEY);

            FileAlterationListener fal = new GBFileAlterationListener(notifier);
            observer.addListener(fal);
        } catch (ClassCastException cce) {
            // ClassCastException - if the identified object is not a String.
            throw exceptionPolicy(ExcPolicy.IMMEDIATELY, "The identified object is not a String.\n"
                    + cce.getLocalizedMessage(), cce);

        } catch (NullPointerException npe) {
            // NullPointerException - If the pathname argument is null
            throw exceptionPolicy(ExcPolicy.IMMEDIATELY,
                    "The pathname argument is null.\n" + npe.getLocalizedMessage(), npe);
        } catch (IllegalArgumentException iae) {
            // IllegalArgumentException - if the pattern is null
            throw exceptionPolicy(ExcPolicy.IMMEDIATELY,
                    "The pattern is null.\n" + iae.getLocalizedMessage(), iae);
        } catch (Throwable e) {
            throw exceptionPolicy(ExcPolicy.IMMEDIATELY, "Probably the consumer cannot start.\n"
                    + e.getLocalizedMessage(), e);
        }

        try {
            observer.initialize();
        } catch (Throwable t) {
            throw exceptionPolicy(ExcPolicy.IMMEDIATELY,
                    "An error occurs.\n" + t.getLocalizedMessage(), t);
        }

        jdm.put(OBSERVER_KEY, observer);

        return observer;
    }

    /**
     * the job. You'll never may call this method manually, (this is executed by the quartz
     * Scheduler).
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (LOGGER.isTraceEnabled()){
            final JobDetail detail = context.getJobDetail();
            LOGGER.trace("Starting FSM job named: " + detail.getGroup() + detail.getName());
        }

        final JobDataMap jdm = context.getJobDetail().getJobDataMap();
        FileAlterationObserver observer = null;

        if ((observer = getObserver(jdm)) == null) {
            // System.out.println("if1 done sync");
            try {
                long wait = WAITING_LOCK_TIME_DEFAULT; // default wait in seconds
                try {
                    wait = jdm.getLong(WAITING_LOCK_TIME_KEY);
                } catch (ClassCastException cce) {
                    // NOT HANDLED: using default value
                }
                // System.out.println("WAIT"+wait);
                lock.tryLock(wait, TimeUnit.MILLISECONDS);
                // System.out.println("if1 + synch done if2");
                if ((observer = getObserver(jdm)) == null) {
                    // System.out.println("all is done building");
                    if (LOGGER.isInfoEnabled())
                        LOGGER.info("Building the observer tree...");

                    observer = buildObserver(jdm);

                    if (LOGGER.isInfoEnabled())
                        LOGGER.info("Observer tree complete.");
                }
            } catch (InterruptedException ie) {
                // NOT HANDLED
                // DEBUG
                // ie.printStackTrace();
            } catch (Throwable t) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("GBFileSystemMonitorJob JOB throws a throwable: "
                                    + t.getLocalizedMessage(), t);
                // DEBUG
                // t.printStackTrace();
            } finally {
                lock.unlock();
            }
            // System.out.println("build");

        } // first time initializer ends

        // do the job
        observer.checkAndNotify();
        // DEBUG
        // System.out.println("DOTHEJOB");

        if (LOGGER.isTraceEnabled()){
            final JobDetail detail = context.getJobDetail();
            LOGGER.trace("job named: " + detail.getGroup() + detail.getName() + " completed");
        }
    }
}
