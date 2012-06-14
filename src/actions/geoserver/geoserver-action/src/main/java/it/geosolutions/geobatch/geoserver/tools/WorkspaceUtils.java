package it.geosolutions.geobatch.geoserver.tools;

import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class provide a collection of useful static method for interact with
 * GeoServer for the Workspace Creation
 * 
 * @author DamianoG
 * 
 */
public abstract class WorkspaceUtils {

	/**
	 * This Utility method create a workspace on geoserver with the provided
	 * name.
	 * 
	 * @param reader
	 *            not null
	 * @param publisher
	 *            not null
	 * @param defaultNamespace
	 * @param defaultNamespaceUri
	 * @return true if the workspace is created, false if a workspace with the
	 *         provided name already exist.
	 * @throws URISyntaxException
	 */
	public static boolean createWorkspace(GeoServerRESTReader reader,
			GeoServerRESTPublisher publisher, String defaultNamespace,
			String defaultNamespaceUri) throws URISyntaxException {

		if (reader == null || publisher == null) {
			throw new IllegalArgumentException(
					"params reader and default namespace must NOT be NULL or empty");
		}

		// Check if the namespace exists in GS. If not, create it.
		if (!reader.getWorkspaceNames().contains(defaultNamespace)) {
			if (defaultNamespaceUri != null) {
				publisher.createNamespace(defaultNamespace, new URI(
						defaultNamespaceUri));
			} else {
				if (defaultNamespace == null || defaultNamespace.isEmpty()) {
					throw new IllegalStateException(
							"Providing an empty or null defaultNamespaceUri defaultNamespace must rappresent a valid workspace name");
				}
				// No uri given; create a workspace
				publisher.createWorkspace(defaultNamespace);
			}
			return true;
		}

		return false;
	}

	/**
	 * This Utility method create a workspace on geoserver with the provided
	 * name.
	 * 
	 * @param geoserverURL
	 * @param geoserverUID
	 * @param geoserverPWD
	 * @param defaultNamespace
	 * @param defaultNamespaceUri
	 * @return true if the workspace is created, false if a workspace with the
	 *         provided name already exist.
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 */
	public static boolean createWorkspace(String geoserverURL,
			String geoserverUID, String geoserverPWD, String defaultNamespace,
			String defaultNamespaceUri) throws URISyntaxException,
			MalformedURLException {

		if (geoserverURL == null || geoserverUID == null
				|| geoserverPWD == null || geoserverURL.isEmpty()
				|| geoserverUID.isEmpty()) {
			throw new IllegalArgumentException(
					"Geoserver URL, UID and PWD must NOT be NULL or empty");
		}

		// Get GS reader & publisher
		GeoServerRESTReader reader = new GeoServerRESTReader(geoserverURL,
				geoserverUID, geoserverPWD);
		GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(
				geoserverURL, geoserverUID, geoserverPWD);

		return createWorkspace(reader, publisher, defaultNamespace,
				defaultNamespaceUri);
	}

	/**
	 * This Utility method create a workspace on geoserver with the provided
	 * name.
	 * 
	 * @param configuration
	 * @return true if the workspace is created, false if a workspace with the
	 *         provided name already exist.
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static boolean createWorkspace(
			GeoServerActionConfiguration configuration)
			throws MalformedURLException, URISyntaxException {

		if (configuration == null) {
			throw new IllegalArgumentException(
					"something must NOT be NULL or empty");
		}

		return createWorkspace(configuration.getGeoserverURL(),
				configuration.getGeoserverUID(),
				configuration.getGeoserverPWD(),
				configuration.getDefaultNamespace(),
				configuration.getDefaultNamespaceUri());
	}

}
