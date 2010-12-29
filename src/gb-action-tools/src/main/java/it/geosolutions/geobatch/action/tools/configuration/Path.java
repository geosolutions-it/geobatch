package it.geosolutions.geobatch.action.tools.configuration;

import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.global.CatalogHolder;

import java.io.File;
import java.io.IOException;

public final class Path {

    /**
     * Obtaining the Absolute path of the working dir
     * @param working_dir the relative (or absolute) path to absolutize
     * @note it should be a sub-dir of ...
* @TODO open a ticket to get getBaseDirectory() into Catalog interface
     */
    public static String getAbsolutePath(String working_dir) /*throws FileNotFoundException */{ 
        FileBaseCatalog c = (FileBaseCatalog) CatalogHolder.getCatalog();
        
        File fo=null;
        try {
            fo=it.geosolutions.geobatch.tools.file.Path.findLocation(working_dir,new File(c.getBaseDirectory()));
        }
        catch (IOException ioe){
            return null;
        }
        
        if (fo!=null){
            return fo.toString();
        }
        else {
//TODO LOG            throw new FileNotFoundException("Unable to locate the working dir");
//            throw new FileNotFoundException();
            return null;
        }
    } 

}
