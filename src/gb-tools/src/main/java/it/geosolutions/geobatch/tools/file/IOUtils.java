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
import it.geosolutions.geobatch.tools.check.Objects;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FilenameUtils;

/**
 * Assorted IO related utilities
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 * 
 */
public class IOUtils extends org.apache.commons.io.IOUtils {

    private final static Logger LOGGER = Logger.getLogger(IOUtils.class.toString());

    /** Background to perform file deletions. */
    private final static FileRemover FILE_CLEANER = new FileRemover();

    public final static String FILE_SEPARATOR = System.getProperty("file.separator");

    /**
     * The max time the node will wait for, prior to stop to attempt for acquiring a lock on a
     * <code>File</code>.
     */
    public static final long MAX_WAITING_TIME_FOR_LOCK = 12 * 60 * 60 * 1000;// 12h

    static {
        FILE_CLEANER.setMaxAttempts(100);
        FILE_CLEANER.setPeriod(30);
        FILE_CLEANER.setPriority(1);
        FILE_CLEANER.start();
    }
    
    public static FileRemover getFileRemover(){
        return FILE_CLEANER;
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

        Objects.notNull(source, destination);
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

        Objects.notNull(source, destination);
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
        Objects.notNull(channel);
        if (channel.isOpen())
            channel.close();
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
        Objects.notNull(source);
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
        Objects.notNull(file);
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
        Objects.notNull(source, destDir);
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
        Path.copyFile(source, new File(destDir, source.getName()));

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
        copyStream(sourceStream, destinationStream, Conf.DEFAULT_SIZE, closeInput, closeOutput);
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

        Objects.notNull(sourceStream, destinationStream);
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
        Objects.notNull(src);
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
        Objects.notNull(src);
        InputStream inputStream = src.getInputStream();
        if (inputStream != null) {
            return toString(inputStream, ecoding);
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



    /**
     * Get the contents of a {@link File} as a String using the specified character encoding.
     * 
     * @param file
     *            {@link File} to read from
     * @param encoding
     *            IANA encoding
     * @return a {@link String} containig the content of the {@link File} or
     *         <code>null<code> if an error happens.
     */
    public static String toString(final File file, final String encoding) {
        Objects.notNull(file);
        if (!file.isFile() || !file.canRead() || !file.exists())
            return null;
        InputStream stream = null;
        try {
            if (encoding == null)
                return toString(new FileInputStream(file));
            else
                return toString(new FileInputStream(file), encoding);
        } catch (Throwable e) {
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            return null;
        } finally {
            if (stream != null)
                try {
                    stream.close();
                } catch (Throwable e) {
                    if (LOGGER.isLoggable(Level.FINEST))
                        LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
                }
        }
    }

    /**
     * Get the contents of a {@link File} as a String using the default character encoding.
     * 
     * @param file
     *            {@link File} to read from
     * @return a {@link String} containig the content of the {@link File} or
     *         <code>null<code> if an error happens.
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
     * 
     * @param caller
     * @param inputFile
     * @param maxwait
     * @return <code>true</code> if the lock has been successfully acquired. <code>false</code>
     *         otherwise
     * @throws InterruptedException
     * @throws IOException
     */
    public static boolean acquireLock(Object caller, File inputFile, final long maxwait)
            throws InterruptedException, IOException {
        
        if (!inputFile.exists())
            return false;// file not exists!
        
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
                caller.wait(Conf.ATOMIC_WAIT);
            }

            sumWait += Conf.ATOMIC_WAIT;
            if (sumWait > maxwait) {
                LOGGER.info("Waiting time beyond maximum specified waiting time, exiting...");
                // Quitting the loop
                break;
            }
        }

        // A time greater than MAX_WAITING_TIME_FOR_LOCK has elapsed and no lock
        // has
        // been acquired. Thus, I need to return false
        return false;
    }
}
