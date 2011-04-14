/**
 * 
 */
package it.geosolutions.geobatch.action.egeos.emsa;

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.action.scripting.ScriptingAction;
import it.geosolutions.geobatch.action.scripting.ScriptingConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Administrator
 * 
 */
public class EMSAAction extends ScriptingAction implements Action<FileSystemEvent> {

    /**
     * Default Logger
     */
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(EMSAAction.class.toString());

    public EMSAAction(ScriptingConfiguration configuration) throws IOException {
        super(configuration);
    }

}
