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
package it.geosolutions.geobatch.tools.file;

import it.geosolutions.geobatch.tools.Conf;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple class implementing a Thread that periodically tries to delete the files that
 * were provided to him.
 * <p>
 * It tries to delete each file at most {@link FileRemover#maxAttempts} number of times. If this
 * number is exceeded it simply throws the file away notifying the users with a warning message.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * 
 * @deprecated Use FileGarbageCollector instead
 */
public final class FileRemover extends Thread {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(Path.class.toString());

    private final static Map<String, Integer> FILE_ATTEMPTS_COUNTS = Collections
            .synchronizedMap(new HashMap<String, Integer>());

    /**
     * Maximum number of attempts to delete a given {@link File}.
     * 
     * <p>
     * If the provided number of attempts is exceeded we simply drop warn the user and we remove
     * the {@link File} from our list.
     */
    private int maxAttempts = Conf.DEF_MAX_ATTEMPTS;

    /**
     * Period in seconds between two checks.
     */
    private volatile long period = Conf.DEFAULT_PERIOD;
    
    private final static Set<String> FILES_PATH = Collections
    .synchronizedSet(new HashSet<String>());
    
    public static List<File> collectOlder(final long time,final int daysAgo,final File root){
    	if (daysAgo<0){
    		return null;
    	}
    	Calendar cal=Calendar.getInstance();
    	cal.setTimeInMillis(time);
    	int days=cal.get(Calendar.DAY_OF_YEAR);
    	if (days>=daysAgo)
    		cal.set(Calendar.DAY_OF_YEAR, days-daysAgo);
    	else {
    		// TODO use  getActualMaximum for days
    		cal.set(Calendar.DAY_OF_YEAR, (354+(days-daysAgo)));
    		cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)-1);
    	}
//    	cal.getTime().toString()
    	final Collector coll=new Collector(FileFilterUtils.andFileFilter(FileFilterUtils.directoryFileFilter(),FileFilterUtils.ageFileFilter(cal.getTime(), true)),1);
    	return coll.collect(root);
    }

    /**
     * Asks this {@link FileRemover} to clean up this file.
     * 
     * @param fileToDelete
     *            {@link File} that we want to permanently delete.
     */
    public void addFile(final File fileToDelete) {
        // does it exists
        if (!fileToDelete.exists())
            return;
        synchronized (FILES_PATH) {
            synchronized (FILE_ATTEMPTS_COUNTS) {
                // /////////////////////////////////////////////////////////////////
                //
                // We add the file to our lists for later check.
                //
                // /////////////////////////////////////////////////////////////////
                if (!FILES_PATH.contains(fileToDelete.getAbsolutePath())) {
                    FILES_PATH.add(fileToDelete.getAbsolutePath());
                    FILE_ATTEMPTS_COUNTS.put(fileToDelete.getAbsolutePath(), new Integer(0));

                }
            }
        }
    }

    /**
     * Default constructor for a {@link FileRemover}.
     */
    public FileRemover() {
        this(Conf.DEFAULT_PERIOD, Thread.NORM_PRIORITY - 3, Conf.DEF_MAX_ATTEMPTS);
    }

    /**
     * Constructor for a {@link FileRemover}.
     * 
     * @param period
     *            default time period between two cycles.
     * @param priority
     *            is the priority for the cleaner thread.
     * @param maxattempts
     *            maximum number of time the cleaner thread tries to delete a file.
     */
    public FileRemover(long period, int priority, int maxattempts) {
        this.period = period;
        this.setName("FileRemover");
        this.setPriority(priority);
        this.setDaemon(true);
        this.maxAttempts = maxattempts;
    }

    /**
     * This method does the magic:
     * 
     * <ol>
     * <li>iterate over all the files</li>
     * <li>try to delete it</li>
     * <li>if successful drop the file references</li>
     * <li>if not successful increase the attempts count for the file and call the gc. If the
     * maximum number was exceeded drop the file and warn the user</li>
     * 
     */
    public void run() {
        while (true) {
            try {
                synchronized (FILES_PATH) {
                    synchronized (FILE_ATTEMPTS_COUNTS) {

                        final Iterator<String> it = FILES_PATH.iterator();
                        while (it.hasNext()) {

                            // get next file path and its count
                            final String sFile = it.next();
                            if (LOGGER.isInfoEnabled())
                                LOGGER.info("Trying to remove file " + sFile);
                            int attempts = FILE_ATTEMPTS_COUNTS.get(sFile).intValue();
                            if (!new File(sFile).exists()) {
                                it.remove();
                                FILE_ATTEMPTS_COUNTS.remove(sFile);
                            } else {
                                // try to delete it
                                if (new File(sFile).delete()) {
                                    if (LOGGER.isInfoEnabled())
                                        LOGGER.info("Successfully removed file " + sFile);
                                    it.remove();
                                    FILE_ATTEMPTS_COUNTS.remove(sFile);
                                } else {
                                    if (LOGGER.isInfoEnabled())
                                        LOGGER.info("Unable to  remove file " + sFile);
                                    attempts++;
                                    if (maxAttempts < attempts) {
                                        if (LOGGER.isInfoEnabled())
                                            LOGGER.info("Dropping file " + sFile);
                                        it.remove();
                                        FILE_ATTEMPTS_COUNTS.remove(sFile);
                                        if (LOGGER.isWarnEnabled())
                                            LOGGER.warn("Unable to delete file " + sFile);
                                    } else {
                                        FILE_ATTEMPTS_COUNTS.remove(sFile);
                                        FILE_ATTEMPTS_COUNTS.put(sFile, new Integer(attempts));
                                        // might help, see
                                        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4715154
                                        Runtime.getRuntime().gc();
                                        Runtime.getRuntime().gc();
                                        Runtime.getRuntime().gc();
                                        Runtime.getRuntime().gc();
                                        Runtime.getRuntime().gc();
                                        Runtime.getRuntime().gc();
                                        System.runFinalization();
                                        System.runFinalization();
                                        System.runFinalization();
                                        System.runFinalization();
                                        System.runFinalization();
                                        System.runFinalization();

                                    }
                                }
                            }
                        }
                    }
                }
                Thread.sleep(period * 1000);

            } catch (Throwable t) {
                if (LOGGER.isInfoEnabled())
                    LOGGER.info(t.getLocalizedMessage(), t);
            }
        }
    }

    /**
     * Retrieves the maximum number of times we try to delete a file before giving up.
     * 
     * @return the maximum number of times we try to delete a file before giving up.
     * 
     */
    public int getMaxAttempts() {
        synchronized (FILES_PATH) {
            synchronized (FILE_ATTEMPTS_COUNTS) {
                return maxAttempts;
            }
        }

    }

    /**
     * Sets the maximum number of times we try to delete a file before giving up.
     * 
     * @param maxAttempts
     *            the maximum number of times we try to delete a file before giving up.
     * 
     */
    public void setMaxAttempts(int maxAttempts) {
        synchronized (FILES_PATH) {
            synchronized (FILE_ATTEMPTS_COUNTS) {
                this.maxAttempts = maxAttempts;
            }
        }

    }

    /**
     * Retrieves the period in seconds for this {@link FileRemover} .
     * 
     * @return the period in seconds for this {@link FileRemover} .
     * 
     */
    public long getPeriod() {
        return period;
    }

    /**
     * Sets the period in seconds for this {@link FileRemover} .
     * 
     * @param period
     *            the new period for this {@link FileRemover} .
     * 
     */
    public void setPeriod(long period) {
        this.period = period;
    }

}
