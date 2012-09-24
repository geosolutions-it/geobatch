/* Copyright (c) 2011 GeoSolutions - http://www.geo-solutions.it/.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package it.geosolutions.geobatch.users;

import it.geosolutions.geobatch.catalog.file.DataDirHandler;
import it.geosolutions.geobatch.catalog.file.GeoBatchDataDirAwarePropertyOverrideConfigurer;
import it.geosolutions.tools.commons.file.Path;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.util.logging.Logging;
/**
 * 
 * Specific implementation of a {@link GeoBatchDataDirAwarePropertyOverrideConfigurer} for placing properly the database for
 * users and ftp users in GeoBatch.
 * 
 * <p> This class is a special {@link GeoBatchDataDirAwarePropertyOverrideConfigurer} that:
 * <ol>
 *   <li> By default looks first for a property override file gb_database.properties inside the <b>settings</b> directory 
 *   <li> If there is no default gb_database.properties file inside the settings directory it tries to place the dbs inside the settings/database directory if that exists and is writable
 *   <li> It still allows users to specify locations from where loading the properties. This can be done by placing a gb_datastore.properties file inside the home directory of the GeoServer process or directly specificng a full path for property file via the DATABASE_CONFIG_FILE env property.
 * </ol>
 * @author Simone Giannecchini, GeoSolutions SAS.
 *
 */
public class UsersDBPropertyOverrideConfigurer extends GeoBatchDataDirAwarePropertyOverrideConfigurer{
	
	/**Logger.*/
	static final private Logger LOGGER= Logging.getLogger(UsersDBPropertyOverrideConfigurer.class);
	
	/** 
	 * Constructor..
	 * 
	 * @param dataDirHandler a valid {@link DataDirHandler}. Cannot be <code>null</code>.
	 */
	public UsersDBPropertyOverrideConfigurer(final DataDirHandler dataDirHandler) {
		super(dataDirHandler,false);
	}

	@Override
	protected Properties getCustomProperties() {

		// do we have a gb_database.properties file inside the config dir?
		final File file=Path.findLocation("settings/gb_database.properties", dataDirectoryHandler.getBaseConfigDirectory());
		if(file==null||!(file.isFile()&&file.canRead()&&file.exists())){
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
					LOGGER.fine("Unable to set embedded database to work in the internal directory, check that such directory exists and it is writable");
				}	
			}
		} else {
			if(file!=null&&file.isFile()&&file.canRead()&&file.exists()){
				if(LOGGER.isLoggable(Level.FINE)){
					LOGGER.fine("Loading default property file");
				}

				try {
					return loadProperties(file);
				} catch (IOException e) {
					if(LOGGER.isLoggable(Level.SEVERE)){
						LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
					}
				}
			} 			
		}
		return null;
	}
}