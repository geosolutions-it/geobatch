package it.geosolutions.geobatch.nurc.sem.rep10.mars3d;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.impl.BaseService;
import it.geosolutions.geobatch.flow.event.action.ActionService;

public class MARS3DGeneratorService
    extends BaseService 
    implements ActionService<FileSystemMonitorEvent, MARS3DActionConfiguration> {

    public boolean canCreateAction(final MARS3DActionConfiguration configuration) {
        // TODO test if this action can run given the configuration this means:
        // 1 check if the m file is present and is readable
        // 2 I don't remember :)
        
        return true;
    }

    public MARS3DAction createAction(final MARS3DActionConfiguration configuration) {
        if(canCreateAction(configuration))
            return new MARS3DAction(configuration);
        return null;
    }

}
