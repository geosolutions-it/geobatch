package it.geosolutions.geobatch.egeos.deployers.actions;

/**
 * 
 */

import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.geobatch.action.scripting.ScriptingAction;
import it.geosolutions.geobatch.flow.event.action.Action;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Administrator
 * 
 */
public class EGEOSDeployerBaseAction extends ScriptingAction implements Action<FileSystemEvent> {

    /**
     * Default Logger
     */
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(EGEOSDeployerBaseAction.class.toString());

    public EGEOSDeployerBaseAction(EGEOSBaseDeployerConfiguration configuration) throws IOException {
        super(configuration);
    }

}
