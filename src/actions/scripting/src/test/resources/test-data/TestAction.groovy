/** 
 * Java Imports ...
 **/
import it.geosolutions.geobatch.action.scripting.ScriptingConfiguration
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder
import it.geosolutions.geobatch.flow.event.action.ActionException

import java.util.logging.Level
import java.util.logging.Logger

import org.apache.commons.io.FilenameUtils

import ucar.nc2.NetcdfFile

/** 
 * Script execute function
 **/
public Map execute(Map argsMap) throws Exception {
	//	DataStoreHandler.jdbcTest("/home/carlo/work/project/ciok/trunk/gaez/GEOBATCH_DATA_DIR/gaez_flow/config/jdbc.properties");

	final ScriptingConfiguration configuration=argsMap.get(ScriptingAction.CONFIG_KEY);
	final String runningContext=argsMap.get(ScriptingAction.CONTEXT_KEY);
	final File runningContextDir=new File(runningContext);
	//	final List events=argsMap.get(ScriptingAction.EVENTS_KEY);
	final ProgressListenerForwarder listenerForwarder=argsMap.get(ScriptingAction.LISTENER_KEY);

	final Logger LOGGER = Logger.getLogger(GroovyAction.class);

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

		return null;
	} catch (Exception t) {
		LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t); // no need to log, we're rethrowing it
		listenerForwarder.failed(t);
		throw new ActionException(this, t.getMessage(), t);
	}
}
