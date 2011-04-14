package it.geosolutions.geobatch.metocs.utils.io.rest;

import it.geosolutions.geobatch.tools.file.IOUtils;
import it.geosolutions.geobatch.tools.file.Path;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Fabiani
 * @author Simone Giannecchini, GeoSolutions SAS
 */
public class StorageCleaner extends TimerTask {
    private final static Logger LOGGER = LoggerFactory.getLogger(StorageCleaner.class);

    private long expirationDelay;

    private PublishingRestletGlobalConfig config;

    public PublishingRestletGlobalConfig getConfig() {
        return config;
    }

    public void setConfig(PublishingRestletGlobalConfig config) {
        this.config = config;
    }

    @Override
    public void run() {
        try {

            //
            // getting base directory
            //
            final File workingDir = new File(config.getRootDirectory());
            // final File workingDir =
            // Path.findLocation(config.getRootDirectory(),new
            // File(((FileBaseCatalog)
            // CatalogHolder.getCatalog()).getBaseDirectory()));
            // if (workingDir == null ||
            // !workingDir.exists()||!workingDir.canRead()||!workingDir.isDirectory())
            // {
            // if (LOGGER.isErrorEnabled())
            // LOGGER.error("Unable to work with the provided working directory:"+(workingDir!=null?workingDir:""));
            // return;
            // }

            // ok, now scan for existing files there and clean up those
            // that are too old
            long now = System.currentTimeMillis();
            for (File f : workingDir.listFiles()) {
                if (now - f.lastModified() > (expirationDelay * 1000)) {
                    // lock the file
                    RandomAccessFile raf = null;
                    FileChannel channel = null;
                    FileLock lock = null;
                    try {
                        raf = new RandomAccessFile(f, "rw");
                        channel = raf.getChannel();
                        lock = channel.lock();
                        Path.deleteFile(f);
                    } catch (Throwable e) {
                    } finally {
                        try {
                            if (raf != null)
                                raf.close();
                        } catch (Throwable e) {
                            if (LOGGER.isTraceEnabled())
                                LOGGER.trace(e.getLocalizedMessage(), e);
                        }
                        try {
                            if (lock != null)
                                lock.release();
                        } catch (Throwable e) {
                            if (LOGGER.isTraceEnabled())
                                LOGGER.trace(e.getLocalizedMessage(), e);
                        }

                        try {
                            if (channel != null)
                                IOUtils.closeQuietly(channel);
                        } catch (Throwable e) {
                            if (LOGGER.isTraceEnabled())
                                LOGGER.trace(e.getLocalizedMessage(), e);
                        }

                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn(
                    "Error occurred while trying to clean up old coverages from temp storage", e);
        }
    }

    /**
     * The file expiration delay in seconds, a file will be deleted when it's been around more than
     * expirationDelay
     * 
     * @return
     */
    public long getExpirationDelay() {
        return expirationDelay;
    }

    public void setExpirationDelay(long expirationDelay) {
        this.expirationDelay = expirationDelay;
    }

}
