import java.util.logging.Level;
import java.util.logging.Logger;

import it.geosolutions.geobatch.flow.event.action.BaseAction
import it.geosolutions.geobatch.flow.event.action.Action
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType
import it.geosolutions.geobatch.flow.event.action.ActionException
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog
import it.geosolutions.geobatch.action.scripting.ScriptingAction
import it.geosolutions.geobatch.action.scripting.ScriptingConfiguration

class GAction extends ScriptingAction implements Action {

    private final static Logger LOGGER = Logger.getLogger(GAction.class.toString());

    public GAction(ScriptingConfiguration configuration) throws IOException {
        super(configuration);
    }

    public Queue<FileSystemEvent> execute(
        Queue<FileSystemEvent> events) throws ActionException {
        try {
            listenerForwarder.started();

            // looking for file
            if (events.size() != 1) {
                throw new IllegalArgumentException(
                        "Wrong number of elements for this action: "
                    + events.size());
            }
            FileSystemEvent event = events.remove();

            // //
            // data flow configuration and dataStore name must not be null.
            // //
            if (getConfiguration() == null) {
                LOGGER.log(Level.SEVERE, "Configuration is null.");
                throw new IllegalStateException("Configuration is null.");
            }

            final String configId = getConfiguration().getName();

            // ////////////////////////////////////////////////////////////////////
            //
            // Initializing input variables
            //
            // ////////////////////////////////////////////////////////////////////
            Map props = getConfiguration().getProperties();

            String example0 = props.get("key0");
            listenerForwarder.progressing(10, example0);
            String example1 = props.get("key1");
            listenerForwarder.progressing(20, example1);

//            String inputFileName = event.getSource().getAbsolutePath();
//            final String filePrefix = FilenameUtils.getBaseName(inputFileName);
//            final String fileSuffix = FilenameUtils.getExtension(inputFileName);

            listenerForwarder.setTask("Processing event " + event)

			// DO SOMETHING HERE

            events.add(new FileSystemEvent(new File(example0), FileSystemEventType.FILE_ADDED));

            listenerForwarder.completed();
            return events;
        } catch (Exception t) {
            LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t); // no need to log, we're rethrowing it
            listenerForwarder.failed(t);
            throw new ActionException(this, t.getMessage(), t);
        }
    }

}
