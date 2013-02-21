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

import it.geosolutions.tools.commons.file.Path;
import it.geosolutions.tools.commons.listener.DefaultProgress;
import it.geosolutions.tools.commons.listener.Progress;
import it.geosolutions.tools.commons.listener.ProgressList;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy a Tree recursively and asynchronously
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class CopyTree extends DirectoryWalker<Future<File>> {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(CopyTree.class);

	private final CompletionService<File> cs;

	private final File sourceDir;
	private final File destDir;

	/**
	 * container for futures works this is internally handled to avoid
	 * concurrent modification during collecting operations.
	 */
	private final Collection<Future<File>> works = new ArrayList<Future<File>>();

	private final ProgressList<String> collectingProgressList = new ProgressList<String>();
	/**
	 * used to track cancel collecting op.
	 */
	private boolean collectingCanceled = false;
	/**
	 * used to track end of collecting op.
	 */
	private boolean collectingCompleted = false;

	private final ProgressList<String> copyProgressList = new ProgressList<String>();

	/**
	 * represents the sum of all the collected file size<br>
	 * It is used to calculate copyProgressList
	 */
	private volatile long copyTotalSize = 0L;
	/**
	 * represents the current copyProgress status in byte
	 * 
	 * @see {@link CopyTree#updateProgress(long)}
	 */
	private volatile long copyProgress = 0L;
	/**
	 * used to track cancel file copy op.
	 */
	private boolean copyCanceled = false;
	private boolean copyStarted = false;

	/**
	 * {@link CopyTree#CopyTree(FileFilter, CompletionService, int, File, File)}
	 */
	public CopyTree(FileFilter filter, final CompletionService<File> cs,
			File sourceDir, File destDir) {
		this(filter, cs, -1, sourceDir, destDir);
	}

	/**
	 * @param filter
	 *            the filter to apply, null means visit all files
	 * @param cs
	 *            the CompletionService to use
	 * @param depth
	 *            controls how deep the hierarchy is navigated to (less than 0
	 *            means unlimited)
	 * @param sourceDir
	 * @param destDir
	 */
	public CopyTree(FileFilter filter, final CompletionService<File> cs,
			int depth, File sourceDir, File destDir) {
		super(filter, depth);
		if (sourceDir == null || destDir == null || cs == null) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Invalid null argument");
			}
			throw new IllegalArgumentException("Invalid null argument");
		}
		this.sourceDir = sourceDir;
		this.destDir = destDir;
		this.cs = cs;
	}

	/**
	 * @return check if any op. is canceled
	 */
	public final synchronized boolean isCancelled() {
		return collectingCanceled || copyCanceled;
	}

	/**
	 * set the canceled status for all pending operations
	 */
	public synchronized final void setCancelled() {
		// send cancel event
		collectingProgressList.onCancel();
		collectingProgressList.onWarningOccurred("manually canceled", Thread
				.currentThread().getName(), "Operation is canceled");
		// send cancel event
		copyProgressList.onCancel();
		copyProgressList.onWarningOccurred("manually canceled", Thread
				.currentThread().getName(), "Operation is canceled");

		this.collectingCanceled = true;
		this.copyCanceled = true;

		// cancel all pending calls
		cancelCopyCalls();

	}

	/**
	 * used internally to cancel all pending calls
	 */
	private void cancelCopyCalls() {
		// cancel all pending calls
		final Iterator<Future<File>> it = this.works.iterator();
		while (it.hasNext()) {
			Future<File> future = it.next();
			// cancel the call
			future.cancel(true);
		}
	}

	/**
	 * 
	 * @param listener
	 *            the listener to add
	 * @return as specified {@link Collection#add(Object)}
	 */
	public boolean addCollectingListener(Progress<String> listener) {
		return collectingProgressList.addListener(listener);
	}

	/**
	 * 
	 * @param listener
	 *            the listener to add
	 * @return as specified {@link Collection#add(Object)}
	 */
	public boolean addCopyListener(Progress<String> listener) {
		return copyProgressList.addListener(listener);
	}

	/**
	 * Call reset to clean the object state
	 * 
	 * @param force
	 *            if in progress works should be canceled
	 * 
	 * @deprecated untested method
	 */
	public synchronized void reset(final boolean force) {
		// send cancel event
		if (!collectingCompleted) {
			collectingProgressList.onWarningOccurred("Manually reset called",
					Thread.currentThread().getName(), "Object reset");
		}
		if (copyStarted) {
			copyProgressList.onWarningOccurred("Manually reset called", Thread
					.currentThread().getName(), "Object reset");
		}
		if (force) {
			setCancelled();
		}

		works.clear();
		copyStarted = false;

		this.collectingCanceled = false;
		this.copyCanceled = false;

	}

	/**
	 * 
	 * Copy the entire tree recursively (depending from the passed filter) can
	 * be interrupted using setCancelled(true)
	 * 
	 * @return number of files scheduled for the copy
	 * @throws IOException
	 *             {@link DirectoryWalker#walk(File, Collection)}
	 * @throws IllegalStateException
	 *             if CopyTree object is not in a legal state: call
	 *             {@link #reset()}
	 */
	public synchronized int copy() throws IOException, IllegalStateException {
		if (copyStarted) {
			throw new IllegalStateException(
					"This object is not in a clean state. Please call reset() or use a new CopyTree object.");
		}
		try {
			super.walk(sourceDir, this.works);

			return this.works.size();

		} catch (CancelException ioe) {
			/*
			 * this happens when the setCancel() is called during the collecting
			 * operation
			 */

			// store exception
			collectingProgressList.onExceptionOccurred(ioe);
			copyProgressList.onExceptionOccurred(ioe);

			// send cancel event
			// done in setCancel() -> collectingProgressList.cancel();

			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(ioe.getLocalizedMessage());
			}

		} catch (IOException ioe) {
			// store exception
			collectingProgressList.onExceptionOccurred(ioe);
			copyProgressList.onExceptionOccurred(ioe);

			// send cancel event
			setCancelled();

			// if an I/O Error occurs
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(ioe.getLocalizedMessage(), ioe);
			}
		}
		return 0;
	}

	/**
	 * Synchronized: This is called by all the copy threads modifying members
	 * 
	 * @see {@link #copyTotalSize}
	 * @see {@link #copyProgress}
	 */
	private synchronized void updateProgress(long fileSize) {
		this.copyProgress += fileSize;
		/**
		 * when collecting operation is complete [copyTotalSize means 100%]
		 */
		if (collectingCompleted) {
			// x : 100 = (fileSize+copyProgress) : copyTotalSize
			copyProgressList.onUpdateProgress(copyProgress * 100
					/ copyTotalSize);
		} else {
			/*
			 * fake percent progress still calculating total file size
			 */
			copyProgressList.onWarningOccurred("updating process", Thread
					.currentThread().getName(),
					"fake % progress still calculating total file size");
			copyProgressList.onUpdateProgress(copyProgress * 100
					/ copyTotalSize);
		}
	}

	/**
	 * 
	 * @param cs
	 *            CompletionService
	 * @param source
	 *            file to copy
	 * @param sourceDir
	 *            where source is mounted
	 * @param destinationDir
	 *            mount point where to copy source
	 * @return
	 * @throws RejectedExecutionException
	 *             - if this task cannot be accepted for execution.
	 * @throws IllegalArgumentException
	 *             - if executor is null or terminated.
	 */
	public static Future<File> asyncCopyTree(final CompletionService<File> cs,
			final File source, final File sourceDir, final File destinationDir,
			final Progress<String> listener) throws RejectedExecutionException,
			IllegalArgumentException {

		final Callable<File> call = new Callable<File>() {
			public File call() throws Exception {
				try {
					// build the new path
					listener.onNewTask("rebase file path");
					listener.onStart();
					File destFile = Path.rebaseFile(sourceDir, destinationDir,
							source);
					listener.onUpdateProgress(10);

					// try to build the directory tree
					listener.onNewTask("building directory structure");
					listener.onStart();
					if (!destFile.getParentFile().mkdirs()) {
						listener.onWarningOccurred(
								this.getClass().getSimpleName(),
								Thread.currentThread().getName(),
								"Unable to create the destination directory structure: probably it already exists");
					}
					listener.onUpdateProgress(30);

					// start copy
					listener.onNewTask("copying " + source + " to " + destFile);
					listener.onStart();
					FileUtils.copyFile(source, destFile);

					listener.onUpdateProgress(100);
					listener.onCompleted();
					// return the rebased and copied file
					return destFile;
				} catch (Exception e) {
					listener.onExceptionOccurred(e);
					listener.onCancel();
					throw e;
				}
			}
		};
		try {
			return cs.submit(call);
		} catch (NullPointerException e) {
			listener.onExceptionOccurred(e);
			listener.onCancel();
			throw e;
		} catch (RejectedExecutionException e) {
			listener.onExceptionOccurred(e);
			listener.onCancel();
			throw e;
		}
	}

	// ////////////////////////////////////////////////////////////////
	// DirectoryWalker overrides
	// ////////////////////////////////////////////////////////////////

	/**
	 * {@link #DirectoryWalker#handleIsCancelled(File, int, Collection)}
	 */
	@Override
	protected boolean handleIsCancelled(File file, int depth,
			Collection<Future<File>> results) throws IOException {
		return collectingCanceled;
	}

	/**
	 * {@link #DirectoryWalker#handleCancelled(File, Collection,
	 * org.apache.commons.io.DirectoryWalker.CancelException)}
	 */
	@Override
	protected void handleCancelled(File startDirectory,
			Collection<Future<File>> results, CancelException cancel)
			throws IOException {
		// THIS IS DANGEROUS: commented out
		// remove partially copied files
		// FileUtils.deleteQuietly(destDir);

		// re-throw exception
		throw cancel;
	}

	/**
	 * {@link #DirectoryWalker#handleDirectory(File, int, Collection)}
	 */
	@Override
	protected boolean handleDirectory(File directory, int depth,
			Collection<Future<File>> results) throws IOException {
		return true; // process ALL directories
	}

	/**
	 * {@link #DirectoryWalker#filterDirectoryContents(File, int, File[])}
	 */
	@Override
	protected File[] filterDirectoryContents(File directory, int depth,
			File[] files) throws IOException {

		// if directory is empty
		if (files.length == 0) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Found empty source dir: "
						+ directory.getAbsolutePath());
			}
			final File destDirFile = Path.rebaseFile(sourceDir, this.destDir,
					directory);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Creating empty destination dir: "
						+ destDirFile.getAbsolutePath());
			}
			if (!destDirFile.mkdirs()) {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error("Unable to create destination dir: "
							+ destDirFile.getAbsolutePath());
				}
			}
		}
		return files;
	}

	/**
	 * This method is called by the DirectoryWalker.walk() one. Here we:<br>
	 * <ul>
	 * <li>continue collecting files to copy</li>
	 * <li>calculate file size to correctly track copy progress</li>
	 * <li>start copy task</li>
	 * </ul>
	 * 
	 * {@link #DirectoryWalker#handleFile(File, int, Collection)}
	 */
	@Override
	protected void handleFile(File file, int depth,
			Collection<Future<File>> results) throws IOException,
			SecurityException {
		if (!collectingCanceled) {
			if (!copyStarted) {
				copyProgressList.onStart();
				copyProgressList.onNewTask("starting copy tree");
				copyProgressList.onUpdateProgress(0);
			}
			// calculate file size
			final long fileSize = file.length();

			// update total file size to copy
			this.copyTotalSize += fileSize;

			//
			Progress<String> listener = new DefaultProgress("COPY["
					+ file.getName() + "]") {
				/**
				 * override default setCompleted call to update progress on file
				 * copy completion
				 */
				@Override
				public void onCompleted() {
					super.onCompleted();
					updateProgress(fileSize);
				}
			};

			// start copy
			results.add(asyncCopyTree(cs, file, sourceDir, destDir, listener));
		}
	}

	/**
	 * collecting operation is started
	 * {@link DirectoryWalker#handleEnd(Collection)}
	 */
	@Override
	protected void handleStart(File startDirectory,
			Collection<Future<File>> results) throws IOException {
		collectingProgressList.onStart();
		collectingProgressList
				.onNewTask("Starting collecting files to copy from: "
						+ startDirectory.getAbsolutePath());
		collectingProgressList.onUpdateProgress(0);
	}

	/**
	 * collecting operation is concluded
	 * {@link DirectoryWalker#handleEnd(Collection)}
	 */
	@Override
	protected void handleEnd(Collection<Future<File>> results)
			throws IOException {
		collectingProgressList.onCompleted();
		collectingProgressList.onUpdateProgress(100);
		collectingCompleted = true;
	}

}
