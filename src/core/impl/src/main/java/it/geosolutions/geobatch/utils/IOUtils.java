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



package it.geosolutions.geobatch.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.net.URL;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.geotools.data.DataUtilities;

/**
 * Assorted IO related utilities
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 * 
 */
public class IOUtils extends org.apache.commons.io.IOUtils {

    private final static Logger LOGGER = Logger.getLogger(FileCleaner.class.toString());

    /** Default size of element for {@link FileChannel} based copy method. */
    private static final int DEFAULT_SIZE = 10 * 1024 * 1024;

    /** Background to perform file deletions. */
    private final static FileCleaner FILE_CLEANER = new FileCleaner();

    private final static Set<String> FILES_PATH = Collections.synchronizedSet(new HashSet<String>());
    
    public final static String FILE_SEPARATOR = System.getProperty("file.separator");

    private final static Map<String, Integer> FILE_ATTEMPTS_COUNTS = Collections.synchronizedMap(new HashMap<String, Integer>());

    /**
     * 30 seconds is the default period between two checks.
     */
    public static long DEFAULT_PERIOD = 5L;

    /**
     * The default number of attempts is 50
     */
    private final static int DEF_MAX_ATTEMPTS = 50;

    private static final long ATOMIC_WAIT = 5000;

    /**
     * The max time the node will wait for, prior to stop to attempt for acquiring a lock on a
     * <code>File</code>.
     */
    public static final long MAX_WAITING_TIME_FOR_LOCK = 12*60*60 * 1000;//12h
    static {
        FILE_CLEANER.setMaxAttempts(100);
        FILE_CLEANER.setPeriod(30);
        FILE_CLEANER.setPriority(1);
        FILE_CLEANER.start();
    }

    /**
     * Simple class implementing a periodic Thread that periodically tries to delete the files that
     * were provided to him.
     * <p>
     * It tries to delete each file at most {@link FileCleaner#maxAttempts} number of times. If this
     * number is exceeded it simply throws the file away notifying the users with a warning message.
     * 
     * @author Simone Giannecchini, GeoSolutions.
     */
    public final static class FileCleaner extends Thread {

        /**
         * Maximum number of attempts to delete a given {@link File}.
         * 
         * <p>
         * If the provided number of attempts is exceeded we simply drop warn the user and we remove
         * the {@link File} from our list.
         */
        private int maxAttempts = DEF_MAX_ATTEMPTS;

        /**
         * Period in seconds between two checks.
         */
        private volatile long period = DEFAULT_PERIOD;

        /**
         * Asks this {@link FileCleaner} to clean up this file.
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
         * Default constructor for a {@link FileCleaner}.
         */
        public FileCleaner() {
            this(DEFAULT_PERIOD, Thread.NORM_PRIORITY - 3, DEF_MAX_ATTEMPTS);
        }

