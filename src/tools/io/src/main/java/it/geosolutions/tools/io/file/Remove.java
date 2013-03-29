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

import it.geosolutions.tools.commons.check.Objects;

import java.io.File;
import java.io.FilenameFilter;

public abstract class Remove {

	/**
	 * Delete all the files/dirs with matching the specified
	 * {@link FilenameFilter} in the specified directory. The method can work
	 * recursively.
	 * 
	 * @param sourceDirectory
	 *            the directory to delete files from.
	 * @param filter
	 *            the {@link FilenameFilter} to use for selecting files to
	 *            delete.
	 * @param recursive
	 *            boolean that specifies if we want to delete files recursively
	 *            or not.
	 * @return
	 */
	public static boolean deleteDirectory(File sourceDirectory,
			FilenameFilter filter, boolean recursive, boolean deleteItself) {
		Objects.notNull(sourceDirectory, filter);
		if (!sourceDirectory.exists() || !sourceDirectory.canRead()
				|| !sourceDirectory.isDirectory())
			throw new IllegalStateException("Source is not in a legal state.");
	
		final File[] files = (filter != null ? sourceDirectory
				.listFiles(filter) : sourceDirectory.listFiles());
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
	 * Delete asynchronously the specified File.
	 */
	public static Object deleteFile(File file) {
		Objects.notNull(file);
		if (!file.exists() || !file.canRead() || !file.isFile())
			throw new IllegalStateException("Source is not in a legal state.");
	
		Object obj = new Object();
		FileGarbageCollector.getFileCleaningTracker().track(file, obj);
		return obj;
	}

	/**
	 * Empty the specified directory. The method can work recursively.
	 * 
	 * @param sourceDirectory
	 *            the directory to delete files/dirs from.
	 * @param recursive
	 *            boolean that specifies if we want to delete files/dirs
	 *            recursively or not.
	 * @param deleteItself
	 *            boolean used if we want to delete the sourceDirectory itself
	 * @return
	 */
	public static boolean emptyDirectory(File sourceDirectory,
			boolean recursive, boolean deleteItself) {
		Objects.notNull(sourceDirectory);
		if (!sourceDirectory.exists() || !sourceDirectory.canRead()
				|| !sourceDirectory.isDirectory()) {
			throw new IllegalStateException("Source is not in a legal state.");
		}
	
		final File[] files = sourceDirectory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				if (recursive) {
					if (!emptyDirectory(file, recursive, true)) {// delete
						// subdirs
						// recursively
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

}
