/**
 * 
 */
package it.geosolutions.geobatch.action.egeos.emsa;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.action.scripting.GroovyAction;
import it.geosolutions.geobatch.action.scripting.ScriptingConfiguration;
import it.geosolutions.geobatch.action.splitting.SplittingAction;
import it.geosolutions.geobatch.action.splitting.SplittingConfiguration;
import it.geosolutions.geobatch.action.splitting.SplittingService;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Administrator
 *
 */
public class EMSAAction extends GroovyAction implements Action<FileSystemMonitorEvent> {

    /**
     * Default Logger
     */
    @SuppressWarnings("unused")
    private final static Logger LOGGER = Logger.getLogger(EMSAAction.class.toString());
    
    public EMSAAction(ScriptingConfiguration configuration) throws IOException {
        super(configuration);
        
        SplittingService splittingService = new SplittingService();
        
        SplittingConfiguration splittingConfiguration = new SplittingConfiguration();
        List<String> serviceIDs = new LinkedList<String>();
        serviceIDs.add("pippo");
        splittingConfiguration.setServiceIDs(serviceIDs);
        
        SplittingAction splittingAction = splittingService.createAction(splittingConfiguration);
        Queue<FileSystemMonitorEvent> events = new LinkedList<FileSystemMonitorEvent>();
        events.add(new FileSystemMonitorEvent(new File("G:/tmp/pippo"), FileSystemMonitorNotifications.FILE_ADDED));
        try {
            splittingAction.execute(events);
        } catch (ActionException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IOException(e);
        }
    }

}
