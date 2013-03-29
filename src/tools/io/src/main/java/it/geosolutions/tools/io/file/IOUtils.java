/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package it.geosolutions.tools.io.file;

import it.geosolutions.tools.commons.Conf;
import it.geosolutions.tools.commons.check.Objects;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Assorted IO related utilities
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 * @author (r2)Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @version 0.2
 */
public final class IOUtils extends org.apache.commons.io.IOUtils {

	private final static Logger LOGGER = LoggerFactory.getLogger(IOUtils.class
			.toString());

	/**
	 * do not intantiate
	 */
	private IOUtils() {
	};

	/**
	 * The max time the node will wait for, prior to stop to attempt for
	 * acquiring a lock on a <code>File</code>.
	 */
	public static final long MAX_WAITING_TIME_FOR_LOCK = 12 * 60 * 60 * 1000;// 12h

	public final static String FILE_SEPARATOR = System
			.getProperty("file.separator");

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
			throw new IllegalStateException(
					"Source and destination channels must be open.");

		final java.nio.ByteBuffer buffer = java.nio.ByteBuffer
				.allocateDirect(bufferSize);
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
	public static void copyFileChannel(int bufferSize, FileChannel source,
			FileChannel destination) throws IOException {

		Objects.notNull(source, destination);
		if (!source.isOpen() || !destination.isOpen())
			throw new IllegalStateException(
					"Source and destination channels must be open.");
		FileLock lock = null;
		try {

			lock = destination.lock();
			final long sourceSize = source.size();
			long pos = 0;
			while (pos < sourceSize) {
				// read and flip
				final long remaining = (sourceSize - pos);
				final int mappedZoneSize = remaining >= bufferSize ? bufferSize
						: (int) remaining;
				destination.transferFrom(source, pos, mappedZoneSize);
				// update zone
				pos += mappedZoneSize;

			}
		} finally {
			if (lock != null) {
				try {
					lock.release();
				} catch (Throwable t) {
					if (LOGGER.isInfoEnabled())
						LOGGER.info(t.getLocalizedMessage(), t);
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

	// /**
	// * Get an input <code>FileChannel</code> for the provided
	// <code>File</code>
	// *
	// * @param file
	// * <code>File</code> for which we need to get an input
	// <code>FileChannel</code>
	// * @return a <code>FileChannel</code>
	// * @throws IOException
	// * in case something bad happens.
	// */
	// public static FileChannel getInputChannel(File source) throws IOException
	// {
	// Objects.notNull(source);
	// if (!source.exists() || !source.canRead() || !source.isDirectory())
	// throw new IllegalStateException("Source is not in a legal state.");
	// FileChannel channel = null;
	// while (channel == null) {
	// try {
	// channel = new FileInputStream(source).getChannel();
	// } catch (Exception e) {
	// channel = null;
	// }
	// }
	// return channel;
	// }

	// /**
	// * Get an output <code>FileChannel</code> for the provided
	// <code>File</code>
	// *
	// * @param file
	// * <code>File</code> for which we need to get an output
	// <code>FileChannel</code>
	// * @return a <code>FileChannel</code>
	// * @throws IOException
	// * in case something bad happens.
	// */
	// public static FileChannel getOuputChannel(File file) throws IOException {
	// Objects.notNull(file);
	// return new RandomAccessFile(file, "r").getChannel();
	//
	// }

	/**
	 * This method is responsible for checking if the input file is still being
	 * written or if its available for being parsed.
	 * 
	 * <p>
	 * Specifically this method tries to open up a "rw" channel on the provided
	 * input file. If the file is still being written this operation fails with
	 * an exception, we therefore catch this exception and sleep for
	 * {@value #ATOMIC_WAIT} seconds as defined in the constant
	 * {@link #ATOMIC_WAIT}.
	 * 
	 * <p>
	 * If after having waited for {@link #MAX_WAITING_TIME_FOR_LOCK} (which is
	 * read from the configuration or set to the default value of
	 * {@link #DEFAULT_WAITING_TIME}) we have not yet acquired the channel we
	 * skip this file but we signal this situation.
	 * 
	 * @param inputFile
	 * @return <code>true</code> if the lock has been successfully acquired.
	 *         <code>false</code> otherwise
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static boolean acquireLock(Object caller, File inputFile)
			throws InterruptedException, IOException {
		return IOUtils.acquireLock(caller, inputFile,
				IOUtils.MAX_WAITING_TIME_FOR_LOCK);
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
	public static void copyStream(InputStream sourceStream,
			OutputStream destinationStream, boolean closeInput,
			boolean closeOutput) throws IOException {
		copyStream(sourceStream, destinationStream, Conf.DEFAULT_SIZE,
				closeInput, closeOutput);
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
	public static void copyStream(InputStream sourceStream,
			OutputStream destinationStream, int size, boolean closeInput,
			boolean closeOutput) throws IOException {

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
	 * Create a subDirectory having the actual date as name, within a specified
	 * destination directory.
	 * 
	 * @param destDir
	 *            the destination directory where to build the "today"
	 *            directory.
	 * @return the created directory.
	 */
	public static File createTodayDirectory(File destDir) {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd");
		final String newPath = (new StringBuffer(destDir.getAbsolutePath()
				.trim()).append(File.separatorChar).append(sdf
				.format(new Date()))).toString();
		File dir = new File(newPath);
		if (!dir.exists())
			dir.mkdir();
		return dir;
	}

	/**
	 * This method is responsible for checking if the input file is still being
	 * written or if its available for being parsed.
	 * 
	 * <p>
	 * Specifically this method tries to open up a "rw" channel on the provided
	 * input file. If the file is still being written this operation fails with
	 * an exception, we therefore catch this exception and sleep for
	 * {@value #ATOMIC_WAIT} seconds as defined in the constant
	 * {@link #ATOMIC_WAIT}.
	 * 
	 * <p>
	 * If after having waited for {@link MAX_WAITING_TIME_FOR_LOCK} (which is
	 * read from the configuration or set to the default value of
	 * {@link #DEFAULT_WAITING_TIME}) we have not yet acquired the channel we
	 * skip this file but we signal this situation.
	 * 
	 * NOTE: To make use of mandatory locks, mandatory locking must be enabled
	 * both on the file system that contains the file to be locked, and on the
	 * file itself. Mandatory locking is enabled on a file system using the
	 * "-o mand" option to mount(8), or the MS_MANDLOCK flag for mount(2).
	 * Mandatory locking is enabled on a file by disabling group execute
	 * permission on the file and enabling the set-group-ID permission bit (see
	 * chmod(1) and chmod(2)).
	 * 
	 * @param caller
	 * @param inputFile
	 * @param maxwait
	 * @return <code>true</code> if the lock has been successfully acquired.
	 *         <code>false</code> otherwise
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static boolean acquireLock(Object caller, File inputFile,
			final long maxwait) throws InterruptedException, IOException {

		if (!inputFile.exists())
			return false;// file not exists!

		if (inputFile.isDirectory()) {
			// return inputFile.setReadOnly();
			return true;// cannot lock directory
		}

		// //
		//
		// Acquire an exclusive lock to wait for long
		// writing processes before trying to check on them
		//
		// //
		double sumWait = 0;
		while (true) {
			FileOutputStream outStream = null;
			FileChannel channel = null;
			FileLock lock = null;
			try {
				outStream = new FileOutputStream(inputFile, true);

				// get a rw channel
				channel = outStream.getChannel();
				if (channel != null) {
					// here we could block
					lock = channel.tryLock();
					if (lock != null) {
						if (LOGGER.isTraceEnabled())
							LOGGER.trace("File locked successfully");
						return true;
					}
				}
			} catch (OverlappingFileLockException e) {
				// File is already locked in this thread or virtual machine
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("File is already locked in this thread or virtual machine");
			} catch (Exception e) {
				if (LOGGER.isDebugEnabled())
					LOGGER.debug(e.getLocalizedMessage(), e);
			} finally {

				org.apache.commons.io.IOUtils.closeQuietly(outStream);

				// release the lock
				if (lock != null)
					try {
						lock.release();
					} catch (Exception e) {
						// eat me
					}

				if (channel != null)
					try {
						channel.close();
					} catch (Exception e) {
						// eat me
					}
			}

			// Sleep for ATOMIC_WAIT milliseconds prior to retry for acquiring
			// the lock
			synchronized (caller) {
				caller.wait(Conf.ATOMIC_WAIT);
			}

			sumWait += Conf.ATOMIC_WAIT;
			if (sumWait > maxwait) {
				if (LOGGER.isWarnEnabled())
					LOGGER.warn("Waiting time beyond maximum specified waiting time, exiting...");
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
