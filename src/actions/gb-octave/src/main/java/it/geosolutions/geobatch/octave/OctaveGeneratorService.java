package it.geosolutions.geobatch.octave;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;

public class OctaveGeneratorService
    extends BaseService 
    implements ActionService<FileSystemMonitorEvent, OctaveActionConfiguration> {

    public boolean canCreateAction(final OctaveActionConfiguration configuration) {
        // TODO test if this action can run given the configuration this means:
        // 1 check if the m file is present and is readable
        // 2 I don't remember :)
        
        return true;
    }

    public OctaveAction createAction(final OctaveActionConfiguration configuration) {
        if(canCreateAction(configuration))
            return new OctaveAction(configuration);
        return null;
    }

}
