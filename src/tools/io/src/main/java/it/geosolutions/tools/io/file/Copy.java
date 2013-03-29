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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Copy {
	private final static Logger LOGGER = LoggerFactory.getLogger(Copy.class);

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
	public static void copyFile(File sourceFile, File destinationFile)
			throws IOException {
		copyFile(sourceFile, destinationFile, Conf.DEFAULT_SIZE);
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
	public static void copyFile(File sourceFile, File destinationFile, int size)
			throws IOException {
		Objects.notNull(sourceFile, destinationFile);
		if (!sourceFile.exists() || !sourceFile.canRead()
				|| !sourceFile.isFile())
			throw new IllegalStateException("Source is not in a legal state.");
		if (!destinationFile.exists()) {
			destinationFile.createNewFile();
		}
		if (destinationFile.getAbsolutePath().equalsIgnoreCase(
				sourceFile.getAbsolutePath()))
			throw new IllegalArgumentException("Cannot copy a file on itself");
	
		RandomAccessFile s=null,d=null;
		FileChannel source = null;
		FileChannel destination = null;
		try {
		        s= new RandomAccessFile(sourceFile, "r");
	                source =s.getChannel();
	                d=new RandomAccessFile(destinationFile, "rw");
	                destination = d.getChannel();		    
			IOUtils.copyFileChannel(size, source, destination);
		} finally {
                    if (source != null) {
                        try {
                            source.close();
                        } catch (Throwable t) {
                            if (LOGGER.isInfoEnabled())
                                LOGGER.info(t.getLocalizedMessage(), t);
                        }
                    }
                    if (s != null) {
                        try {
                            s.close();
                        } catch (Throwable t) {
                            if (LOGGER.isInfoEnabled())
                                LOGGER.info(t.getLocalizedMessage(), t);
                        }
                    }                    

                    if (destination != null) {
                        try {
                            destination.close();
                        } catch (Throwable t) {
                            if (LOGGER.isInfoEnabled())
                                LOGGER.info(t.getLocalizedMessage(), t);
                        }
                    }
                    
                    if (d != null) {
                        try {
                            d.close();
                        } catch (Throwable t) {
                            if (LOGGER.isInfoEnabled())
                                LOGGER.info(t.getLocalizedMessage(), t);
                        }
                    }                    
		}
	}

	/**
	 * Copy a file (preserving data) to a destination (which can be on nfs)
	 * waiting (at least) 'seconds' seconds for its propagation.
	 * 
	 * @param source
	 * @param dest
	 * @param overwrite
	 *            if false and destination exists() do not overwrite the file
	 * @param seconds
	 *            to wait (maximum) for nfs propagate. If -1 no check is
	 *            performed.
	 * @return the copied file if success, null if not.
	 */
	public static File copyFileToNFS(File source, File dest, boolean overwrite,
			final int seconds) {
		if (source != null && dest != null) {
			if (dest.exists()) {
				// source == destination
				if (source.equals(dest)) {
					// YES
					// (dest.exists, !overwrite, source==dest) -> return source
					if (LOGGER.isErrorEnabled())
						LOGGER.error("Unable to copy file to: \'"
								+ dest.getAbsolutePath()
								+ "\' source and destination are the same! (overwrite is set to \'"
								+ overwrite + "\'). Returning source.");
					return source;
				}
				// overwrite?
				if (!overwrite) {
					// NO
					// source != destination
					if (!dest.exists()) {
						// NO
						// (dest.exists, !overwrite, source!=dest) -> fail
						if (LOGGER.isErrorEnabled())
							LOGGER.error("Failed to copy file to: \'"
									+ dest.getAbsolutePath()
									+ "\' destination exists! (overwrite is set to \'"
									+ overwrite + "\').");
						return null;
					}
				}
			}
			return copyFileToNFS(source, dest, seconds);
	
		} else {
			// NullPointerException - if source or destination is null
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Source or destination is null.");
			return null;
		}
	}

	/**
	 * Copy a file (preserving data) to a destination (which can be on nfs)
	 * waiting (at least) 'seconds' seconds for its propagation.
	 * 
	 * @param source
	 * @param dest
	 * @param seconds
	 *            to wait (maximum) for nfs propagate. If -1 no check is
	 *            performed.
	 * @return the copied file if success, null if not.
	 */
	public static File copyFileToNFS(final File source, final File dest,
			final int seconds) {
		try {
			/**
			 * Carlo commented out on 22 Aug 2011<br>
			 * this function is not thread safe:<br>
			 * an IOExcheption is thrown which hide a more correct:<br>
			 * OverlappingFileLockException - If a lock that overlaps the
			 * requested region is already held by this Java virtual machine, or
			 * if another thread is already blocked in this method and is
			 * attempting to lock an overlapping region of the same file
			 */
			// if (!dest.exists()) {
			// // copy the file
			// // FileUtils.copyFile(source, dest);
			// } else
			// return null;
	
			try {
				copyFile(source, dest);
			} catch (OverlappingFileLockException o) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error("Problem writing the file: \'"
							+ source
							+ "\' to \'"
							+ dest
							+ "\'.\nA lock that overlaps the requested region is already held by this Java virtual machine, or if another thread is already blocked in this method and is attempting to lock an overlapping region of the same file.");
				return null;
			} catch (Throwable t) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error("Problem writing the file: \'" + source
							+ "\' to \'" + dest + "\'.", t);
				return null;
			}
	
			if (seconds > 0) {
				if (!FileUtils.waitFor(dest, seconds)) {
					if (LOGGER.isErrorEnabled())
						LOGGER.error("Failed to propagate file to: "
								+ dest.getAbsolutePath());
					return null;
				} else if (LOGGER.isInfoEnabled()) {
					LOGGER.info("File: \'" + source.getAbsoluteFile()
							+ "\' succesfully copied and propagated to: "
							+ dest.getAbsolutePath());
				}
			} else if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Source file: \'" + source.getAbsoluteFile()
						+ "\' succesfully copied to: " + dest.getAbsolutePath());
			}
		} catch (NullPointerException e) {
			// NullPointerException - if source or destination is null
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Source or destination is null."
						+ "\n\tThe message is: " + e.getLocalizedMessage());
			return null;
		}
		return dest;
	}

	/**
	 * 
	 * @param list
	 * @param baseDestDir
	 * @param serialOverwrite
	 *            if set true, overwrite is permitted (but parallel file transfer is
	 *            disabled)
	 * @param seconds
	 * @return
	 */
	public static List<File> copyListFileToNFS(final List<File> list,
			final File baseDestDir, final boolean serialOverwrite,
			final int seconds) {
		// list
		if (list == null) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Failed to copy files.");
			return null;
		}
		final int size = list.size();
		if (size == 0) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Failed to copy file list using an empty list");
			return null;
		}
	
		if (serialOverwrite) {
			final List<File> ret = new ArrayList<File>(size);
			for (final File f : list) {
				final File retFile = copyFileToNFS(f,
						new File(baseDestDir, f.getName()), serialOverwrite,
						seconds);
				if (retFile != null) {
					ret.add(retFile);
				}
			}
			return ret;
		} else
			return parallelCopyListFileToNFS(null, list, baseDestDir, seconds);
	
	}

	/**
	 * Copy a list of files (preserving data) to a destination (which can be on
	 * nfs) waiting (at least) 'seconds' seconds for each file propagation.
	 * 
	 * @param es
	 *            The ExecutorService or null if you want to use a
	 *            CachedThreadPool.
	 * @note potentially this is a bad executor (for log lists of big files)
	 *       NOTE: we should make some tests on this 22 Aug 2011
	 * @param list
	 * @param baseDestDir
	 * @param overwrite
	 *            if false and destination exists() do not overwrite the file
	 * @param seconds
	 * @return the resulting moved file list or null
	 * 
	 */
	public static List<File> parallelCopyListFileToNFS(ExecutorService es,
			final List<File> list, final File baseDestDir, final int seconds) {
	
		try {
	
			/*
			 * this could be potentially a bad executor (for log lists of big
			 * files) NOTE: we should make some tests on this 22 Aug 2011
			 */
			if (es == null) {
				final ThreadFactory threadFactory = Executors
						.defaultThreadFactory();
				es = Executors.newCachedThreadPool(threadFactory);
			}
	
			final List<FutureTask<File>> futureFileList = asynchCopyListFileToNFS(
					es, list, baseDestDir, seconds);
	
			// list
			if (futureFileList == null) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error("Failed to copy files.");
				return null;
			}
			final int size = futureFileList.size();
			if (size == 0) {
				if (LOGGER.isErrorEnabled())
					LOGGER.error("Failed to copy file list using an empty list");
				return null;
			}
	
			final List<File> ret = new ArrayList<File>(size);
			for (Future<File> futureFile : futureFileList) {
	
				if (futureFile != null) {
	
					File file;
					try {
						file = futureFile.get();
						if (file != null && file.exists()) {
							ret.add(file);
						} else {
							if (LOGGER.isWarnEnabled())
								LOGGER.warn("SKIPPING file:\n\t"
										+ file
										+ ".\nUnable to copy a not existent file.");
						}
					} catch (InterruptedException e) {
						if (LOGGER.isErrorEnabled())
							LOGGER.error(
									"Unable to get the file from this future File copy. ",
									e);
					} catch (ExecutionException e) {
						if (LOGGER.isErrorEnabled())
							LOGGER.error(
									"Unable to get the file from this future File copy. ",
									e);
					}
				}
			}
	
			return ret;
		} catch (Throwable t) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Unrecognized error occurred. ", t);
		} finally {
			if (es != null)
				es.shutdownNow();
		}
		return null;
	
	}

	/**
	 * 
	 * @param ex
	 * @param source
	 * @param destination
	 * @param seconds
	 * @return
	 * @throws RejectedExecutionException
	 *             - if this task cannot be accepted for execution.
	 * @throws IllegalArgumentException
	 *             - if executor is null or terminated.
	 */
	public static FutureTask<File> asynchFileCopyToNFS(
			final ExecutorService ex, final File source,
			final File destination, final int seconds)
			throws RejectedExecutionException, IllegalArgumentException {
		if (ex == null || ex.isTerminated()) {
			throw new IllegalArgumentException(
					"Unable to run asynchronously using a terminated or null ThreadPoolExecutor");
		}
	
		final Callable<File> call = new Callable<File>() {
			public File call() throws Exception {
				return Copy.copyFileToNFS(source, destination, seconds);
			}
		};
		//
		final FutureTask<File> futureFile = new FutureTask<File>(call);
		ex.execute(futureFile);
		return futureFile;
		// return ex.submit(call);
	}

	/**
	 * Copy a list of files asynchronously to a destination (which can be on
	 * nfs) each thread wait (at least) 'seconds' seconds for each file
	 * propagation.
	 * 
	 * @param ex
	 *            the thread pool executor
	 * @param baseDestDir
	 * @param overwrite
	 *            if false and destination exists() do not overwrite the file
	 * @param seconds
	 * @return the resulting moved file list or null
	 * @note this function could not use overwrite boolean flag to avoid file
	 *       lock on the same section when 2 thread are called to write the same
	 *       file name
	 */
	public static List<FutureTask<File>> asynchCopyListFileToNFS(
			final ExecutorService ex, final List<File> list,
			final File baseDestDir, final int seconds) {
		// list
		if (list == null) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Failed to copy file list using a NULL list");
			return null;
		}
		final int size = list.size();
		if (size == 0) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Failed to copy file list using an empty list");
			return null;
		}
		// baseDestDir
		if (baseDestDir == null) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Failed to copy file list using a NULL baseDestDir");
			return null;
		} else if (!baseDestDir.isDirectory() || !baseDestDir.canWrite()) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Failed to copy file list using a not "
						+ "writeable directory as baseDestDir: "
						+ baseDestDir.getAbsolutePath());
			return null;
		}
		// Asynch executor check
		if (ex == null || ex.isTerminated()) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error("Unable to run asynchronously using a terminated or null ThreadPoolExecutor");
			return null;
		}
	
		final List<FutureTask<File>> asyncRes = new ArrayList<FutureTask<File>>(
				size);
		for (File file : list) {
			if (file != null) {
				if (file.exists()) {
					try {
						asyncRes.add(asynchFileCopyToNFS(ex, file, new File(
								baseDestDir, file.getName()), seconds));
					} catch (RejectedExecutionException e) {
						if (LOGGER.isWarnEnabled())
							LOGGER.warn("SKIPPING file:\n"
									+ file.getAbsolutePath() + ".\nError: "
									+ e.getLocalizedMessage());
					} catch (IllegalArgumentException e) {
						if (LOGGER.isWarnEnabled())
							LOGGER.warn("SKIPPING file:\n"
									+ file.getAbsolutePath() + ".\nError: "
									+ e.getLocalizedMessage());
					}
	
				} else {
					if (LOGGER.isWarnEnabled())
						LOGGER.warn("SKIPPING file:\n" + file.getAbsolutePath()
								+ "\nUnable to copy a not existent file.");
				}
			}
		}
	
		return asyncRes;
	}
}
