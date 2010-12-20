package it.geosolutions.geobatch.nurc.sem.rep10.shom;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.ncml.Aggregation;
import ucar.nc2.ncml.Aggregation.Dataset;
import ucar.nc2.ncml.AggregationExisting;
import ucar.nc2.ncml.DatasetCollectionManager;
import ucar.nc2.ncml.Scanner;
import ucar.nc2.util.CancelTask;
import ucar.nc2.util.DiskCache2;

public class SHOMAggregationTest {
    
  static String dir_name="/home/carlo/work/data/SHOMworkingdir/DATA/SHOM_MENOR4000_FORECAST_20100920T00Z/";

  public static void main(String[] args) throws IOException{
      
      NetcdfDataset dataset=null;
      try {
          NetcdfDataset.initNetcdfFileCache(100,200,15*60); // on application startup
          dataset = new NetcdfDataset();
          
          Aggregation aggr=new AggregationExisting(dataset, "time", "days");
          
          Aggregation.setPersistenceCache(new DiskCache2(dir_name, false, 1, -1));
          
          dataset.setAggregation(aggr);
          
          
          File dir=new File(dir_name);
          String []files=dir.list();
          Group root=dataset.getRootGroup();
          System.out.println("Aggregating files:");
          for (String f:files){
              System.out.println("Aggregating: "+f);
              NetcdfFile ncf = null;
              try {
                  ncf = NetcdfDataset.open(dir_name+f);
                  
                  dataset.addGroup(ncf.getRootGroup(),root);
                  
                  for (Variable v:ncf.getVariables()){
                      dataset.addVariable(root, new VariableDS(null,v,true));
                  }
                  
//                  aggr.addDataset(new CrawlableDatasetFile(ncfile));
                  
              } finally {
                  if (ncf!=null)
                      ncf.close();
              }
          }
          
//          aggr.addDatasetScan(null, dir.getAbsolutePath(), null, null, null, null, "false", null);
          
  // IMPORTANT!!!!
          aggr.persistWrite();
          
          aggr.sync();
          
          
          System.out.println("AG_TITLE: "+dataset.getTitle());
          
          for (Variable var : root.getVariables()) {
              System.out.println("AG_var: "+var.getName());
              System.out.println("AG_dimensions: "+var.getDimensionsString());
          }
      }
      finally{
          if (dataset!=null)
              dataset.close();
      }
}
}
