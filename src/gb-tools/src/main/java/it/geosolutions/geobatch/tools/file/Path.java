package it.geosolutions.geobatch.tools.file;

import it.geosolutions.geobatch.tools.Conf;
import it.geosolutions.geobatch.tools.check.Objects;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Path {
    private final static Logger LOGGER = Logger.getLogger(Path.class.toString());

    /**
     * 
     * PLACED INTO FileBasedCatalogConfiguration
     * 
     * Obtaining the Absolute path of the working dir
     * @param working_dir the relative (or absolute) path to absolutize
     * @note it should be a sub-dir of ...
* @TODO open a ticket to get getBaseDirectory() into Catalog interface
     
    public static String absolutize(String working_dir) /*throws FileNotFoundException { 
        FileBaseCatalog c=(FileBaseCatalog) CatalogHolder.getCatalog();
        File fo=null;
        try {
            fo=findLocation(working_dir,new File(c.getBaseDirectory()));
        }catch (IOException ioe){
            return null;
        }
        
        if (fo!=null){
            return fo.toString();
        }
        else {
//TODO LOG            throw new FileNotFoundException("Unable to locate the working dir");
//            throw new FileNotFoundException();
            return null;
        }
    }
*/
    /**
     * @note can return null
     * @param location
     * @param directory 
     * @return
     * @throws IOException
     */
    public static File findLocation(String location, File directory) throws IOException {
        if (location!=null){
            // trim spaces
            location = location.trim();
        }
        else
            return null;
        
        // first to an existance check
        File file = new File(location);

        if (file.isAbsolute()) {
            return file;
        } else {
            // try a relative url
            if (directory!=null)
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
        Objects.notNull(file);
        if (!file.exists() || !file.canRead() || !file.isFile())
            throw new IllegalStateException("Source is not in a legal state.");

        if (file.delete())
            return;

        IOUtils.getFileRemover().addFile(file);

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

}
