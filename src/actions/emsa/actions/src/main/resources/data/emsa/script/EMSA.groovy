/** 
 * Java Imports ...
 **/
import it.geosolutions.geobatch.action.egeos.emsa.EMSAAction

import java.util.logging.Level
import java.util.logging.Logger

import java.util.List
import java.util.ArrayList

import java.util.logging.Level

import it.geosolutions.geobatch.flow.event.IProgressListener
import it.geosolutions.geobatch.flow.event.ProgressListener
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder

import it.geosolutions.geobatch.flow.event.action.ActionException

import it.geosolutions.geobatch.action.scripting.ScriptingConfiguration

import it.geosolutions.geobatch.action.egeos.emsa.PackageType
import it.geosolutions.geobatch.action.egeos.emsa.EMSAIOUtils

import it.geosolutions.geobatch.tools.file.IOUtils;

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

/** 
 * Script Main "execute" function
 **/
public List execute(ScriptingConfiguration configuration, String eventFilePath, ProgressListenerForwarder listenerForwarder) throws Exception {
		/* ----------------------------------------------------------------- */
		// Main Input Variables: must be configured 
		/**
		 * The physical folder where to extract emsa packages
		 **/
		def emsaExchangePhysicalDir = "/home/carlo/work/data/emsa/out/";
		
    try {
        listenerForwarder.started();
        // ///
        // Instatiate EMSA Utilities
        // ////
        def utils = new EMSAUtils();

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

        if(!IOUtils.acquireLock(this, new File(inputFileName))) {
            utils.sendError(listenerForwarder, "::EMSAService : could not acquire lock on \"" + inputFileName + "\"");
        }

        final String filePrefix = FilenameUtils.getBaseName(inputFileName);
        final String fileSuffix = FilenameUtils.getExtension(inputFileName);
        if (!fileSuffix.equalsIgnoreCase("tar") && !fileSuffix.equalsIgnoreCase("txt")) {
            utils.sendError(listenerForwarder, "::EMSAService : invalid input archive \"" + inputFileName + "\"");
        }

        // ////
        // forwarding some logging information to Flow Logger Listener
        // ////
        listenerForwarder.setTask("Processing event " + eventFilePath)

        // ////
        // defining the Input Data Dir:
        //  - here the input packages will be extracted to be further processed
        //  - please provide an absolute path
        // ////
        File inputDataDir = utils.createInputDataDirIfNotExists(listenerForwarder,emsaExchangePhysicalDir);

        // ////
        // decompressing archive
        // ////
        File deflatedInputDir = null;

        // ////
        // getting package type
        // ////
        PackageType type = null;

        if (fileSuffix.equalsIgnoreCase("txt")) {
            deflatedInputDir = utils.getEOPNotReady(inputDataDir);
            type = PackageType.EOP;
        } else {
            deflatedInputDir = EMSAIOUtils.unTar(new File(eventFilePath), inputDataDir);
            type = utils.getPackageTypeFromName(FilenameUtils.getBaseName(deflatedInputDir.getName()));
        }

        // ////
        // Checking Package Type and deploy.
        //  - what we are going to do is:
        //    a) check if the EOP package is available on the inputDataDir for the same package
        //     a.0) if the package IS the EOP, process all tagged packages of the same type
        //     a.1) if is available deploy the package
        //    b) tag the package waiting for the EOP arrival
        // ////
        /** The outcome events variable **/
        List results = new ArrayList();
        if (deflatedInputDir != null && type != null) {
            if (type == PackageType.EOP && utils.isPackageReady(inputDataDir, FilenameUtils.getBaseName(deflatedInputDir.getName()))) {
                utils.processPackage(type, inputDataDir, deflatedInputDir, results);

                File[] pendingPackages = utils.getTaggedPackages(inputDataDir, deflatedInputDir);

                if (pendingPackages != null && pendingPackages.length > 0 && results.size() > 0) {
                    for (File packageDir : pendingPackages) {
                        utils.unTagPackage(packageDir);
                        type = utils.getPackageTypeFromName(FilenameUtils.getBaseName(packageDir.getName()));
                        utils.processPackage(type, inputDataDir, packageDir, results);
                    }
                }
            } else if (!utils.isAvailableEOP(inputDataDir, FilenameUtils.getBaseName(deflatedInputDir.getName())) ||
            !utils.isPackageReady(inputDataDir, FilenameUtils.getBaseName(deflatedInputDir.getName()))
            ) {
                utils.tagPackage(deflatedInputDir);
            } else {
                utils.unTagPackage(deflatedInputDir);
                utils.processPackage(type, inputDataDir, deflatedInputDir, results);
            }
        } else {
            utils.sendError(listenerForwarder, "::EMSAService : invalid package \"" + inputFileName + "\". Maybe it has been already processed!");
        }

        // ////
        // forwarding event to the next action
        // ////
        // fake event to avoid Failed Status!
        results.add("DONE");
        return results;
    } catch (Exception cause) {
	      utils.sendError(listenerForwarder, cause.getLocalizedMessage(), cause);
    }
    
}
