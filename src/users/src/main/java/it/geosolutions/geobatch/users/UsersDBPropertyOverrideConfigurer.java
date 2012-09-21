/* Copyright (c) 2011 GeoSolutions - http://www.geo-solutions.it/.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package it.geosolutions.geobatch.users;

import it.geosolutions.geobatch.catalog.file.DataDirHandler;
import it.geosolutions.geobatch.catalog.file.GeoBatchDataDirAwarePropertyOverrideConfigurer;
import it.geosolutions.tools.commons.file.Path;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.util.logging.Logging;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
/**
 * 
 * GeoServer's specific subclass for {@link PropertyPlaceholderConfigurer} Spring bean to load properties file.
 * 
 * <p> This class is a special {@link PropertyPlaceholderConfigurer} that:
 * <ol>
 *   <li> By default load properties from a specific file inside a specific dir of the GeoServer data dir. Hence it is aware of the GeoServer data dir. This id done via setting internally {@link #setLocalOverride(boolean)} to <code>true</code> </li>
 *   <li> It still allows users to specify locations from where loading the properties. In this case we must set localovveride property to false though.
 * </ol>
 * @author Simone Giannecchini, GeoSolutions SAS.
 *
 */
public class UsersDBPropertyOverrideConfigurer extends GeoBatchDataDirAwarePropertyOverrideConfigurer{
	
	/**Logger.*/
	static final private Logger LOGGER= Logging.getLogger(UsersDBPropertyOverrideConfigurer.class);
	
	/** 
	 * Constructor.
	 * 
	 * <p> Allows subclasses or users to specify from which directory which properties files to load.
	 * 
	 * @param dataDirectory a valid {@link GeoServerDataDirectory}. Cannot be <code>null</code>.
	 */
	public UsersDBPropertyOverrideConfigurer(final DataDirHandler dataDirectory) {
		super(dataDirectory,false);
	}

	@Override
	protected Properties getCustomProperties() {

		// do we have a gb_database.properties file inside the config dir?
		final File file=Path.findLocation("settings/gb_database.properties", dataDirectoryHandler.getBaseConfigDirectory());
		if(file==null){
			if(LOGGER.isLoggable(Level.FINE)){
				LOGGER.fine("Setting embedded database to work in the internal directory");
			}
			// move on with default
			final File dbDir=Path.findLocation("settings/database", dataDirectoryHandler.getBaseConfigDirectory());
			if(dbDir!=null&&dbDir.isDirectory()&&dbDir.canWrite()){

				final Properties properties=new Properties();
				properties.put("dataSource-gb-users.jdbcUrl", "jdbc:h2:"+dbDir.getAbsolutePath()+"/gbusers");
				properties.put("dataSource-gb-ftp-server.jdbcUrl", "jdbc:h2:"+dbDir.getAbsolutePath()+"/ftpusers");			
				return properties;
			
			} else {
				if(LOGGER.isLoggable(Level.FINE)){
					LOGGER.fine("Unable to set embedded database to work in the internal directory, check that it exists and it is writable");
				}	
			}
		} 
		return null;
	}
}