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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Path {
    private final static Logger LOGGER = LoggerFactory.getLogger(Path.class);

    /**
     * @note can return null
     * @param location
     * @param directory
     * @return the absolute path
     */
    public static File findLocation(String location, File directory) {
        if (location != null) {
            // trim spaces
            location = location.trim();
        } else
            return null;

        // first to an existance check
        File file = new File(location);

        if (file.isAbsolute()) {
            return file;
        } else {
            // try a relative url
            if (directory != null)
                file = new File(directory, location);

            if (file.exists()) {
                return file;
            }
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geobatch.FileBaseCatalogHelper#createFile(java.lang.String ,
     * java.io.File)
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
     * @see it.geosolutions.geobatch.FileBaseCatalogHelper#createDirectory(java.lang .String,
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
    public static boolean emptyDirectory(File sourceDirectory, boolean recursive,
            boolean deleteItself) {
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
     * Copy the input file onto the output file using a default buffer size.
     * 
     * @param sourceFile
     *            the {@link File} to copy from.
     * @param destinationFile
     *            the {@link File} to copy to.
     * @throws IOException
     *             in case something bad happens.
     * @deprecated use the org.apache.commons.io.FileUtils.copyFile(sourceDataFile, destDataFile);
     */
    public static void copyFile(File sourceFile, File destinationFile) throws IOException {
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
     * @deprecated use the org.apache.commons.io.FileUtils.copyFile(sourceDataFile, destDataFile);
     */
    public static void copyFile(File sourceFile, File destinationFile, int size) throws IOException {
        Objects.notNull(sourceFile, destinationFile);
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
            IOUtils.copyFileChannel(size, source, destination);
        } finally {
            try {
                if (source != null) {
                    try {
                        source.close();
                    } catch (Throwable t) {
                        if (LOGGER.isInfoEnabled())
                            LOGGER.info(t.getLocalizedMessage(), t);
                    }
                }
            } finally {
                if (destination != null) {
                    try {
                        destination.close();
                    } catch (Throwable t) {
                        if (LOGGER.isInfoEnabled())
                            LOGGER.info(t.getLocalizedMessage(), t);
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
        Objects.notNull(sourceDirectory, filter);
        if (!sourceDirectory.exists() || !sourceDirectory.canRead()
                || !sourceDirectory.isDirectory())
            throw new IllegalStateException("Source is not in a legal state.");

        final File[] files = (filter != null ? sourceDirectory.listFiles(filter) : sourceDirectory
                .listFiles());
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
     * Copy a file (preserving data) to a destination (which can be on nfs) waiting (at least)
     * 'seconds' seconds for its propagation.
     * 
     * @param source
     * @param dest
     * @param overwrite
     *            if false and destination exists() do not overwrite the file
     * @param seconds
     *            to wait (maximum) for nfs propagate. If -1 no check is performed.
     * @return the copied file if success, null if not.
     */
    public static File copyFileToNFS(File source, File dest, boolean overwrite, final int seconds) {
        if (source != null && dest != null) {
            if (dest.exists()) {
                // source == destination
                if (source.equals(dest)) {
                    // YES
                    // (dest.exists, !overwrite, source==dest) -> return source
                    if (LOGGER.isInfoEnabled())
                        LOGGER.info("Path:copyFileToNFS(): Unable to copy file to: \'"
                                + dest.getAbsolutePath()
                                + "\' source and destination are the same! (overwrite is set to \'"
                                + overwrite + "\'). Returning source.");
                    return source;
                }
                // overwrite?
                if (!overwrite) {
                    // NO
                    // source != destination
                    if (!source.equals(dest)) {
                        // NO
                        // (dest.exists, !overwrite, source!=dest) -> fail
                        if (LOGGER.isWarnEnabled())
                            LOGGER.warn("Path:copyFileToNFS(): failed to copy file to: \'"
                                    + dest.getAbsolutePath()
                                    + "\' destination exists! (overwrite is set to \'" + overwrite
                                    + "\').");
                        return null;
                    }
                }
            }
            return copyFileToNFS(source, dest, seconds);

        } else {
            // NullPointerException - if source or destination is null
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Path:copyFileToNFS() : source or destination is null.");
            return null;
        }
    }

    /**
     * Copy a file (preserving data) to a destination (which can be on nfs) waiting (at least)
     * 'seconds' seconds for its propagation.
     * 
     * @param source
     * @param dest
     * @param seconds
     *            to wait (maximum) for nfs propagate. If -1 no check is performed.
     * @return the copied file if success, null if not.
     */
    public static File copyFileToNFS(File source, File dest, final int seconds) {
        try {
            // copy the file
            FileUtils.copyFile(source, dest);
            if (seconds > 0) {
                if (!FileUtils.waitFor(dest, seconds)) {
                    if (LOGGER.isErrorEnabled())
                        LOGGER.error("Path:copyFileToNFS() : failed to propagate file to: "
                                + dest.getAbsolutePath());
                    dest = null;
                } else if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Path:copyFileToNFS() : file: \'" + source.getAbsoluteFile()
                            + "\' succesfully copied and propagated to: " + dest.getAbsolutePath());
                }
            } else if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Path:copyFileToNFS() : source file: \'" + source.getAbsoluteFile()
                        + "\' succesfully copied to: " + dest.getAbsolutePath());
            }
        } catch (NullPointerException e) {
            // NullPointerException - if source or destination is null
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Path:copyFileToNFS() : source or destination is null."
                        + "\n\tThe message is: " + e.getLocalizedMessage());
            dest = null;
        } catch (IOException e) {
            /*
             * IOException - if source or destination is invalid IOException - if an IO error occurs
             * during copying
             */
            if (LOGGER.isErrorEnabled())
                LOGGER.error(
                        "Path:copyFileToNFS() : \n\tThe message is: " + e.getLocalizedMessage(), e);
            dest = null;
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Path:copyFileToNFS() : failed to copy file." + "\n\tThe message is: "
                        + e.getLocalizedMessage());
            dest = null;
        }
        return dest;
    }

    /**
     * Copy a list of files (preserving data) to a destination (which can be on nfs) waiting (at
     * least) 'seconds' seconds for each file propagation.
     * 
     * @param list
     * @param baseDestDir
     * @param overwrite
     *            if false and destination exists() do not overwrite the file
     * @param seconds
     * @return the resulting moved file list or null
     */
    public static List<File> copyListFileToNFS(List<File> list, File baseDestDir,
            boolean overwrite, int seconds) {
        // list
        if (list == null) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Path:copyListFileToNFS() : failed to copy file list using a NULL list");
            return null;
        }
        final int size = list.size();
        if (size == 0) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Path:copyListFileToNFS() : failed to copy file list using an empty list");
            return null;
        }
        // baseDestDir
        if (baseDestDir == null) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Path:copyListFileToNFS() : failed to copy file list using a NULL baseDestDir");
            return null;
        } else if (!baseDestDir.isDirectory() || !baseDestDir.canWrite()) {
            if (LOGGER.isErrorEnabled())
                LOGGER.error("Path:copyListFileToNFS() : failed to copy file list using a not "
                        + "writeable directory as baseDestDir: " + baseDestDir.getAbsolutePath());
            return null;
        }

        List<File> ret = new ArrayList<File>(size);
        for (File file : list) {
            if (file != null) {
                if (file.exists()) {
                    File dest = copyFileToNFS(file, new File(baseDestDir, file.getName()),
                            overwrite, seconds);
                    if (dest != null) {
                        ret.add(dest);
                    }
                } else {
                    if (LOGGER.isWarnEnabled())
                        LOGGER.warn("Path:copyListFileToNFS() : SKIPPING file:\n"
                                + file.getAbsolutePath() + "\nUnable to copy a not existent file.");
                }
            }
        }
        return ret;
    }

    /**
     * Create a subDirectory having the actual date as name, within a specified destination
     * directory.
     * 
     * @param destDir
     *            the destination directory where to build the "today" directory.
     * @param inputFileName
     * @return the created directory.
     */
    public final static File createTodayDirectory(File destDir, String inputFileName,
            final boolean withTime) {
        final SimpleDateFormat SDF = withTime ? new SimpleDateFormat("yyyy_MM_dd_hhmmsss")
                : new SimpleDateFormat("yyyy_MM_dd");
        final String newPath = (new StringBuffer(destDir.getAbsolutePath().trim())
                .append(File.separatorChar).append(SDF.format(new Date())).append("_")
                .append(inputFileName)).toString();
        File dir = new File(newPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }
        return dir;
    }

    /**
     * Create a subDirectory having the actual date as name, within a specified destination
     * directory.
     * 
     * @param destDir
     *            the destination directory where to build the "today" directory.
     * @param inputFileName
     * @return the created directory.
     */
    public final static File createTodayDirectory(File destDir, String inputFileName) {
        return createTodayDirectory(destDir, inputFileName, false);
    }
}
