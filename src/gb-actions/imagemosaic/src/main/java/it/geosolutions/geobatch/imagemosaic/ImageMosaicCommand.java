package it.geosolutions.geobatch.imagemosaic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.xerces.internal.xs.XSException;
import com.thoughtworks.xstream.InitializationException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * 
 * A serializable bean used to set the ImageMosaic command list.
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @note: on 28 Feb 2011 discussion: carlo: do we need to clean this bean from Xstream specific
 *        methods and annotations? simone: leave it here since this class is actually used only by
 *        geobatch
 * @note: This is public to make it usable from other packages
 * @see metoc actions
 */

@XStreamInclude(ImageMosaicCommand.class)
@XStreamAlias("ImageMosaic")
public class ImageMosaicCommand implements Serializable {

    /**
     * Serial version id
     */
    @XStreamOmitField
    private static final long serialVersionUID = 7592430220578935089L;

    @XStreamAlias("base")
    private File baseDir;

    @XStreamImplicit(itemFieldName = "del")
    private List<File> delFiles;

    @XStreamImplicit(itemFieldName = "add")
    private List<File> addFiles;

    @XStreamOmitField
    private static XStream stream;
    static {
        init();
    }

    /**
     * initialize the XStream env
     * 
     * @throws InitializationException
     *             - in case of an initialization problem
     */
    private static void init() throws InitializationException {
        stream = new XStream();
        stream.processAnnotations(ImageMosaicCommand.class);
    }

    /**
     * Try to deserialize the command, return null if some goes wrong
     * 
     * @param file
     *            the file to deserialize
     * @return the deserialized ImageMosaicCommand object or null
     * @throws FileNotFoundException
     *             - if the file exists but is a directory rather than a regular file, does not
     *             exist but cannot be created, or cannot be opened for any other reason
     * @throws SecurityException
     *             - if a security manager exists and its checkWrite method denies write access to
     *             the file
     * @throws XStreamException
     *             - if the object cannot be serialized
     */
    public static File serialize(ImageMosaicCommand cmd, String path) throws XSException,
            FileNotFoundException, SecurityException {
        final File outFile = new File(path);
        final FileOutputStream fos = new FileOutputStream(outFile);
        if (stream == null)
            init();
        stream.toXML(cmd, fos);
        return outFile;
    }

    /**
     * Try to deserialize the command, return null if some goes wrong
     * 
     * @param file
     *            the file to deserialize
     * @return the deserialized ImageMosaicCommand object or null
     * @throws FileNotFoundException
     *             - if the file exists but is a directory rather than a regular file, does not
     *             exist but cannot be created, or cannot be opened for any other reason
     * @throws SecurityException
     *             - if a security manager exists and its checkWrite method denies write access to
     *             the file
     * @throws XStreamException
     *             - if the object cannot be serialized
     */
    public static ImageMosaicCommand deserialize(File file) throws XSException,
            FileNotFoundException, SecurityException {
        // try {
        final InputStream is = new FileInputStream(file);
        if (stream == null)
            init();
        final ImageMosaicCommand cmd = (ImageMosaicCommand) stream.fromXML(is);
        return cmd;
        // } catch (XSException e) {
        // // LOGGER.trace(e.getMessage(), e);
        // e.printStackTrace();
        // } catch (FileNotFoundException e) {
        // // LOGGER.trace(e.getMessage(), e);
        // e.printStackTrace();
        // }
        // return null;
    }

    public ImageMosaicCommand(final File baseDir, final List<File> addFiles,
            final List<File> delFiles) {
        super();
        this.baseDir = baseDir;
        this.addFiles = addFiles;
        this.delFiles = delFiles;
    }

    public ImageMosaicCommand(final String baseDir, final List<String> addFiles,
            final List<String> delFiles) {
        super();
        this.baseDir = new File(baseDir);
        if (addFiles != null) {
            this.addFiles = new ArrayList<File>();
            for (String fileName : addFiles) {
                this.addFiles.add(new File(fileName));
            }
        }
        if (delFiles != null) {
            this.delFiles = new ArrayList<File>();
            for (String fileName : delFiles) {
                this.delFiles.add(new File(fileName));
            }
        }
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public List<File> getAddFiles() {
        return addFiles;
    }

    public void setAddFiles(List<File> addFiles) {
        this.addFiles = addFiles;
    }

    public List<File> getDelFiles() {
        return delFiles;
    }

    public void setDelFiles(List<File> delFiles) {
        this.delFiles = delFiles;
    }

    @Override
    public String toString() {
        if (stream == null)
            init();

        return stream.toXML(this);
        // "ImageMosaicCommand [baseDir=" + baseDir + ", addFiles=" + addFiles + ", delFiles="
        // + delFiles + "]";
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        List<File> addList = null;
        List<File> getAddList = this.getAddFiles();
        if (getAddList != null) {
            addList = new ArrayList<File>();
            for (File add : getAddList) {
                addList.add(new File(add.getAbsolutePath()));
            }
        }

        List<File> delList = null;
        List<File> getDelList = this.getDelFiles();
        if (getDelList != null) {
            delList = new ArrayList<File>();
            for (File del : getDelList) {
                delList.add(new File(del.getAbsolutePath()));
            }
        }

        ImageMosaicCommand cmd = new ImageMosaicCommand(new File(this.getBaseDir()
                .getAbsolutePath()), addList, delList);

        return cmd;
    }

}
