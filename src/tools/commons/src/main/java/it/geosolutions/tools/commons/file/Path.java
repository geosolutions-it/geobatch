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
package it.geosolutions.tools.commons.file;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 */
public class Path {
    private final static Logger LOGGER = LoggerFactory.getLogger(Path.class);

    /**
     * Return the absolute dir represented by "dir", rooted at "parentDir" if
     * needed.
     * 
     * @return the absolute dir represented by "dir", rooted at "parentDir" if
     *         needed, or null if no existing dir could be found.
     */
    private File resolveDir(File dir, File parentDir) {
        if (dir == null)
            return parentDir;

        if (dir.isAbsolute()) {
            return dir.exists() ? dir : null;
        } else {
            File absDir = new File(parentDir, dir.getPath());
            return absDir.exists() ? absDir : null;
        }
    }

    /**
     * {@link #findLocation(String, String)}
     */
    public static File findLocation(String location, File directory) {
        if (directory == null) {
            throw new IllegalArgumentException("Argements can not be null");
        }
        final String path = findLocation(location, directory.getAbsolutePath());
        if (path == null)
            return null;
        return new File(path);
    }

    /**
     * {@link #findLocation(String, String)}
     */
    public static File findLocation(File location, File directory) {
        if (location == null || directory == null) {
            throw new IllegalArgumentException("Arguments can not be null");
        }
        final String path = findLocation(location.getPath(), directory.getPath());
        if (path == null)
            return null;
        return new File(path);
    }

    /**
     * @note return null if is impossible to resolve the path
     * @param location
     * @param directory
     * @return the absolute path
     * @throws IllegalArgumentException if arguments are null
     */
    public static String findLocation(String location, String directory) throws IllegalArgumentException {
        if (location == null || directory == null) {
            throw new IllegalArgumentException("Arguments can not be null");
        }

        // trim spaces
        location = location.trim();

        // first to an existance check
        File file = new File(location);

        if (file.isAbsolute()) {
            return file.getAbsolutePath();
        } else {
            // try a relative url
            file = new File(directory, location);

            if (file.exists()) {
                return file.getAbsolutePath();
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * it.geosolutions.geobatch.FileBaseCatalogHelper#createFile(java.lang.String
     * , java.io.File)
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
     * @see
     * it.geosolutions.geobatch.FileBaseCatalogHelper#createDirectory(java.lang
     * .String, java.io.File)
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

    /**
     * Rebase the file name in input which must be into the srcMountPoint to
     * destMountPoint:<br/>
     * <br/>
     * /src/mount/point/subDir/file.txt<br/>
     * |-srcMountPoint-|---fileName---|<br/>
     * |------------file--------------|<br/>
     * <br/>
     * /dest/mount/point/subDir/file.txt<br/>
     * |-destMountPoint-|---fileName---|<br/>
     * |------------return-------------|
     * 
     * @param sourceMountPoint
     * @param destMountPoint
     * @param file
     * @throws IOException
     */
    public static File rebaseFile(final File srcMountPoint, final File destMountPoint, final File file)
        throws IOException, IllegalArgumentException {

        final StringBuffer buf = new StringBuffer(file.getCanonicalPath());
        String srcMountPointFilePath = srcMountPoint.getCanonicalPath();

        // file is equals to the mount point -> rebasing file:
        // srcMountPointFileName+"./"
        if (buf.toString().equals(srcMountPointFilePath)) {
            // returning the destination mount point
            return destMountPoint;
        }

        return new File(destMountPoint, toRelativeFile(srcMountPoint, file).getPath());
    }
    
    /**
     * Relativize the file name in input which must be into the srcMountPoint:<br/>
     * <br/>
     * /src/mount/point/subDir/file.txt<br/>
     * |-srcMountPoint-|---fileName---|<br/>
     * |------------file--------------|<br/>
     * <br/>
     * subDir/file.txt<br/>
     * |---fileName---|<br/>
     * |---return-----|
     * 
     * @param sourceMountPoint
     * @param file
     * @throws IOException
     * @throws IllegalArgumentException if arguments are null or not absolute
     */
    public static File toRelativeFile(final File srcMountPoint, final File file)
        throws IOException, IllegalArgumentException {

    	
    	if (file==null || srcMountPoint==null){
    		throw new IllegalArgumentException("Some passed file argument are null: mountPoint:"+srcMountPoint+" file: "+file);
    	} else if (!file.isAbsolute() || !srcMountPoint.isAbsolute()){
    		throw new IllegalArgumentException("Some passed file arguments are not absolute: mountPoint:"+srcMountPoint+" file: "+file);
    	}
    	
        final StringBuffer buf = new StringBuffer(file.getCanonicalPath());
        String srcMountPointFilePath = srcMountPoint.getCanonicalPath();

        // force to end with File.separator to be shure it is search for a
        // directory
        srcMountPointFilePath += File.separator;
        final int index = buf.indexOf(srcMountPointFilePath);
        if (index != 0) {
            throw new IllegalArgumentException("The passed file: \'" + file
                                               + "\' is not a child of the source mount point: \'"
                                               + srcMountPoint + "\'");
        } else {
            buf.delete(0, srcMountPointFilePath.length());
        }
        return new File(buf.toString());
    }

    /**
     * Create a subDirectory having the actual date as name, within a specified
     * destination directory.
     * 
     * @param destDir the destination directory where to build the "today"
     *            directory.
     * @param inputFileName
     * @return the created directory.
     */
    public final static File createTodayDirectory(File destDir, String inputFileName, final boolean withTime) {
        final SimpleDateFormat SDF = withTime
            ? new SimpleDateFormat("yyyy_MM_dd_hhmmsss") : new SimpleDateFormat("yyyy_MM_dd");
        final String newPath = (new StringBuffer(destDir.getAbsolutePath().trim()).append(File.separatorChar)
            .append(SDF.format(new Date())).append("_").append(inputFileName)).toString();
        File dir = new File(newPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }
        return dir;
    }

    /**
     * Create a subDirectory having the actual date as name, within a specified
     * destination directory.
     * 
     * @param destDir the destination directory where to build the "today"
     *            directory.
     * @param inputFileName
     * @return the created directory.
     */
    public final static File createTodayDirectory(File destDir, String inputFileName) {
        return createTodayDirectory(destDir, inputFileName, false);
    }

}
