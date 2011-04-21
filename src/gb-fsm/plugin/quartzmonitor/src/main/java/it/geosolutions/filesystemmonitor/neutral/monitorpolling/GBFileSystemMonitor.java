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

import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemListener;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitor;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorSPI;

import java.io.File;
import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.event.EventListenerList;

import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GBFileSystemMonitor implements FileSystemMonitor {
    private final static Logger LOGGER = LoggerFactory.getLogger(GBFileSystemMonitor.class);

    // JOB

    String jobName = null;

    String jobGroup = null;

    JobDetail jobDetail = null;

    JobDataMap jdm = null;

    // TRIGGER

    Trigger trigger = null;

    String triggerName = null;

    /*
     * status (means isPaused() or !isPaused() do not regard start() or stop() status
     */

    private boolean pause = false;

    // the stateful GBFileSystemMonitorJob job

    // private GBFileSystemMonitorJob fsm=null;

    /*
     * The list of reveled events. this is used to detach the poller thread job from the event
     * delivery
     */

    private EventListenerList listeners = new EventListenerList();

    /*
     * the event consumer, this is used to pass events to the events listener list
     */

    GBEventNotifier consumer = null;

    /*
     * This is discussed on 17 jan 2011 with Carlo Cancellieri and Alessio Fabiani
     * 
     * The scheduler is a singleton and is used by all the GBFileSystemMonitor instances.
     * 
     * This is due to the GeoBatch architecture.
     */
    // the scheduler
    private static Scheduler sched = null;

    // a reentrant lock to synchronize scheduler accesses
    private static Lock lock = new ReentrantLock();

    /*
     * key used to store and retrieve the number of FS Job still active in the context of this
     * scheduler (actually, using SimpleScheduler it's the 'DEFAULT' one)
     */
    private static String FS_JOBS_NUM_KEY = "FS_JOB_NUM";

    /**
     * TODO get a personalized scheduler using a properties file
     * 
     * Return the scheduler singleton rebuilding it if it is necessary
     * 
     * @return the Scheduler (singleton)
     * @throws SchedulerException
     *             from the scheduler factory
     * @throws InterruptedException
     *             if the lock can't be locked
     */
    private Scheduler getScheduler() throws SchedulerException, InterruptedException {
        if (sched == null) {
            try {
                lock.tryLock(GBFileSystemMonitorSPI.DEFAULT_MAX_LOOKING_INTERVAL,
                        TimeUnit.MILLISECONDS);
                if (sched == null) {
                    try {
                        sched = StdSchedulerFactory.getDefaultScheduler();
                    } catch (SchedulerException e) {
                        if (LOGGER.isErrorEnabled())
                            LOGGER.error(e.getMessage(), e);
                        throw e;
                    }
                }
            } catch (InterruptedException ie) {
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("SchedulerException - unable to get the lock on the scheduler.\n"
                            + ie.getLocalizedMessage(), ie);
                throw ie;
            } finally {
                lock.unlock();
            }
        }
        return sched;
    }

    public GBFileSystemMonitor(final String path, final String wildcard,
            final FileSystemEventType type, final String pollingInterval,
            final boolean lockInputFiles, final long maxLockingWait) throws SchedulerException,
            NullPointerException {
        // fsm=new GBFileSystemMonitorJob();

        /*
         * Discussed on 17 01 2011 with Carlo Cancellieri and Simone Giannecchini
         * 
         * Each FSM job can watch into the same dir but should use a different wildcard. Note that
         * in the case we have: - path1: /home/user/ wildcard: a* - path2: /home/user/ wildcard: *
         * rule is respected but some files of the first FSM job can be handled by the 2nd!
         * 
         * No check on this condition is performed
         */
        if (wildcard != null && wildcard.length() > 0)
            jobName = wildcard;
        else
            throw new NullPointerException(
                    "GBFileSystemMonitor: Could not start a GBFileSystemMonitorJob job using a null or empty wildcard: "
                            + wildcard);

        if (path != null && path.length() > 0)
            jobGroup = path;
        else
            throw new NullPointerException(
                    "GBFileSystemMonitor: Could not start a GBFileSystemMonitorJob job using a null or empty path: "
                            + path);

//      triggerName = jobName + jobGroup;

        jobDetail = JobBuilder.newJob(GBFileSystemMonitorJob.class).withIdentity(jobName, jobGroup).build();

        // consumer=new GBEventNotifier(lockInputFiles, maxLockingWait, listeners);
        consumer = new GBEventNotifier(listeners, type);

        // setting the JobDataMap to initialize the job
        jdm = jobDetail.getJobDataMap();
        if (jdm != null) {
            jdm.put(FileSystemMonitorSPI.SOURCE_KEY, path);
            jdm.put(FileSystemMonitorSPI.WILDCARD_KEY, wildcard);
            // jdm.put(FileSystemMonitorSPI.TYPE_KEY, type); // this is only used by the notifier
            jdm.put(GBFileSystemMonitorJob.WAITING_LOCK_TIME_KEY, maxLockingWait);
            jdm.put(GBFileSystemMonitorJob.EVENT_NOTIFIER_KEY, consumer);
        } else
            throw new NullPointerException(
                    "GBFileSystemMonitor: Could not start a GBFileSystemMonitorJob the corresponding JobDataMap is null.");

        // a SimpleTrigger to start the job indefinitely number of times with pollingInterval
        // interval
        if (pollingInterval==null){
         // use the default value
            trigger = TriggerBuilder.newTrigger().withIdentity(new TriggerKey(path, wildcard))
            .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(FileSystemMonitorSPI.INTERVAL_DEFAULT_POLLING)).startAt(DateBuilder.evenSecondDateAfterNow())
                    .build();
        }
        else {
            try {
                //lets try to parse a cron string
            trigger = TriggerBuilder.newTrigger().withIdentity(new TriggerKey(path, wildcard))
            .withSchedule(CronScheduleBuilder.cronSchedule(pollingInterval)).startAt(DateBuilder.evenSecondDateAfterNow())
                    .build();
            }
            catch (ParseException e) {
                try {
                    // FAILED to parse a cron string
                    // lets try to parse an int
                    final int interval=Integer.parseInt(pollingInterval);
                    trigger = TriggerBuilder.newTrigger().withIdentity(new TriggerKey(path, wildcard))
                    .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(interval)).startAt(DateBuilder.evenSecondDateAfterNow())
                            .build();
                }
                catch (NumberFormatException nfe){
                    // FAILED: use the default value
                    trigger = TriggerBuilder.newTrigger().withIdentity(new TriggerKey(path, wildcard))
                    .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(FileSystemMonitorSPI.INTERVAL_DEFAULT_POLLING)).startAt(DateBuilder.evenSecondDateAfterNow())
                            .build();
                }
            }
        }


    }

    /**
     * Start the FSM job scan
     */
    public void start() {
        try {
            if (!getScheduler().isStarted()) {
                getScheduler().start();
            }
            // System.out.print("START");
            try {

                if (pause) {
                    getScheduler().resumeJob(jobDetail.getKey());
                    pause = false;
                } else {
                    // schedule the job
                    getScheduler().scheduleJob(jobDetail, trigger);
                    int numJob = 0;
                    if (getScheduler().getContext().containsKey(FS_JOBS_NUM_KEY)) {
                        numJob = getScheduler().getContext().getInt(FS_JOBS_NUM_KEY);
                    }
                    getScheduler().getContext().put(FS_JOBS_NUM_KEY, ++numJob);
                }

            } catch (SchedulerException e) {
                /*
                 * SchedulerException - if the Job or Trigger cannot be added to the Scheduler, or
                 * there is an internal Scheduler error.
                 */
                if (LOGGER.isErrorEnabled())
                    LOGGER.error("GBFileSystemMonitor.start(): SchedulerException - if the Job or Trigger cannot be "
                            + "added to the Scheduler, or there is an internal Scheduler error.\n"
                            + e.getLocalizedMessage(), e);
                throw e;
            }
        } catch (SchedulerException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    public void stop() {
        try {
            // System.out.print("STOP");
            if (getScheduler().isStarted() && !pause) {
                getScheduler().deleteJob(jobDetail.getKey());
                int numJob = getScheduler().getContext().getInt(FS_JOBS_NUM_KEY);
                getScheduler().getContext().put(FS_JOBS_NUM_KEY, --numJob);
            } else
                throw new SchedulerException(
                        "GBFileSystemMonitor: The job is already stopped or the scheduler is down");
        } catch (SchedulerException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    // TODO check when is this method called?
    public void pause() {
        try {
            // System.out.print("PAUSE");
            if (getScheduler().isStarted() && !pause) {
                pause = true;
                getScheduler().pauseJob(jobDetail.getKey());
            } else
                throw new SchedulerException(
                        "GBFileSystemMonitor: The job is already paused or the scheduler is down");
        } catch (SchedulerException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        } catch (InterruptedException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * TODO: check: no sense to remove and re-add with the same trigger so no operation is
     * performed.
     * 
     * @see getScheduler().rescheduleJob(triggerName, jobGroup,trigger);
     */
    public void reset() {
        // System.out.print("RESET");
        // try {

        // if (!getScheduler().isStarted()){
        // getScheduler().start();
        // }

        // getScheduler().rescheduleJob(triggerName, jobGroup,trigger);
        // number of jobs may vary if readd fails

        // } catch (SchedulerException e) {
        // if (LOGGER.isErrorEnabled())
        // LOGGER.error(e.getLocalizedMessage());
        // } catch (InterruptedException e) {
        // if (LOGGER.isErrorEnabled())
        // LOGGER.error(e.getLocalizedMessage());
        // }
    }

    public boolean isRunning() {
        return !pause;
    }

    public boolean isPaused() {
        return pause;
    }

    public void dispose() {
        try {

            // System.out.print("DISPOSE");

            /*
             * if the job is NOT the last in its group do not stop the scheduler
             */
            int numJob = getScheduler().getContext().getInt(FS_JOBS_NUM_KEY);
            if (numJob > 1) {
                getScheduler().unscheduleJob(trigger.getKey());
            } else {
                getScheduler().shutdown();
                // to make getScheduler() able to rebuild the scheduler
                sched = null;
            }

            getScheduler().getContext().put(FS_JOBS_NUM_KEY, (--numJob < 0) ? 0 : numJob);

            if (listeners != null) {
                Object[] listenerArray = listeners.getListenerList();
                final int length = listenerArray.length;
                for (int i = length - 2; i >= 0; i -= 2) {
                    if (listenerArray[i] == FileSystemListener.class) {
                        listeners.remove(FileSystemListener.class,
                                (FileSystemListener) listenerArray[i + 1]);
                    }
                }
            }
            listeners = null;

            if (consumer != null) {
                consumer.stop();
            }
            consumer = null;

        } catch (SchedulerException e) {
            if (LOGGER.isWarnEnabled())
                LOGGER.warn("GBFileSystemMonitor: Unable to stop the scheduler: "
                        + e.getLocalizedMessage());
        } catch (InterruptedException e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("GBFileSystemMonitor: " + e.getLocalizedMessage(), e);
        }
    }

    public File getFile() {
        return new File(jobGroup);
    }

    public String getWildCard() {
        return jobName;
    }

    public void addListener(FileSystemListener fileListener) {
        try {
            if (fileListener != null) {
                // Guaranteed to return a non-null array
                final Object[] listenerArray = listeners.getListenerList();
                // Don't add if its already there
                final int length = listenerArray.length;
                for (int i = length - 2; i >= 0; i -= 2) {
                    if (listenerArray[i].equals(fileListener)) {
                        return;
                    }
                }
                // add
                listeners.add(FileSystemListener.class, fileListener);
            } else
                throw new NullPointerException("GBFileSystemMonitor: Unable to add a NULL listener");
        } catch (Throwable t) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error(
                        "GBFileSystemMonitor: Error adding a listener.\n" + t.getLocalizedMessage(),
                        t);
        }
    }

    public void removeListener(FileSystemListener fileListener) {
        try {
            if (fileListener != null)
                listeners.remove(FileSystemListener.class, fileListener);
        } catch (Throwable t) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("GBFileSystemMonitor: Unable to remove the listener: " + fileListener
                        + " message:\n" + t.getLocalizedMessage(), t);
        }
    }

}