        /**
         * Constructor for a {@link FileCleaner}.
         * 
         * @param period
         *            default time period between two cycles.
         * @param priority
         *            is the priority for the cleaner thread.
         * @param maxattempts
         *            maximum number of time the cleaner thread tries to delete a file.
         */
        public FileCleaner(long period, int priority, int maxattempts) {
            this.period = period;
            this.setName("FileCleaner");
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
                                if (LOGGER.isLoggable(Level.INFO))
                                    LOGGER.info("Trying to remove file " + sFile);
                                int attempts = FILE_ATTEMPTS_COUNTS.get(sFile).intValue();
                                if (!new File(sFile).exists()) {
                                    it.remove();
                                    FILE_ATTEMPTS_COUNTS.remove(sFile);
                                } else {
                                    // try to delete it
                                    if (new File(sFile).delete()) {
                                        if (LOGGER.isLoggable(Level.INFO))
                                            LOGGER.info("Successfully removed file " + sFile);
                                        it.remove();
                                        FILE_ATTEMPTS_COUNTS.remove(sFile);
                                    } else {
                                        if (LOGGER.isLoggable(Level.INFO))
                                            LOGGER.info("Unable to  remove file " + sFile);
                                        attempts++;
                                        if (maxAttempts < attempts) {
                                            if (LOGGER.isLoggable(Level.INFO))
                                                LOGGER.info("Dropping file " + sFile);
                                            it.remove();
                                            FILE_ATTEMPTS_COUNTS.remove(sFile);
                                            if (LOGGER.isLoggable(Level.WARNING))
                                                LOGGER.warning("Unable to delete file " + sFile);
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
                    if (LOGGER.isLoggable(Level.INFO))
                        LOGGER.log(Level.INFO, t.getLocalizedMessage(), t);
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
         * Retrieves the period in seconds for this {@link FileCleaner} .
         * 
         * @return the period in seconds for this {@link FileCleaner} .
         * 
         */
        public long getPeriod() {
            return period;
        }

        /**
         * Sets the period in seconds for this {@link FileCleaner} .
         * 
         * @param period
         *            the new period for this {@link FileCleaner} .
         * 
         */
        public void setPeriod(long period) {
            this.period = period;
        }

    }

    /**
     * Copies the content of the source channel onto the destination channel.
     * 
     * @param bufferSize
     *            size of the temp buffer to use for this copy.
     * @param source
     *            the source {@link ReadableByteChannel}.
     * @param destination
     *            the destination {@link WritableByteChannel};.
     * @throws IOException
     *             in case something bad happens.
     */
    public static void copyChannel(int bufferSize, ReadableByteChannel source,
            WritableByteChannel destination) throws IOException {

        inputNotNull(source, destination);
        if (!source.isOpen() || !destination.isOpen())
            throw new IllegalStateException("Source and destination channels must be open.");

        final java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocateDirect(bufferSize);
        while (source.read(buffer) != -1) {
            // prepare the buffer for draining
            buffer.flip();

            // write to destination
            while (buffer.hasRemaining())
                destination.write(buffer);

            // clear
            buffer.clear();

        }

    }

    /**
     * Optimize version of copy method for file channels.
     * 
     * @param bufferSize
     *            size of the temp buffer to use for this copy.
     * @param source
     *            the source {@link ReadableByteChannel}.
     * @param destination
     *            the destination {@link WritableByteChannel};.
     * @throws IOException
     *             in case something bad happens.
     */
    public static void copyFileChannel(int bufferSize, FileChannel source, FileChannel destination)
            throws IOException {

        inputNotNull(source, destination);
        if (!source.isOpen() || !destination.isOpen())
            throw new IllegalStateException("Source and destination channels must be open.");
        FileLock lock = null;
        try {

            lock = destination.lock();
            final long sourceSize = source.size();
            long pos = 0;
            while (pos < sourceSize) {
                // read and flip
                final long remaining = (sourceSize - pos);
                final int mappedZoneSize = remaining >= bufferSize ? bufferSize : (int) remaining;
                destination.transferFrom(source, pos, mappedZoneSize);
                // update zone
                pos += mappedZoneSize;

            }
        } finally {
            if (lock != null) {
                try {
                    lock.release();
                } catch (Throwable t) {
                    if (LOGGER.isLoggable(Level.INFO))
                        LOGGER.log(Level.INFO, t.getLocalizedMessage(), t);
                }
            }

        }
    }

    /**
     * Close the specified input <code>FileChannel</code>
     * 
     * @throws IOException
     *             in case something bad happens.
     */
    public static void closeQuietly(Channel channel) throws IOException {
        inputNotNull(channel);
        if (channel.isOpen())
            channel.close();
    }

    /**
     * Checks if the input is not null.
     * 
     * @param oList
     *            list of elements to check for null.
     */
    private static void inputNotNull(Object... oList) {
        for (Object o : oList)
            if (o == null)
                throw new NullPointerException("Input objects cannot be null");

    }

    /**
     * Copy the input file onto the output file using a default buffer size.
     * 
     * @param sourceFile
     *            the {@link File} to copy from.
     * @param destinationFile
     *            the {@link File} to copy to.
     * @throws IOException
     *             in case something bad happens.
     */
    public static void copyFile(File sourceFile, File destinationFile) throws IOException {
        copyFile(sourceFile, destinationFile, DEFAULT_SIZE);
    }

    /**
     * Copy the input file onto the output file using the specified buffer size.
     * 
     * @param sourceFile
     *            the {@link File} to copy from.
     * @param destinationFile
     *            the {@link File} to copy to.
     * @param size
     *            buffer size.
     * @throws IOException
     *             in case something bad happens.
     */
    public static void copyFile(File sourceFile, File destinationFile, int size) throws IOException {
        inputNotNull(sourceFile, destinationFile);
        if (!sourceFile.exists() || !sourceFile.canRead() || !sourceFile.isFile())
            throw new IllegalStateException("Source is not in a legal state.");
        if (!destinationFile.exists()) {
            destinationFile.createNewFile();
        }
        if (destinationFile.getAbsolutePath().equalsIgnoreCase(sourceFile.getAbsolutePath()))
            throw new IllegalArgumentException("Cannot copy a file on itself");

        FileChannel source = null;
        FileChannel destination = null;
        source = new RandomAccessFile(sourceFile, "r").getChannel();
        destination = new RandomAccessFile(destinationFile, "rw").getChannel();
        try {
            copyFileChannel(size, source, destination);
        } finally {
            try {
                if (source != null) {
                    try {
                        source.close();
                    } catch (Throwable t) {
                        if (LOGGER.isLoggable(Level.INFO))
                            LOGGER.log(Level.INFO, t.getLocalizedMessage(), t);
                    }
                }
            } finally {
                if (destination != null) {
                    try {
                        destination.close();
                    } catch (Throwable t) {
                        if (LOGGER.isLoggable(Level.INFO))
                            LOGGER.log(Level.INFO, t.getLocalizedMessage(), t);
                    }
                }
            }
        }
    }

    /**
     * Delete all the files/dirs with matching the specified {@link FilenameFilter} in the specified
     * directory. The method can work recursively.
     * 
     * @param sourceDirectory
     *            the directory to delete files from.
     * @param filter
     *            the {@link FilenameFilter} to use for selecting files to delete.
     * @param recursive
     *            boolean that specifies if we want to delete files recursively or not.
     * @return
     */
    public static boolean deleteDirectory(File sourceDirectory, FilenameFilter filter,
            boolean recursive, boolean deleteItself) {
        inputNotNull(sourceDirectory, filter);
        if (!sourceDirectory.exists() || !sourceDirectory.canRead()
                || !sourceDirectory.isDirectory())
            throw new IllegalStateException("Source is not in a legal state.");

        final File[] files = (filter != null ? sourceDirectory.listFiles(filter) : sourceDirectory.listFiles());
        for (File file : files) {
            if (file.isDirectory()) {
                if (recursive)
                    deleteDirectory(file, filter, recursive, deleteItself);
            } else {
                if (!file.delete())
                    return false;
            }
        }
        return deleteItself ? sourceDirectory.delete() : true;

    }

    /**
     * Empty the specified directory. The method can work recursively.
     * 
     * @param sourceDirectory
     *            the directory to delete files/dirs from.
     * @param recursive
     *            boolean that specifies if we want to delete files/dirs recursively or not.
     * @param deleteItself
     *            boolean used if we want to delete the sourceDirectory itself
     * @return
     */
    public static boolean emptyDirectory(File sourceDirectory,
            boolean recursive, boolean deleteItself) {
        inputNotNull(sourceDirectory);
        if (!sourceDirectory.exists() || !sourceDirectory.canRead() || !sourceDirectory.isDirectory()) {
            throw new IllegalStateException("Source is not in a legal state.");
        }

        final File[] files = sourceDirectory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                if (recursive) {
                    if (!emptyDirectory(file, recursive, true)) {//delete subdirs recursively
                        return false;
                    }
                }
            } else {
                if (!file.delete()) {
                    return false;
                }
            }
        }
        return deleteItself ? sourceDirectory.delete() : true;
    }

    /**
     * Delete the specified File.
     * 
     * @param sourceDirectory
     *            the directory to delete files from.
     * @param filter
     *            the {@link FilenameFilter} to use for selecting files to delete.
     * @param recursive
     *            boolean that specifies if we want to delete files recursively or not.
     * @return
     */
    public static void deleteFile(File file) {
        inputNotNull(file);
        if (!file.exists() || !file.canRead() || !file.isFile())
            throw new IllegalStateException("Source is not in a legal state.");

        if (file.delete())
            return;

        IOUtils.FILE_CLEANER.addFile(file);

    }

    /**
     * Get an input <code>FileChannel</code> for the provided <code>File</code>
     * 
     * @param file
     *            <code>File</code> for which we need to get an input <code>FileChannel</code>
     * @return a <code>FileChannel</code>
     * @throws IOException
     *             in case something bad happens.
     */
    public static FileChannel getInputChannel(File source) throws IOException {
        inputNotNull(source);
        if (!source.exists() || !source.canRead() || !source.isDirectory())
            throw new IllegalStateException("Source is not in a legal state.");
        FileChannel channel = null;
        while (channel == null) {
            try {
                channel = new FileInputStream(source).getChannel();
            } catch (Exception e) {
                channel = null;
            }
        }
        return channel;
    }

    /**
     * Get an output <code>FileChannel</code> for the provided <code>File</code>
     * 
     * @param file
     *            <code>File</code> for which we need to get an output <code>FileChannel</code>
     * @return a <code>FileChannel</code>
     * @throws IOException
     *             in case something bad happens.
     */
    public static FileChannel getOuputChannel(File file) throws IOException {
        inputNotNull(file);
        return new RandomAccessFile(file, "rw").getChannel();

    }

    /**
     * Move the specified input file to the specified destination directory.
     * 
     * @param source
     *            the input <code>File</code> which need to be moved.
     * @param destDir
     *            the destination directory where to move the file.
     * @throws IOException
     */
    public static void moveFileTo(File source, File destDir, boolean removeInputFile)
            throws IOException {
        inputNotNull(source, destDir);
        if (!source.exists() || !source.canRead() || source.isDirectory())
            throw new IllegalStateException("Source is not in a legal state.");
        if (!destDir.exists() || !destDir.canWrite() || !destDir.isDirectory())
            throw new IllegalStateException("Source is not in a legal state.");
        if (destDir.getAbsolutePath().equalsIgnoreCase(source.getParentFile().getAbsolutePath()))
            return;
        // ///////////////////////////////////////////////////////////////
        //
        // Copy the inputFile in the specified destination directory
        //
        // ///////////////////////////////////////////////////////////////
        copyFile(source, new File(destDir, source.getName()));

        // ///////////////////////////////////////////////////////////////
        //
        // Delete the source file.
        //
        // ///////////////////////////////////////////////////////////////
        // we need to call the gc, see
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4715154
        if (removeInputFile)
            FILE_CLEANER.addFile(source);

    }

    /**
     * Tries to convert a {@link URL} into a {@link File}. Return null if something bad happens
     * 
     * @param fileURL
     *            {@link URL} to be converted into a {@link File}.
     * @return {@link File} for this {@link URL} or null.
     */
    public static File URLToFile(URL fileURL) {
        inputNotNull(fileURL);
        try {

            final File retFile = DataUtilities.urlToFile(fileURL);
            return retFile;

        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, t.getLocalizedMessage(), t);
        }
        return null;
    }

