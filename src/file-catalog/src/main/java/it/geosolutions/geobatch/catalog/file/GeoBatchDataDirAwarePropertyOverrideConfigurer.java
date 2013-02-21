/* Copyright (c) 2011 GeoSolutions - http://www.geo-solutions.it/.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package it.geosolutions.geobatch.catalog.file;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.log4j.LogManager;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
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
public abstract class GeoBatchDataDirAwarePropertyOverrideConfigurer extends PropertyOverrideConfigurer{
	
	/**Logger.*/
	static final private org.apache.log4j.Logger LOGGER= LogManager.getLogger(GeoBatchDataDirAwarePropertyOverrideConfigurer.class);

	/**This object mediates access to the GeoServer data directory.**/
	protected final DataDirHandler dataDirectoryHandler;
	
	
	/** 
	 * Constructor.
	 * 
	 * <p> Allows subclasses or users to specify from which directory which properties files to load.
	 * 
	 * @param dataDirectory a valid {@link GeoServerDataDirectory}. Cannot be <code>null</code>.
	 * @param parentDirectory An absolute path to the directory where to look for properties files.
	 * @param propertiesFiles List of names (with extension) of properties files to load.
	 */
	public GeoBatchDataDirAwarePropertyOverrideConfigurer(final DataDirHandler dataDirectory, final boolean localOverride) {
		//checks
		if(dataDirectory==null){
			throw new IllegalArgumentException("Unable to proceed with the placeholder initialization as the provided DataDirHandler is null!");
		}
		this.dataDirectoryHandler = dataDirectory;
		
		// localOverride to true, so that what we do here takes precedence over external properties
		setLocalOverride(localOverride);
		
		//
		// set the properties
		//
		// load from properties file	
		final Properties props=getCustomProperties();
		if(props!=null){
			if(LOGGER.isTraceEnabled()){
				LOGGER.trace("Trying to set default properties:\n"+props);
			}						
			setProperties(props);
		}
	}

	/**
	 * Method that subclasses should implement to inject custom default properties in his override.
	 * 
	 * @return {@link Properties} to set as defaults
	 */
	protected abstract Properties getCustomProperties();
	
	/**
	 * Tries to find the provided properties file and then returns its properties.
	 * 
	 * @param propertiesFile the {@link File} to load from. Must exists and be readable.
	 * @throws IOException in case something bad happens trying to read the proeprties from the file.
	 * 
	 */
	protected static Properties loadProperties(final File propertiesFile)
			throws IOException {
		// checks on provided file
		if(!(propertiesFile!=null&&propertiesFile.exists()&&propertiesFile.isFile()&&propertiesFile.canRead())){
			return null;
		}
		// load the file
		InputStream is=null;
		try{
			is= new BufferedInputStream(new FileInputStream(propertiesFile));
			final Properties properties= new Properties();
			properties.load(is);
			return properties;
		} finally {
			// close
			if(is!=null){
				try{
					is.close();
				}catch (Exception e) {
					if(LOGGER.isTraceEnabled()){
						LOGGER.trace(e.getLocalizedMessage(),e);
					}
				}
			}
		}
	}
}