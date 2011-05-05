package it.geosolutions.geobatch.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import it.geosolutions.geobatch.tools.file.Collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Test;

public class CollectorTests {
    final static int FILES_IN_TEST=6;
    final static String path="src/test/resources/test-data/collector/";
    
    @Test
    public final void testCollect() {
        Collector c=new Collector(
                FileFilterUtils.or(
                        new WildcardFileFilter("*_PCK.xml",IOCase.INSENSITIVE),
                        new WildcardFileFilter("*_PRO",IOCase.INSENSITIVE)));

        File location=new File(path);
        
        System.out.println("Location: "+location.getAbsoluteFile());
        
        assertNotNull(location);
        
        assertTrue(location.exists());
        
        List<File> list=c.collect(location);

        assertNotNull(list);
        
        System.out.println("Number of files..."+list.size());
        
        for (File f : list){
            System.out.println("FILE: "+f.getAbsolutePath());
        }

        assertEquals("Wrong number of files...", FILES_IN_TEST, list.size());
            
    }

}
