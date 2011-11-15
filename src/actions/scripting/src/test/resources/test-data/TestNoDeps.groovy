/** 
 * Java Imports ...
 **/
/*
import it.geosolutions.geobatch.action.scripting.ScriptingConfiguration
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder
import it.geosolutions.geobatch.flow.event.action.ActionException

*/
import org.apache.commons.io.FilenameUtils

import java.util.logging.Level
import java.util.logging.Logger

/** 
 * Script execute function
 **/

public List<String> execute(Object configuration, String eventFilePath, Object listenerForwarder) {
	  final Logger LOGGER = Logger.getLogger("TestNoDeps");
	  
    try {
        listenerForwarder.started();

        println(eventFilePath);

        String inputFileName = eventFilePath;
        final String filePrefix = FilenameUtils.getBaseName(inputFileName);
        final String fileSuffix = FilenameUtils.getExtension(inputFileName);

        listenerForwarder.setTask("Processing event " + eventFilePath)

        // ////////////////////////////////////////////////////////////////////

        /// Extract values from config map
        Map props = configuration.getProperties();

        String v0 = props.get("key0"); // key0 non defined, should be null
        listenerForwarder.progressing(50, v0);
        String v1 = props.get("k1");   // k1 value should be "v1"
        listenerForwarder.progressing(90, v1);

        List ret = new ArrayList();
        ret.add(v0!=null?v0:"null");
        ret.add(v1!=null?v1:"null");

        /// Extract explicit values
        ret.add(k1!=null?k1:"null");
        ret.add(k2!=null?k2:"null");
		intVar++;
		ret.add(intVar!=null?intVar.toString():"null");
        //ret.add(pippo!=null?pippo:"null");

        return ret;

    } catch (Exception t) {
        LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t); // no need to log, we're rethrowing it
        listenerForwarder.failed(t);
        throw new Exception(t.getMessage(), t);
    }
}
