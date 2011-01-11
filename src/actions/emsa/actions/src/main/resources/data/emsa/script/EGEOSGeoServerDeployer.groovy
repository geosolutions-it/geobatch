/** 
 * Java Imports ...
 **/
import it.geosolutions.geobatch.egeos.deployers.services.EGEOSRegistryDeployerConfiguration
import it.geosolutions.geobatch.egeos.deployers.actions.EGEOSDeployerBaseAction

import java.util.logging.Level
import java.util.logging.Logger

import java.util.HashMap;
import java.util.Map;

import it.geosolutions.geobatch.flow.event.IProgressListener
import it.geosolutions.geobatch.flow.event.ProgressListener
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder

import it.geosolutions.geobatch.flow.event.action.ActionException

import it.geosolutions.geobatch.action.egeos.emsa.PackageType
import it.geosolutions.geobatch.action.egeos.emsa.EMSAIOUtils

import it.geosolutions.geobatch.action.egeos.emsa.features.*;
import it.geosolutions.geobatch.action.scripting.ScriptingConfiguration;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

import java.net.MalformedURLException;
import java.net.URL;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.jdbc.JDBCDataStoreFactory;

/** 
 * Script Main "execute" function
 **/
/* ----------------------------------------------------------------- */
public List execute(ScriptingConfiguration configuration, String eventFilePath, ProgressListenerForwarder listenerForwarder) throws Exception {
		/* ----------------------------------------------------------------- */
		// Main Input Variables: must be configured 
		
		/** DataStore properties **/
		def database 	= "egeos"
		def dbtype 		= "postgis"
		def host 			= "localhost"
		def port 			= 5432
		def user 			= "postgres"
		def passwd 		= "postgres_matera"

    try {
        listenerForwarder.started();
				// ////
				// Instatiate EMSA Utilities
				// ////
				utils = new EMSAUtils();
				
        // ////////////////////////////////////////////////////////////////////
        //
        // Initializing input variables from Flow configuration
        //
        // ////////////////////////////////////////////////////////////////////
        /* Map props = configuration.getProperties();

        String example0 = props.get("key0");
        listenerForwarder.progressing(50, example0);
        String example1 = props.get("key1");
        listenerForwarder.progressing(90, example1); */

				// ////
				// some initial checks on input file name
				// ////
        String inputFileName = eventFilePath;
        final String filePrefix = FilenameUtils.getBaseName(inputFileName);
        final String fileSuffix = FilenameUtils.getExtension(inputFileName);
				if (!fileSuffix.equalsIgnoreCase("xml")) {
					sendError(listenerForwarder, "::EGEOSGeoServerDeployer : invalid input archive \"" + inputFileName + "\"");
				}
				
				// ////
				// forwarding some logging information to Flow Logger Listener
				// ////
        listenerForwarder.setTask("Processing event " + eventFilePath)

				/** The outcome events variable **/
				List results = new ArrayList();

				// ////
				// getting package directory
				// ////
				File pkgDir = new File(inputFileName).getParentFile();
				
				// ////
				// getting package type
				// ////
				PackageType type = utils.getPackageTypeFromName(FilenameUtils.getBaseName(pkgDir.getName()));
				
        // ////
        // Update GeoServer DataStore...
        // ////
        // connect to the store
        DataStore store = connect(database, dbtype, host, port, user, passwd);
        
        if (type != null) {
					if (type == PackageType.DER) {
						// deploy Ship Detections...
						File[] shipFiles = pkgDir.listFiles(new ShipDetectionNameFilter());
		
						ShipParser shipParser = new ShipParser();
		        for (File shipFile : shipFiles) {
            		shipParser.parseShip(store, ShipParser.xpath, shipFile);
		        }
		
		        store.dispose();

						// Forwarding Wind and Wave to GeoBatch METOC Actions...
		        File[] ncDerivedFiles = pkgDir.listFiles(new NetCDFNameFilter());
		
		        for (File ncFile : ncDerivedFiles) {
            		results.add(ncFile.getAbsolutePath());
		        }
		        
					} else if (type == PackageType.OSW || type == PackageType.OSN) {
						// deploy Oil Spills...
						File[] spillFiles = pkgDir.listFiles(new OilSpillNameFilter());
		        
		        SpillParser spillParser = new SpillParser();
		        for (File spillFile : spillFiles) {
            		spillParser.parseOilSpill(store, SpillParser.xpath, spillFile);
		        }
		
		        store.dispose();
					}
				}
				
				// ////
				// forwarding event to the next action
				// ////
				// fake event to avoid Failed Status!
				results.add("DONE");
        return results;
    } catch (Exception cause) {
	      sendError(listenerForwarder, cause.getLocalizedMessage(), cause);
    }
    
}

	// ///////////////////////////////////////////////////////////////////////////// //
	//                                                                               //
	//                       E-GEOS - U T I L I T I E S                              //
	//                                                                               //
	// ///////////////////////////////////////////////////////////////////////////// //

/** ****************************************************************************
    Script Utility Methods...
    **************************************************************************** **/
    
    /**
     * Error forwarder...
     **/
    void sendError(final ProgressListenerForwarder listenerForwarder, final String message) throws Exception {
    	sendError(listenerForwarder, message, null);
  	}
    void sendError(final ProgressListenerForwarder listenerForwarder, final String message, final Throwable cause) throws Exception {
    	/**
			 * Default LOGGER
			 **/
			final Logger LOGGER = Logger.getLogger(EGEOSDeployerBaseAction.class.toString());

    	LOGGER.log(Level.SEVERE, message);
	    Exception theCause = (cause != null ? cause : new Exception(message));
	    listenerForwarder.failed(theCause);
	    throw theCause;
	  }

		/**
		 * DS connect:
		 * - database
		 * - dbtype
		 * - host
		 * - port
		 * - user
		 * - passwd
		 */
		 private DataStore connect(
		 	String database,
		  String dbtype,
		  String host,
		  int port,
		  String user,
		  String passwd) throws IOException {
        Map params = new HashMap();
        params.put(JDBCDataStoreFactory.DATABASE.key, database);
        params.put(JDBCDataStoreFactory.DBTYPE.key,   dbtype);
        params.put(JDBCDataStoreFactory.HOST.key,     host);
        params.put(JDBCDataStoreFactory.PORT.key,     port);
        params.put(JDBCDataStoreFactory.USER.key,     user);
        params.put(JDBCDataStoreFactory.PASSWD.key,   passwd);
        // important as there are some chars that need escaping
        params.put(PostgisNGDataStoreFactory.PREPARED_STATEMENTS.key, Boolean.TRUE);
        
        return DataStoreFinder.getDataStore(params);
    }