    /**
     * This method is responsible for checking if the input file is still being written or if its
     * available for being parsed.
     * 
     * <p>
     * Specifically this method tries to open up a "rw" channel on the provided input file. If the
     * file is still being written this operation fails with an exception, we therefore catch this
     * exception and sleep for {@value #ATOMIC_WAIT} seconds as defined in the constant
     * {@link #ATOMIC_WAIT}.
     * 
     * <p>
     * If after having waited for {@link #MAX_WAITING_TIME_FOR_LOCK} (which is read from the
     * configuration or set to the default value of {@link #DEFAULT_WAITING_TIME}) we have not yet
     * acquired the channel we skip this file but we signal this situation.
     * 
     * @param inputFile
     * @return <code>true</code> if the lock has been successfully acquired. <code>false</code>
     *         otherwise
     * @throws InterruptedException
     * @throws IOException
     */
    public static boolean acquireLock(Object caller, File inputFile) throws InterruptedException,
            IOException {
        return acquireLock(caller, inputFile, IOUtils.MAX_WAITING_TIME_FOR_LOCK);
    }

    /**
     * Copy {@link InputStream} to {@link OutputStream}.
     * 
     * @param sourceStream
     *            {@link InputStream} to copy from.
     * @param destinationStream
     *            {@link OutputStream} to copy to.
     * @param closeInput
     *            quietly close {@link InputStream}.
     * @param closeOutput
     *            quietly close {@link OutputStream}
     * @throws IOException
     *             in case something bad happens.
     */
    public static void copyStream(InputStream sourceStream, OutputStream destinationStream,
            boolean closeInput, boolean closeOutput) throws IOException {
        copyStream(sourceStream, destinationStream, DEFAULT_SIZE, closeInput, closeOutput);
    }

