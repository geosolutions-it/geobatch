/**
 * 
 */
package it.geosolutions.geobatch.imagemosaic;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.util.Converters;

/**
 * @author afabiani
 * 
 *
 */
public class Utils {
	/**
	 * Logger.
	 */
	private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(Utils.class.toString());
	
	/**
	 * Checks that a {@link File} is a real file, exists and is readable.
	 * 
	 * @param file
	 *            the {@link File} instance to check. Must not be null.
	 * 
	 * @return <code>true</code> in case the file is a real file, exists and is
	 *         readable; <code>false </code> otherwise.
	 */
	public static boolean checkFileReadable(final File file) {
		if (LOGGER.isLoggable(Level.FINE)) {
			final StringBuilder builder = new StringBuilder();
			builder.append("Checking file:").append(
					FilenameUtils.getFullPath(file.getAbsolutePath())).append(
					"\n");
			builder.append("canRead:").append(file.canRead()).append("\n");
			builder.append("isHidden:").append(file.isHidden()).append("\n");
			builder.append("isFile").append(file.isFile()).append("\n");
			builder.append("canWrite").append(file.canWrite()).append("\n");
			LOGGER.fine(builder.toString());
		}
		if (!file.exists() || !file.canRead() || !file.isFile())
			return false;
		return true;
	}
	
	/**
	 * 
	 * @param propsURL
	 * @return
	 */
	public static Properties loadPropertiesFromURL(URL propsURL) {
		final Properties properties = new Properties();
		InputStream stream = null;
		InputStream openStream = null;
		try {
			openStream = propsURL.openStream();
			stream = new BufferedInputStream(openStream);
			properties.load(stream);
		} catch (FileNotFoundException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return null;
		} catch (IOException e) {
			if (LOGGER.isLoggable(Level.SEVERE))
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			return null;
		} finally {

			if (stream != null)
				IOUtils.closeQuietly(stream);

			if (openStream != null)
				IOUtils.closeQuietly(openStream);

		}
		return properties;
	}
	
	/**
	 * 
	 * @param properties
	 * @param spi
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Serializable> createDataStoreParamsFromPropertiesFile(
			Properties properties, DataStoreFactorySpi spi) throws IOException {
		// get the params
		final Map<String, Serializable> params = new HashMap<String, Serializable>();
		final Param[] paramsInfo = spi.getParametersInfo();
		for (Param p : paramsInfo) {
			// search for this param and set the value if found
			if (properties.containsKey(p.key))
				params.put(p.key, (Serializable) Converters.convert(properties.getProperty(p.key), p.type));
			else if (p.required && p.sample == null)
				throw new IOException("Required parameter missing: "+ p.toString());
		}
		
		return params;
	}
}
