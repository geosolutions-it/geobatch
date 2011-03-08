package it.geosolutions.geobatch.imagemosaic;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.org.apache.xerces.internal.xs.XSException;

/**
 * Test the ImageMosaicCommand
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class ImageMosaicCommandTest {
    ImageMosaicCommand cmd=null;

    @Before
    public void setUp() throws Exception {
        // create in memory object
        List<File> addList = new ArrayList<File>();
        addList.add(new File("test1.txt"));
        List<File> delList = new ArrayList<File>();
        delList.add(new File("test2.txt"));
        cmd=new ImageMosaicCommand(new File("src/test/resources/data"), addList, delList);
    }

    @After
    public void tearDown() throws Exception {
        cmd=null;
    }

    @Test
    public final void testSerialize() {
        try {
            final String path="src/test/resources/test_cmd_out.xml";
            File out=ImageMosaicCommand.serialize(cmd, path);
            if (!out.exists())
                fail("Unable to serialize object to: "+path);
        } catch (XSException e) {
            fail(e.getMessage());
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        } catch (SecurityException e) {
            fail(e.getMessage());
        }
        
    }

    @Test
    public final void testDeserialize() {
        try {
            final String path="src/test/resources/test_cmd_out.xml";
            final File outFile=new File(path);
            ImageMosaicCommand cmd2=ImageMosaicCommand.deserialize(outFile);
            if (cmd2==null)
                fail("Unable to deserialize object from: "+path);
        } catch (XSException e) {
            fail(e.getMessage());
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        } catch (SecurityException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public final void testToString() {
        if (cmd!=null)
            cmd.toString();
    }

    @Test
    public final void testClone() {
        try {
            ImageMosaicCommand cmd2=(ImageMosaicCommand) cmd.clone();
            if (cmd2==null)
                fail("Unable to clone object");    
        } catch (CloneNotSupportedException e) {
            fail(e.getMessage());
        }
    }

}
