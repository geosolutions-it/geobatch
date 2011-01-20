/** 
 * Java Imports ...
 **/
import it.geosolutions.geobatch.egeos.deployers.services.EGEOSRegistryDeployerConfiguration
import it.geosolutions.geobatch.egeos.deployers.actions.EGEOSDeployerBaseAction

import java.util.logging.Level
import java.util.logging.Logger

import it.geosolutions.geobatch.flow.event.IProgressListener
import it.geosolutions.geobatch.flow.event.ProgressListener
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder

import it.geosolutions.geobatch.flow.event.action.ActionException

import it.geosolutions.geobatch.action.scripting.ScriptingConfiguration

import it.geosolutions.geobatch.action.egeos.emsa.PackageType
import it.geosolutions.geobatch.action.egeos.emsa.EMSAIOUtils

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
    /** Web Service URL 
     *  - IMPORTANT: DON'T FORGET THE '/' AT THE END OF 'httpdServiceURL'
     **/
    def httpdServiceURL = "http://ows-csn.e-geos.it/e-geos/";

    /** Server physical directory:
     *  - where to copy files
     **/
    def httpdPhysicalBaseDir = "/home/tomcat/e-geos/";
    //def httpdPhysicalBaseDir = "/home/carlo/work/data/emsa/out/";

    try {
        listenerForwarder.started();
        // ////
        // Instatiate EMSA Utilities
        // ////
        utils = new EMSAUtils();

        // ////
        // some initial checks on input file name
        // ////
        String inputFileName = eventFilePath;
        final String filePrefix = FilenameUtils.getBaseName(inputFileName);
        final String fileSuffix = FilenameUtils.getExtension(inputFileName);
        if (!fileSuffix.equalsIgnoreCase("xml")) {
            sendError(listenerForwarder, "::EGEOSWebDeployer : invalid input archive \"" + inputFileName + "\"");
        }

        // ////
        // forwarding some logging information to Flow Logger Listener
        // ////
        listenerForwarder.setTask("::EGEOSWebDeployer : Processing event " + eventFilePath)

        /** The outcome events variable **/
        List results = new ArrayList();

        // ////
        // getting package directory
        // ////
        File pkgDir = new File(inputFileName).getParentFile();
/** DO NOT CHANGE THIS! **/
//String pkgDirName = FilenameUtils.getBaseName(pkgDir.getName()).substring(11) + "/";

        // ////
        // getting package type
        // ////
//PackageType type = utils.getPackageTypeFromName(FilenameUtils.getBaseName(pkgDir.getName()));

        // ////
        // Copy files...
        // ////
        // creating sub-folder if not exists...
//println(httpdPhysicalBaseDir + "/" + pkgDir.getName());
        File pkgOutputDataDir = utils.createInputDataDirIfNotExists(listenerForwarder,httpdPhysicalBaseDir + "/" + pkgDir.getName());
        println(pkgOutputDataDir.getAbsolutePath());

        if (pkgOutputDataDir != null && pkgOutputDataDir.exists() && pkgOutputDataDir.isDirectory()) {
            FileUtils.copyDirectory(pkgDir, pkgOutputDataDir, true);
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