package it.geosolutions.geobatch.geoserver.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;

public class GeoServerActionReloadTest {

	
	@Test
	public void testSerializer(){

//		final File serialize=new File("test.xml");
		XStream stream =new XStream();
		List<Map<String, String>> list=new ArrayList<Map<String,String>>();
		Map<String,String> map1=new HashMap<String, String>();
		map1.put(it.geosolutions.geobatch.geoserver.reload.GeoServerReload.PASS, "pass1");
		map1.put(it.geosolutions.geobatch.geoserver.reload.GeoServerReload.USER, "user1");
		map1.put(it.geosolutions.geobatch.geoserver.reload.GeoServerReload.URL, "url1");
		list.add(map1);
		
		Map<String,String> map2=new HashMap<String, String>();
		map2.put(it.geosolutions.geobatch.geoserver.reload.GeoServerReload.PASS, "pass1");
		map2.put(it.geosolutions.geobatch.geoserver.reload.GeoServerReload.USER, "user1");
		map2.put(it.geosolutions.geobatch.geoserver.reload.GeoServerReload.URL, "url1");
		list.add(map2);
		System.out.println(stream.toXML(list));
	}

}