    /**
     * Copy {@link InputStream} to {@link OutputStream}.
     * 
     * @param sourceStream
     *            {@link InputStream} to copy from.
     * @param destinationStream
     *            {@link OutputStream} to copy to.
     * @param size
     *            size of the buffer to use internally.
     * @param closeInput
     *            quietly close {@link InputStream}.
     * @param closeOutput
     *            quietly close {@link OutputStream}
     * @throws IOException
     *             in case something bad happens.
     */
    public static void copyStream(InputStream sourceStream, OutputStream destinationStream,
            int size, boolean closeInput, boolean closeOutput) throws IOException {

        inputNotNull(sourceStream, destinationStream);
        byte[] buf = new byte[size];
        int n = -1;
        try {
            while (-1 != (n = sourceStream.read(buf))) {
                destinationStream.write(buf, 0, n);
                destinationStream.flush();
            }
        } finally {
            // closing streams and connections
            try {
                destinationStream.flush();
            } finally {
                try {
                    if (closeOutput)
                        destinationStream.close();
                } finally {
                    try {
                        if (closeInput)
                            sourceStream.close();
                    } finally {

                    }
                }
            }
        }
    }



    /**
     * Convert the input from the provided {@link Reader} into a {@link String}.
     * 
     * @param inputStream
     *            the {@link Reader} to copy from.
     * @return a {@link String} that contains the content of the provided {@link Reader}.
     * @throws IOException
     *             in case something bad happens.
     */
    public static String toString(StreamSource src) throws IOException {
        inputNotNull(src);
        InputStream inputStream = src.getInputStream();
        if (inputStream != null) {
            return toString(inputStream);
        } else {

            final Reader r = src.getReader();
            return toString(r);
        }
    }
    
