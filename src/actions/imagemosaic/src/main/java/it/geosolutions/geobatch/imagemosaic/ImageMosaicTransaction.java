package it.geosolutions.geobatch.imagemosaic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sun.org.apache.xerces.internal.xs.XSException;
import com.thoughtworks.xstream.XStream;
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
 * @note: on 28 Feb 2011 discussion:
 * carlo: do we need to clean this bean from Xstream specific methods and
 * annotations?
 * simone: leave it here since this class is actually used only by geobatch
 */

@XStreamInclude(ImageMosaicTransaction.class)
@XStreamAlias("ImageMosaic")
public class ImageMosaicTransaction implements Serializable {

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
        ImageMosaicTransaction cmd = new ImageMosaicTransaction(new File("BASEDIR"), addList, delList);
        System.out.println("-------------------------------------");

        String sob = stream.toXML(cmd);
        System.out.println(sob);

        System.out.println("-------------------------------------");

        System.out.println("-------------------------------------");
        System.out.println(stream.fromXML(sob));
        System.out.println("-------------------------------------");

        System.out.println("-------------------------------------");

        ImageMosaicTransaction cmd2 = deserialize(
                new File("src/main/resources/serialized.xml"));

        System.out.println(cmd2.toString());

        System.out.println("-------------------------------------");

    }

    /**
     * Try to deserialize the command, return null if some goes wrong
     * @param file the file to deserialize
     * @return the deserialized ImageMosaicTransaction object or null
     */
    public static ImageMosaicTransaction deserialize(File file) {
        try {
            final InputStream is = new FileInputStream(file);
            if (stream == null)
                init();
            final ImageMosaicTransaction cmd = (ImageMosaicTransaction) stream.fromXML(is);
            return cmd;
        } catch (XSException e) {
            // LOGGER.log(Level.FINER, e.getMessage(), e);
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // LOGGER.log(Level.FINER, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * initialize the XStream env
     */
    private static void init() {
        stream = new XStream();
        stream.processAnnotations(ImageMosaicTransaction.class);
    }

    public ImageMosaicTransaction(File baseDir, List<File> addFiles, List<File> delFiles) {
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
        // "ImageMosaicTransaction [baseDir=" + baseDir + ", addFiles=" + addFiles + ", delFiles="
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

        ImageMosaicTransaction cmd = new ImageMosaicTransaction(new File(this.getBaseDir().getAbsolutePath()), addList, delList);

        return cmd;
    }

}
