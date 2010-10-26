/** 
 * Java Imports ...
 **/
import it.geosolutions.geobatch.action.scripting.GroovyAction

import java.util.logging.Level
import java.util.logging.Logger

import it.geosolutions.geobatch.flow.event.IProgressListener
import it.geosolutions.geobatch.flow.event.ProgressListener
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder

import it.geosolutions.geobatch.flow.event.action.ActionException

import it.geosolutions.geobatch.action.scripting.ScriptingConfiguration

import org.apache.commons.io.FilenameUtils

import ucar.ma2.InvalidRangeException
import ucar.nc2.NetcdfFile
import ucar.nc2.NetcdfFileWriteable
import ucar.nc2.Variable

/** 
 * Script execute function
 **/
public String execute(ScriptingConfiguration configuration, String eventFilePath, ProgressListenerForwarder listenerForwarder) throws ActionException {
	  final Logger LOGGER = Logger.getLogger(GroovyAction.class.toString());
	  
    try {
        listenerForwarder.started();

				println(eventFilePath);

        String inputFileName = eventFilePath;
        final String filePrefix = FilenameUtils.getBaseName(inputFileName);
        final String fileSuffix = FilenameUtils.getExtension(inputFileName);

				NetcdfFile ncGridFile = NetcdfFile.open(eventFilePath);

        listenerForwarder.setTask("Processing event " + eventFilePath)

				// DO SOMETHING HERE

        // ////////////////////////////////////////////////////////////////////
        //
        // Initializing input variables
        //
        // ////////////////////////////////////////////////////////////////////
        Map props = configuration.getProperties();

        String example0 = props.get("key0");
        listenerForwarder.progressing(50, example0);
        String example1 = props.get("key1");
        listenerForwarder.progressing(90, example1);

        return new File(example0).getAbsolutePath();
    } catch (Exception t) {
        LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t); // no need to log, we're rethrowing it
        listenerForwarder.failed(t);
        throw new ActionException(this, t.getMessage(), t);
    }
}
