/**
 * 
 */
package it.geosolutions.geobatch.users;

import it.geosolutions.geobatch.catalog.file.DataDirHandler;
import it.geosolutions.geobatch.catalog.file.GeoBatchDataDirAwarePropertyOverrideConfigurer;

import java.io.IOException;
import java.util.Properties;

import org.geotools.TestData;

/**
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class CustomPropertyOverride extends
		GeoBatchDataDirAwarePropertyOverrideConfigurer {

	static final String SETTINGS_GBUSERS = "/settings/database/gbusers";

	/**
	 * @param dataDirectory
	 */
	public CustomPropertyOverride(DataDirHandler dataDirectory) {
		super(dataDirectory, false);
	}

	/* (non-Javadoc)
	 * @see it.geosolutions.geobatch.catalog.file.GeoBatchDataDirAwarePropertyOverrideConfigurer#getCustomProperties()
	 */
	@Override
	protected Properties getCustomProperties() {
		final Properties properties=new Properties();
		try {
			properties.put("dataSource-gb-users.jdbcUrl", "jdbc:h2:"+TestData.file(this,"config").getAbsolutePath()+SETTINGS_GBUSERS);
			return properties;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
	
	public Properties getProperties() throws IOException{
		return mergeProperties();
	}
}