    /**
     * Convert the input from the provided {@link Reader} into a {@link String}.
     * 
     * @param inputStream
     *            the {@link Reader} to copy from.
     * @return a {@link String} that contains the content of the provided {@link Reader}.
     * @throws IOException
     *             in case something bad happens.
     */
    public static String toString(final StreamSource src, final String ecoding) throws IOException {
        inputNotNull(src);
        InputStream inputStream = src.getInputStream();
        if (inputStream != null) {
            return toString(inputStream,ecoding);
        } else {

            final Reader r = src.getReader();
            return toString(r);
        }
    }

    /**
     * Inflate the provided {@link ZipFile} in the provided output directory.
     * 
     * @param archive
     *            the {@link ZipFile} to inflate.
     * @param outputDirectory
     *            the directory where to inflate the archive.
     * @throws IOException
     *             in case something bad happens.
     * @throws FileNotFoundException
     *             in case something bad happens.
     */
    public static void inflate(ZipFile archive, File outputDirectory, String fileName)
            throws IOException, FileNotFoundException {

        final Enumeration<? extends ZipEntry> entries = archive.entries();
        try {
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();

                if (!entry.isDirectory()) {
                    final String name = entry.getName();
                    final String ext = FilenameUtils.getExtension(name);
                    final InputStream in = new BufferedInputStream(archive.getInputStream(entry));
                    final File outFile = new File(outputDirectory,
                            fileName != null ? new StringBuilder(fileName).append(".").append(ext)
                                    .toString() : name);
                    final OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));

                    IOUtils.copyStream(in, out, true, true);

                }
            }
        } finally {
            try {
                archive.close();
            } catch (Throwable e) {
                if (LOGGER.isLoggable(Level.FINE))
                    LOGGER.isLoggable(Level.FINE);
            }
        }

    }

    /**
     * Singleton
     */
    private IOUtils() {

    }

    /**
     * Zip all the files in the input directory which starts with the provided prefix.
     * 
     * @param inputDir
     *            directory where to get files from.
     * @param zipFilePrefix
     *            prefix to choose files.
     * @return a {@link File} that points to the generated zip file, or null.
     */
    public static File deflate(final File inputDir, final String zipFilePrefix) {
        if (inputDir == null || !inputDir.exists() || !inputDir.isDirectory())
            return null;

        // get files to zip
        final File[] files = inputDir.listFiles((FilenameFilter) new PrefixFileFilter(zipFilePrefix));

        return deflate(inputDir, zipFilePrefix, files);
    }

	/**
	 * @param outputDir The directory where the zipfile will be created
	 * @param zipFileBaseName The basename of hte zip file (i.e.: a .zip will be appended)
	 * @param files The files that will be put into the zipfile
	 * @return The created zipfile, or null if an error occurred.
	 */
	public static File deflate(final File outputDir,
			final String zipFileBaseName, final File[] files) {
		// Create a buffer for reading the files
        byte[] buf = new byte[4096];

        final File outZipFile = new File(outputDir, zipFileBaseName + ".zip");
        try {
            // Create the ZIP file
            final ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    new FileOutputStream(outZipFile)));

            // Compress the files
            for (File file : files) {
                final FileInputStream in = new FileInputStream(file);

                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(FilenameUtils.getName(file.getAbsolutePath())));

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                // Complete the entry
                out.closeEntry();
                in.close();
            }

            // Complete the ZIP file
            out.close();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        }

        return outZipFile;
	}
    
	/**
	 * This function zip the input files. 
	 * 
	 * @param outputDir The temporary directory where the zip files.
	 * @param zipFileBaseName The name of the zip file.
	 * @param files The array files to zip.
	 * @return The zip file or null.
	 * @throws IOException
	 */
	public static File zip(final File outputDir,
			final String zipFileBaseName, final File[] files)throws IOException {
		
		if(outputDir != null && files != null && zipFileBaseName != null){
			
			// //////////////////////////////////////////
			// Create a buffer for reading the files
			// //////////////////////////////////////////
	        final File outZipFile = new File(outputDir, zipFileBaseName + ".zip");
	        ZipOutputStream out = null;
	        
	        try {
	        	
	        	// /////////////////////////////////
	            // Create the ZIP output stream
	        	// /////////////////////////////////
	        	
	            out = new ZipOutputStream(new BufferedOutputStream(
	                    new FileOutputStream(outZipFile)));
	            
	            // /////////////////////
	            // Compress the files
	            // /////////////////////
	            
	            for (File file : files){
	            	if(file.isDirectory()){
	            		zipDirectory(file, file, out);
	            	}else{
	            		zipFile(file, out);
	            	}      		
	            } 	            

	            out.close();
	            out = null;
	            
		        return outZipFile;
	            
	        }catch(IOException e) {
	            if (LOGGER.isLoggable(Level.SEVERE))
	                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
	            return null;
	        }finally{
	        	if(out != null)out.close();
	        }
	        
		}else throw new IOException("One or more input parameters are null!");
	}
	
	/**
	 * This function zip the input directory.
	 * 
	 * @param directory The directory to be zipped.
	 * @param base The base directory.
	 * @param out The zip output stream.
	 * @throws IOException
	 */
	 public static void zipDirectory(final File directory, final File base,
			 final ZipOutputStream out) throws IOException {
		 
		 if(directory != null && base != null && out != null){
		    File[] files = directory.listFiles();
		    byte[] buffer = new byte[4096];
		    int read = 0;
		    
		    FileInputStream in = null;
		    ZipEntry entry = null;
		    
	        try {
			    for (int i = 0, n = files.length; i < n; i++) {
			    	if (files[i].isDirectory()) {
			    		zipDirectory(files[i], base, out);
			    	}else{
			    		in = new FileInputStream(files[i]);
			    		entry = new ZipEntry(base.getName().concat("\\").concat(files[i].getPath().substring(
			    				base.getPath().length() + 1)));
			    		out.putNextEntry(entry);
			    		
			    		while (-1 != (read = in.read(buffer))) {
			    			out.write(buffer, 0, read);
			    		}
			    		
			    		// //////////////////////
		                // Complete the entry
			    		// //////////////////////
			    		
			    		out.closeEntry();
			    		
		                in.close();
		                in = null;
			    	}
			    }
			    
	        }catch (IOException e) {
	            if (LOGGER.isLoggable(Level.SEVERE))
	                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);	            
	            if(out != null)out.close();
	        }finally{
	        	if(in != null)in.close();
	        }
	        
		 }else throw new IOException("One or more input parameters are null!");
	  }
	 
	 /**
	  * This function zip the input file.
	  * 
	  * @param file The input file to be zipped.
	  * @param out The zip output stream.
	  * @throws IOException
	  */
	 public static void zipFile(final File file, 
			 final ZipOutputStream out) throws IOException {
		 
		 if(file != null && out != null){
			 
			// /////////////////////////////////////////// 
			// Create a buffer for reading the files
			// ///////////////////////////////////////////
			 
	        byte[] buf = new byte[4096];
	        
	        FileInputStream in = null;
	        
	        try {
                in = new FileInputStream(file);

                // //////////////////////////////////
                // Add ZIP entry to output stream.
                // //////////////////////////////////
                
                out.putNextEntry(new ZipEntry(FilenameUtils.getName(file.getAbsolutePath())));

                // //////////////////////////////////////////////
                // Transfer bytes from the file to the ZIP file
                // //////////////////////////////////////////////
                
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                // //////////////////////
                // Complete the entry
                // //////////////////////
                
                out.closeEntry();
                
                in.close();
                in = null;

	        }catch (IOException e) {
	            if (LOGGER.isLoggable(Level.SEVERE))
	                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
	            if(out != null)out.close();
	        }finally{
	        	if(in != null)in.close();
	        }
	        
		 }else throw new IOException("One or more input parameters are null!");
	  }

	/**
	 * Unzips the files from a zipfile into a directory.
	 * All of the files will be put in a single direcotry. If the zipfile contains
	 * a hierarchycal structure, it will be ignored.
	 *
	 * @param zipFile The zipfile to be examined
	 * @param destDir The direcotry where the extracted files will be stored.
	 * @return The list of the extracted files, or null if an error occurred.
	 * @throws IllegalArgumentException if the destination dir is not writeable.
	 */
	public static List<File> unzipFlat(final File zipFile, final File destDir) {
		if(!destDir.isDirectory())
			throw new IllegalArgumentException("Not a directory '"+destDir.getAbsolutePath()+"'");

		if(!destDir.canWrite())
			throw new IllegalArgumentException("Unwritable directory '"+destDir.getAbsolutePath()+"'");

		try
        {
			List<File> ret = new ArrayList<File>();
            ZipInputStream zipinputstream = new ZipInputStream(new FileInputStream(zipFile));

            for(ZipEntry zipentry = zipinputstream.getNextEntry(); zipentry != null; zipentry = zipinputstream.getNextEntry()) {
                String entryName = zipentry.getName();
				if(zipentry.isDirectory())
					continue;

				File outFile = new File(destDir, entryName);
				ret.add(outFile);
                FileOutputStream fileoutputstream = new FileOutputStream(outFile);

				org.apache.commons.io.IOUtils.copy(zipinputstream, fileoutputstream);
                fileoutputstream.close();
                zipinputstream.closeEntry();
            }

            zipinputstream.close();
			return ret;
        }
        catch (Exception e)
        {
            LOGGER.log(Level.WARNING, "Error unzipping file '"+zipFile.getAbsolutePath()+"'", e);
			return null;
        }
	}

    /**
     * Create a subDirectory having the actual date as name, within a specified destination
     * directory.
     * 
     * @param destDir
     *            the destination directory where to build the "today" directory.
     * @return the created directory.
     */
    public static File createTodayDirectory(File destDir) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
        final String newPath = (new StringBuffer(destDir.getAbsolutePath().trim())
                .append(File.separatorChar).append(sdf.format(new Date()))).toString();
        File dir = new File(newPath);
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.FileBaseCatalogHelper#createDirectory(java.lang.String,
     * java.io.File)
     */
    public static File createDirectory(String location, File directory) throws IOException {
        File file = findLocation(location, directory);

        if (file != null) {
            if (!file.isDirectory()) {
                String msg = location + " already exists and is not directory";
                throw new IOException(msg);
            }
        }

        file = new File(location);

        if (file.isAbsolute()) {
            file.mkdir();

            return file;
        }

        // no base directory set, cannot create a relative path
        if (directory == null) {
            // TODO: log or throw exception
            return null;
        }

        file = new File(directory, location);
        file.mkdir();

        if (file.exists() && file.isDirectory()) {
            return file;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.FileBaseCatalogHelper#createFile(java.lang.String, java.io.File)
     */
    public static File createFile(String location, File directory) throws IOException {
        File file = findLocation(location, directory);

        if (file != null) {
            return file;
        }

        file = new File(location);

        if (file.isAbsolute()) {
            file.createNewFile();

            return file;
        }

        // no base directory set, cannot create a relative path
        if (directory == null) {
            // TODO: log or throw exception
            return null;
        }

        file = new File(directory, location);
        file.createNewFile();

        if (file.exists() && !file.isDirectory()) {
            return file;
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.FileBaseCatalogHelper#findLocation(java.lang.String,
     * java.io.File)
     */
    public static File findLocation(String location, File directory) throws IOException {
    	
    	// trim spaces
    	location=location.trim();
    	
        // first to an existance check
        File file = new File(location);

        if (file.isAbsolute()) {
            return file;
        } else {
            // try a relative url
            file = new File(directory, location);

            if (file.exists()) {
                return file;
			}
        }

        return null;
    }

    /**
     * Get the contents of a {@link File} as a String using the specified character encoding.
     * @param file {@link File} to read from
     * @param encoding IANA encoding
     * @return a {@link String} containig the content of the {@link File} or <code>null<code> if an error happens.
     */
	public static String toString(final File file, final String encoding) {
		inputNotNull(file);
		if(!file.isFile()||!file.canRead()||!file.exists())
			return null;
		InputStream stream =null;
		try {
			if(encoding==null)
				return toString(new FileInputStream(file));
			else
				return toString(new FileInputStream(file),encoding);
		}catch (Throwable e) {
			if(LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING,e.getLocalizedMessage(),e);
			return null;
		}finally{
			if(stream!=null)
				try {
					stream.close();
				}catch (Throwable e) {
					if(LOGGER.isLoggable(Level.FINEST))
						LOGGER.log(Level.FINEST,e.getLocalizedMessage(),e);
				}				
		}
	}
	
    /**
     * Get the contents of a {@link File} as a String using the default character encoding.
     * @param file {@link File} to read from
     * @return a {@link String} containig the content of the {@link File} or <code>null<code> if an error happens.
     */
	public static String toString(final File file) {
		return toString(file, null);
	}

	/**
	 * This method is responsible for checking if the input file is still being written or if its
	 * available for being parsed.
	 * 
	 * <p>
	 * Specifically this method tries to open up a "rw" channel on the provided input file. If the
	 * file is still being written this operation fails with an exception, we therefore catch this
	 * exception and sleep for {@value #ATOMIC_WAIT} seconds as defined in the constant
	 * {@link #ATOMIC_WAIT}.
	 * 
	 * <p>
	 * If after having waited for {@link #MAX_WAITING_TIME_FOR_LOCK} (which is read from the
	 * configuration or set to the default value of {@link #DEFAULT_WAITING_TIME}) we have not yet
	 * acquired the channel we skip this file but we signal this situation.
	 * @param caller 
	 * @param inputFile
	 * @param maxwait
	 * @return <code>true</code> if the lock has been successfully acquired. <code>false</code>
	 *         otherwise
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static boolean acquireLock(Object caller, File inputFile, final long maxwait) throws InterruptedException,
	        IOException {
	    // //
	    //
	    // Acquire an exclusive lock to wait for long
	    // writing processes before trying to check on them
	    //
	    // //
	    double sumWait = 0;
	    while (true) {
	        FileChannel channel = null;
	        FileLock lock = null;
	        try {
	            // get a rw channel
	            channel = getOuputChannel(inputFile);
	
	            if (channel != null) {
	                // here we could block
	                lock = channel.tryLock();
	                if (lock != null)
	                    return true;
	            }
	        } catch (OverlappingFileLockException e) {
	            // File is already locked in this thread or virtual machine
	            LOGGER.info("File is already locked in this thread or virtual machine");
	        } catch (Exception e) {
	            LOGGER.log(Level.FINE, e.getLocalizedMessage(), e);
	        } finally {
	            // release the lock
	            if (lock != null)
	                lock.release();
	            if (channel != null)
	                channel.close();
	        }
	
	        // Sleep for ATOMIC_WAIT milliseconds prior to retry for acquiring
	        // the lock
	        synchronized (caller) {
	            caller.wait(ATOMIC_WAIT);
	        }
	
	        sumWait += ATOMIC_WAIT;
	        if (sumWait > maxwait) {
	            LOGGER.info("Waiting time beyond maximum specified waiting time, exiting...");
	            // Quitting the loop
	            break;
	        }
	    }
	
	    // A time greater than MAX_WAITING_TIME_FOR_LOCK has elapsed and no lock has
	    // been acquired. Thus, I need to return false
	    return false;
	}
}
