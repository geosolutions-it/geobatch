package it.geosolutions.geobatch.imagemosaic;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.sun.org.apache.xerces.internal.xs.XSException;
import com.thoughtworks.xstream.InitializationException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.io.path.Path;

/**
 * 
 * A serializable bean used to set the ImageMosaic command list.
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @note: on 28 Feb 2011 discussion: carlo: do we need to clean this bean from Xstream specific
 *        methods and annotations? simone: leave it here since this class is actually used only by
 *        geobatch
 */

@XStreamInclude(ImageMosaicCommand.class)
@XStreamAlias("ImageMosaic")
class ImageMosaicCommand implements Serializable {

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
     * @throws InitializationException - in case of an initialization problem
     */
    private static void init() throws InitializationException{
        stream = new XStream();
        stream.processAnnotations(ImageMosaicCommand.class);
    }


    /**
     * Test to see the serialized bean
     * 
     * @param args
     */
    public static void main(String args[]) {
        if (stream == null)
            init();

        List<File> addList = new ArrayList<File>();
        addList.add(new File("file1"));
        List<File> delList = new ArrayList<File>();
        delList.add(new File("file3"));
        ImageMosaicCommand cmd = new ImageMosaicCommand(new File("BASEDIR"), addList, delList);
        System.out.println("-------------------------------------");

        String sob = stream.toXML(cmd);
        System.out.println(sob);

        System.out.println("-------------------------------------");

        System.out.println("-------------------------------------");
        System.out.println(stream.fromXML(sob));
        System.out.println("-------------------------------------");

        System.out.println("-------------------------------------");
        ImageMosaicCommand cmd2 = null;
        try {
            cmd2 = deserialize(new File("src/main/resources/serialized.xml"));
        }
        catch (XSException e) {
            // LOGGER.log(Level.FINER, e.getMessage(), e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // LOGGER.log(Level.FINER, e.getMessage(), e);
            e.printStackTrace();
        }
        if (cmd2!=null)
            System.out.println(cmd2.toString());

        System.out.println("-------------------------------------");

    }
    
    /**
     * Copy a list of files to a destination (which can be on nfs) waiting
     * (at least) 'seconds' seconds for each file propagation.
     * @param list
     * @param baseDestDir
     * @param seconds
     * @return the resulting moved file list or null
     */
    public static List<File> copyTo(List<File> list, File baseDestDir, int seconds){
        if (list==null){
            return null;
        }
        final int size=list.size();
        if (size==0){
            return null;
        }
        
        List<File> ret=new ArrayList<File>(size);
        for (File file:list){
            if (file!=null){
                if (file.exists()){
                    ret.add(
                            it.geosolutions.geobatch.tools.file.Path.copyFileOnNFS(
                                    file,
                                    new File(baseDestDir,file.getName()),
                                    seconds));
                }
            }
        }
        return ret;
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
    public static File serialize(ImageMosaicCommand cmd, String path)
        throws XSException, FileNotFoundException, SecurityException {
//        try {
            final File outFile = new File(path);
            final FileOutputStream fos = new FileOutputStream(outFile);
            if (stream == null)
                init();
            stream.toXML(cmd, fos);
            return outFile;
//        } catch (XSException e) {
//            // LOGGER.log(Level.FINER, e.getMessage(), e);
//            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            // LOGGER.log(Level.FINER, e.getMessage(), e);
//            e.printStackTrace();
//        }
//        return null;
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
    public static ImageMosaicCommand deserialize(File file)
        throws XSException, FileNotFoundException, SecurityException {
//        try {
            final InputStream is = new FileInputStream(file);
            if (stream == null)
                init();
            final ImageMosaicCommand cmd = (ImageMosaicCommand) stream.fromXML(is);
            return cmd;
//        } catch (XSException e) {
//            // LOGGER.log(Level.FINER, e.getMessage(), e);
//            e.printStackTrace();
//        } catch (FileNotFoundException e) {
//            // LOGGER.log(Level.FINER, e.getMessage(), e);
//            e.printStackTrace();
//        }
//        return null;
    }

    public ImageMosaicCommand(File baseDir, List<File> addFiles, List<File> delFiles) {
        super();
        this.baseDir = baseDir;
        this.addFiles = addFiles;
        this.delFiles = delFiles;
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
