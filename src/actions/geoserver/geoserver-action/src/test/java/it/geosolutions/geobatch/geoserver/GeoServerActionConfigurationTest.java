package it.geosolutions.geobatch.geoserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GeoServerActionConfigurationTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void cloneTest(){
        GeoServerActionConfiguration config=new GeoServerActionConfiguration("id", "name", "desc");
        
        GeoServerActionConfiguration configCloned=config.clone();
        
        assertEquals(config,configCloned);
        
        assertTrue(config!=configCloned);
        
        assertTrue(config.getClass()==configCloned.getClass());
    }

}
