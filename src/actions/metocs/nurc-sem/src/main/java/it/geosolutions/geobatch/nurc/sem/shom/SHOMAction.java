package it.geosolutions.geobatch.nurc.sem.shom;

import java.io.IOException;

import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

public class SHOMAction {
    public static void main(String[] args) throws IOException{
        
//      NetcdfFile ncGridFile = NetcdfFile.open("Y:/data/sem2010/SHOM/WW3-MED-6MIN/forecast/20100920T00Z/aggExisting.ncml");
      NetcdfDataset dataset = NetcdfDataset.openDataset("Y:/data/sem2010/SHOM/WW3-MED-6MIN/forecast/20100920T00Z/aggExisting.ncml");
      for (Object obj : dataset.getVariables()) {
          final Variable var = (Variable) obj;
          final String varName = var.getName();
          if (varName.equalsIgnoreCase("time")){
              
          }
      }
      
      
  }

}
