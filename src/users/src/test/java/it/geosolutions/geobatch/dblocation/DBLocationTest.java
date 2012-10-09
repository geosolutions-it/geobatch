package it.geosolutions.geobatch.dblocation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import it.geosolutions.geobatch.catalog.file.DataDirHandler;
import it.geosolutions.geobatch.users.UsersDBPropertyOverrideConfigurer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.geotools.test.TestData;
import org.junit.Test;

/**
 * 
 * @author DamianoG
 *
 */
public class DBLocationTest {

	/**
	 * Set a GEOBATCH_CONFIG_DIR mock location and test if the returned jdbc URLs correctly points 
	 * to settings/database dir 
	 * @throws Exception
	 */
	@Test
	public void testNoGBDatabaseFile() throws Exception {
		
		File gbConfigDir = TestData.file(this, "config");
		System.setProperty("GEOBATCH_CONFIG_DIR", gbConfigDir.getAbsolutePath());
		DataDirHandler dataDirHandler = new DataDirHandler();
		dataDirHandler.init();
		UserConfigurerTester configurer = new UserConfigurerTester(dataDirHandler);
		Properties prop = configurer.retrievePropertiesLoaded();
		String ftp_server = (String)prop.get("dataSource-gb-ftp-server.jdbcUrl");
		String users = (String)prop.get("dataSource-gb-users.jdbcUrl");
		assertEquals(ftp_server, "jdbc:h2:" + gbConfigDir + "/settings/database/ftpusers");
		assertEquals(users,  "jdbc:h2:" + gbConfigDir + "/settings/database/gbusers");
	}
	
	/**
	 * Set a GEOBATCH_CONFIG_DIR mock location, create the gb_database.properties file and test 
	 * if the returned jdbc URLs correctly points to those specified in the file
	 * @throws Exception
	 */
	@Test
	public void testGBDatabaseFile() throws Exception {
		
		Properties gb_database = null;
		File propGB = null;
		
		try{
			File gbConfigDir = TestData.file(this, "config");
			File gbSettingsDir = TestData.file(this, "config/settings");
			
			propGB = new File(gbSettingsDir + "/gb_database.properties");
			gb_database = new Properties();
			gb_database.setProperty("dataSource-gb-ftp-server.jdbcUrl", "jdbc:h2:OnlyForTestIfGBDATABASEisRead1");
			gb_database.setProperty("dataSource-gb-users.jdbcUrl", "jdbc:h2:OnlyForTestIfGBDATABASEisRead2");
			gb_database.store(new FileOutputStream(propGB), null);
			
    		        System.setProperty("GEOBATCH_CONFIG_DIR", gbConfigDir.getAbsolutePath());
			DataDirHandler dataDirHandler = new DataDirHandler();
			dataDirHandler.init();
			UserConfigurerTester configurer = new UserConfigurerTester(dataDirHandler);
			Properties prop = configurer.retrievePropertiesLoaded();
			String ftp_server = (String)prop.get("dataSource-gb-ftp-server.jdbcUrl");
			String users = (String)prop.get("dataSource-gb-users.jdbcUrl");
			assertEquals(ftp_server, "jdbc:h2:OnlyForTestIfGBDATABASEisRead1");
			assertEquals(users,  "jdbc:h2:OnlyForTestIfGBDATABASEisRead2");
		}
		finally{
			if (propGB != null && propGB.exists() && propGB.canWrite()){
			    assertTrue(propGB.delete());
				
			}
		}
	}
	
	/**
	 * Extends UsersDBPropertyOverrideConfigurer in order to expose the retrievePropertiesLoaded Method
	 * @author damiano
	 *
	 */
	private class UserConfigurerTester extends UsersDBPropertyOverrideConfigurer{
		
		public UserConfigurerTester(DataDirHandler dataDirHandler){
			super(dataDirHandler);
		}
		
		public Properties retrievePropertiesLoaded(){
			return getCustomProperties();
		}
	}

}
