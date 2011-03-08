package it.geosolutions.geobatch.nurc.sem.rep10.shom;

import org.junit.Test;

/**
 * TODO
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class SHOMAggregationTest {
  
  /**
   * TODO
   */
  @Test
  public void test(){
      /*
      static String data_name="src/test/resources/data/SHOM_WW3-MENOR-4000M_20100919T00Z.zip";
      File data= null;
      NetcdfDataset dataset=null;
      try {
          NetcdfDataset.initNetcdfFileCache(100,200,15*60); // on application startup
          dataset = new NetcdfDataset();
          
          Aggregation aggr=new AggregationExisting(dataset, "time", "days");
          
          data=new File(data_name);
          String outdirName=Extract.extract(data.getAbsolutePath());
          
          Aggregation.setPersistenceCache(new DiskCache2(outdirName, false, 1, -1));
          
          data=new File(outdirName);
          Collector c=new Collector(new WildcardFileFilter("*.nc"));
          List<File> files=c.collect(data);
          Group root=dataset.getRootGroup();
          System.out.println("Aggregating files:");
          for (File f:files){
              System.out.println("Aggregating: "+f.getAbsolutePath());
              NetcdfFile ncf = null;
              try {
                  ncf = NetcdfDataset.open(f.getAbsolutePath());
                  
                  dataset.addGroup(root,ncf.getRootGroup());
                  
                  for (Variable v:ncf.getVariables()){
                      dataset.addVariable(ncf.getRootGroup(), new VariableDS(null,v,true));
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
      catch (Throwable t){
          Assert.fail("FAIL: "+t.getLocalizedMessage());
      }
      finally{
          if (data!=null){
              if (data.exists())
                  FileUtils.deleteQuietly(data);
          }
          if (dataset!=null)
              dataset.close();
              
      }    
*/
}
}
