package it.geosolutions.geobatch.nurc.sem.shom;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.metocs.base.NetcdfEvent;
import it.geosolutions.geobatch.octave.tools.Files;

import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.Aggregation;
import ucar.nc2.ncml.AggregationExisting;

public class SHOMAction extends BaseAction<EventObject> implements Action<EventObject>{
    
    
    public SHOMAction(ActionConfiguration actionConfiguration) {
        super(actionConfiguration);
    }

    /**
     * The variable to use to aggregate netcdf files
     */
    private final static String AGGREGATING_VAR ="time";
    
    private final static Logger LOGGER = Logger.getLogger(SHOMAction.class.toString());
    
//    public static void main(String[] args) throws IOException{
//      
//        
////      NetcdfFile ncGridFile = NetcdfFile.open("Y:/data/sem2010/SHOM/WW3-MED-6MIN/forecast/20100920T00Z/aggExisting.ncml");
//      NetcdfDataset dataset = NetcdfDataset.openDataset("Y:/data/sem2010/SHOM/WW3-MED-6MIN/forecast/20100920T00Z/aggExisting.ncml");
//      for (Object obj : dataset.getVariables()) {
//          final Variable var = (Variable) obj;
//          final String varName = var.getName();
//          if (varName.equalsIgnoreCase("time")){
//              
//          }
//      }
//  }

    public Queue<EventObject> execute(Queue<EventObject> events)
            throws ActionException {

        if (LOGGER.isLoggable(Level.INFO))
            LOGGER.info("Starting with processing...");
        
        // looking for file
        if (events.size() != 1)
            throw new IllegalArgumentException("Wrong number of elements for this action: "+ events.size());
        FileSystemMonitorEvent event=(FileSystemMonitorEvent)events.remove();
        try{
            Files.uncompress(event.getSource().getAbsolutePath());
            
            File dir=event.getSource();
//            String[] chld = dir.list();
//            if(chld == null){
//                if (LOGGER.isLoggable(Level.SEVERE))
//                    LOGGER.severe("Failed to list data dir");
//                throw new ActionException(this, "Failed to list data dir");
//            }
//            else {
//                /*
//                 * listing data dir
//                 */
//                for(int i = 0; i < chld.length; i++){
//                    String fileName = chld[i];
//                    
//                }
//            }
            
            NetcdfDataset dataset=new NetcdfDataset();
            Aggregation aggr=new AggregationExisting(dataset, AGGREGATING_VAR, "");
            /*
             * crawlableDatasetElement - defines a CrawlableDataset, or null
dirName - scan this directory
suffix - filter on this suffix (may be null)
regexpPatternString - include if full name matches this regular expression (may be null)
dateFormatMark - create dates from the filename (may be null)
enhanceMode - how should files be enhanced
subdirs - equals "false" if should not descend into subdirectories
olderThan - files must be older than this time (now - lastModified >= olderThan); must be a time unit, may ne bull
             */
            aggr.addDatasetScan(null, dir.getAbsolutePath(), null, null, null, null, "false", null);
            aggr.sync();
            NetcdfEvent ev=new NetcdfEvent(dataset);
            events.add(ev);
            return events;
        }
        catch (IOException ioe){
            throw new ActionException(this, ioe.getLocalizedMessage());
        }
        catch (Exception e){
            throw new ActionException(this, e.getLocalizedMessage());
        }
    }

}
