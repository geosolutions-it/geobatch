package it.geosolutions.filesystemmonitor.monitorfactory;

import it.geosolutions.filesystemmonitor.FSMSPIFinder;
import it.geosolutions.filesystemmonitor.OsType;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitor;
import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
/**
 * 
 * @author Simone Giannecchini, GeoSolutions
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({})
@ContextConfiguration(locations={"/applicationContext.xml", "/TestDummy-context.xml"})
public class TestDummy extends AbstractJUnit4SpringContextTests {
	
	@Test
	public void testDummy(){
		//get the registered services
		FSMSPIFinder finder= (FSMSPIFinder) applicationContext.getBean("fsmSPIFinder");
		Assert.assertTrue(finder!=null);
		FileSystemMonitor abstractMonitor= FSMSPIFinder.getMonitor(null,OsType.OS_UNDEFINED);
		Assert.assertTrue(abstractMonitor!=null);
		Assert.assertTrue(abstractMonitor instanceof DummyMonitor);
	}

}
