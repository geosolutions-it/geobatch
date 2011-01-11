
	// ///////////////////////////////////////////////////////////////////////////// //
	//                                                                               //
	//                         EMSA - U T I L I T I E S                              //
	//                                                                               //
	// ///////////////////////////////////////////////////////////////////////////// //

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

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

class EMSAUtils {

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
			final Logger LOGGER = Logger.getLogger(EMSAUtils.class.toString());

    	LOGGER.log(Level.SEVERE, message);
	    Exception theCause = (cause != null ? cause : new Exception(message));
	    listenerForwarder.failed(theCause);
	    throw theCause;
	  }
	  
	  /**
	   * Create Input Data Dir -
	   *  - where to extract the packages for further processing
	   **/
	  File createInputDataDirIfNotExists(final String path) throws Exception {
				File inputDataDir = new File(path);
				if (inputDataDir.exists() && inputDataDir.isDirectory()) {
					return inputDataDir;
				} else if (!inputDataDir.exists()){
					if (inputDataDir.mkdirs()) {
						return inputDataDir;
					} else {
						sendError("Could not create EMSA input data dir.");
					}
				}

			 sendError("Input data dir path specified already exists and is not a folder.");
		}
		
    /**
     * TAG ID
     **/
    String getTagId() {
			return "_EMSA_service_tag";
    }
    
    /**
     * Retrieve Package Type from its full name
     *  - expected a package name ending with "_<type>"
     **/
   	PackageType getPackageTypeFromName(final String name) {
   		if (name == null)
   			return null;
   			
   		String suffix = name.substring(name.lastIndexOf("_")+1);
   		try {
				return PackageType.valueOf(suffix)
   		} catch (Exception e) {
   			return null;
   		}
		}
		
		/**
		 * Check if delivery is ready to be processed
		 **/
		boolean isPackageReady(final File baseDir, final String packageName) {
			// 11 -> date length
			String prefix = packageName.substring(11, packageName.lastIndexOf("_"));
			String pkgRdyFileName = /* prefix + ".txt"*/ "PackageReady.txt";
			
			File packageReadyFile = new File(baseDir, pkgRdyFileName);
						
			if (packageReadyFile.exists()) {
				File[] packages = baseDir.listFiles(new PackageNameFilter(prefix));
			
				for (File pkg : packages) {
					if (getPackageTypeFromName(pkg.getName()) == PackageType.EOP) {
						return packageReadyFile.renameTo(new File(pkg, pkgRdyFileName));
					}
				}
			} else {
				File[] packages = baseDir.listFiles(new PackageNameFilter(prefix));
			
				for (File pkg : packages) {
					if (getPackageTypeFromName(pkg.getName()) == PackageType.EOP) {
						packageReadyFile = new File(pkg, pkgRdyFileName);
						return packageReadyFile.exists();
					}
				}
			}
			
			return false;
		}
		
		/**
		 * Retrieve EOP Packages not ready...
		 **/
		File getEOPNotReady(final File baseDir) {
			String pkgRdyFileName = /* prefix + ".txt"*/ "PackageReady.txt";
			
			File[] packages = baseDir.listFiles(new EOPPackageNameFilter());
			
			for (File pkg : packages) {
				File packageReadyFile = new File(pkg, pkgRdyFileName);
				if(!packageReadyFile.exists()) {
					FileUtils.touch(new File(pkg, pkgRdyFileName));
					return pkg;
				}
			}
			
			return null;
		}
		 
		/**
		 * Check if the EOP package is available for the same product
		 **/
		boolean isAvailableEOP(final File baseDir, final String packageName) {
			// 11 -> date length
			String prefix = packageName.substring(11, packageName.lastIndexOf("_"));
			
			File[] packages = baseDir.listFiles(new PackageNameFilter(prefix));
			
			for (File pkg : packages) {
				if (getPackageTypeFromName(pkg.getName()) == PackageType.EOP) {
					return true;
				}
			}

			return false;
		}
		
		/**
		 * Create an empty tag file into package deflated dir
		 **/
		boolean tagPackage(final File deflatedInputDir) {
			File tag = new File(deflatedInputDir, getTagId());
			
			// ////
			// Touch the file, when the file is not exist a new file will be
			// created. If the file exist change the file timestamp.
			// ////
			FileUtils.touch(tag);
			
			if (tag.exists() && !tag.isDirectory()) {
				return true;
			} else {
				return false;
			}
		}

		/**
		 * Remove tag from directory
		 **/
		boolean unTagPackage(final File deflatedInputDir) {
			File tag = new File(deflatedInputDir, getTagId());
			
			if (tag.exists() && !tag.isDirectory()) {
				return tag.delete();
			}
		}

		/**
		 * Remove tag from directory
		 **/
		boolean isTagged(final File deflatedInputDir) {
			File tag = new File(deflatedInputDir, getTagId());
			
			return (tag.exists() && !tag.isDirectory());
		}
		
		/**
		 * Returns the list of tagged packages for the specified product
		 **/
		File[] getTaggedPackages(final File baseDir, final File deflatedInputDir) {
			final String packageName = FilenameUtils.getBaseName(deflatedInputDir.getName());
			// 11 -> date length
			String prefix = packageName.substring(11, packageName.lastIndexOf("_"));
			
			File[] packages = baseDir.listFiles(new PackageNameFilter(prefix));
			
			def taggedPackages = []
			
			for (File pkg : packages) {
				if (isTagged(pkg)) {
					taggedPackages.add(pkg)
				}
			}

			return taggedPackages;
		}
		
		
		/** ******************
		 *  Packages Processing...
		 *
		 ** ****************** */
		void processPackage(PackageType type, final File baseDir, final File deflatedInputDir, List results) {

			// 11 -> date length
			String packageName = FilenameUtils.getBaseName(deflatedInputDir.getName());
			String prefix = packageName.substring(11, packageName.lastIndexOf("_"));
			
			File[] packages = baseDir.listFiles(new PackageNameFilter(prefix));
			
		  for (File pkg : packages) {
				if (getPackageTypeFromName(pkg.getName()) == type) {
					File pckFile = new File(pkg, prefix + "_PCK.xml");
					if (pckFile.exists() && !pckFile.isDirectory())	{
						results.add(pckFile.getAbsolutePath());
					} else if (type == PackageType.PRO) {
						results.add(pkg.getAbsolutePath());
					}
					break;
				}
			}
		}

}