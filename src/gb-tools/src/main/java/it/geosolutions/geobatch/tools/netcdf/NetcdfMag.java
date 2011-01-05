/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.geosolutions.geobatch.tools.netcdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Section;
import ucar.ma2.Section.Iterator;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;

/**
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * Calculate magnitude for a given vector name using its u and v
 * components.
 * 
 * @todo calculate also direction
 * @warning only magnitude is written (do direction)
 *
 */
public class NetcdfMag {
	
	/*
	 * Dictionary
	 */
	private final static String _NO_DATA="_FillValue";
        // used for no_data representation
        private double noData=NODATA;
        // used for no_data comparison
        private double noDataVal=NODATA;
        
        /*
         * Test Target String   matches()     group(0)        group(1)        group(2)        group(3)
         * 1       vat_vel-v       Yes        vat_vel-v       vat_vel         -v              -v
         * 2       vatvel-u        Yes        vatvel-u        vatvel          -u                      
         * 3       svatvel-v       Yes        svatvel-v       svatvel         -v              -v
         * 4       vatvel-usd-v    Yes        vatvel-usd-v    vatvel-usd      -v              -v
         */
        private final static String regex="(.+)(-u)$";
        
/*
* final String regex="(.+)((-u)|(-v))";
* Test Target String   matches()     group(0)        group(1)        group(2)
* 2       vatvel-u        Yes        vatvel-u        vatvel          -u
* 2       vatvel-usd-v    no
* 3       vatvel-usd-u    Yes        vatvel-usd-v    vatvel-usd      -u
*/
        
        private final static Pattern p=Pattern.compile(regex);
        
	private final static String _UNITS="units";
	private final static String _LONG_NAME="long_name";

//TODO calculate an epsilon
	/**
	 * The epsilon used to compare doubles 
	 */
    private final static double EPS=0.01;
	
	/*
	 * This is only a default value the finally 
	 * used FillValue will be searched into the input
	 * file using the dictionary _NO_DATA term, if
	 * it's not found, this value will be used.
	 */
	private final static Double NODATA=-9999d;
	
	
	private static void usage(){
// TODO apply this output to a logger
System.out.println("USAGE:\n$java NetcdfMag in_out_filename.nc");
	}
	
	/**
	 * @param args an array containing 3 elements:
	 * 1- the file name to read and modify
	 * 2- a string indicating variable to sum (windvel or watvel)
	 * @see usage function
	 */
	public static void main(String[] args) throws Exception {
		
		if (args.length != 1){
			throw new Exception("Wrong arguments!");
		}
		File file=null;
		try {
			if (args[0].startsWith("-")){
				usage();
				return;
			}
			file=new File(args[0]);
			if (!file.exists()){
				throw new FileNotFoundException("Wrong path argument!");
			}
			else{
				
//TODO better check output result >0 ==0 or <0
				// read, calculate and write magnitude
				NetcdfMag magCalculator=new NetcdfMag();
				int res=0;
				if ((res=magCalculator.calculate(file))>0)
System.out.println("DONE");
				else
System.out.println("NOT DONE -> error: "+res);
				
			}
		}
		catch (NullPointerException npe){
                    usage();
System.err.println("NullPointerException ERROR: "+npe.getLocalizedMessage());
npe.printStackTrace();
		}
		catch (FileNotFoundException fnfe){
                    usage();
System.err.println("FileNotFoundException ERROR: "+fnfe.getLocalizedMessage());
fnfe.printStackTrace();
		}
		catch(Exception e){
		    usage();
System.err.println("ERROR: "+e.getLocalizedMessage());		    
		}
		
	}
	
	private boolean scan(java.util.Iterator<Variable> i, NetcdfFileWriteable ncdf) throws Exception{
	    Variable v=null;
	    Variable aVar=null;
            Variable bVar=null;
            String magName=null;
            
	    if (i.hasNext()){ // step
	        v=i.next();
	        if (v==null)
	            throw new Exception("Null object into the list!");
	        
	        Matcher m=p.matcher(v.getName());
                /*
                 *  found the U component
                 *  searching for the V
                 */
                if (m.matches()){
                    if (!ncdf.isDefineMode()){
                        // set redefine file mode
                        ncdf.setRedefineMode(true);
                    }
                    
                    /*
                     * We have to build a magnitude variable basing its contents
                     * and shape on the wind_vel variable so we will use those
                     * variables as shape model
                     */
                    final String varName=m.group(1);
                    aVar=ncdf.findVariable(m.group(0));
                    bVar=ncdf.findVariable(varName+"-v");
                    
                    magName=varName+"-mag";
                    String magLName= varName+" velocity magnitude";

                    Variable magnitudeVar = ncdf.findVariable(magName);

                    if (magnitudeVar==null && aVar != null && bVar != null) {
System.out.println("Found variables to calculate. Named A:"+aVar.getName()+" B:"+bVar.getName());
                        // try to get FillValue/missing_value from the dataset
                        final Attribute noDataAtt=aVar.findAttribute(_NO_DATA);

                        if (noDataAtt!=null){
                                Number num=noDataAtt.getNumericValue();
                                noData=Double.parseDouble(num.toString());
                                noDataVal=num.doubleValue();
//        System.out.println("NODATA: "+noData);
                            }
                            else {
System.err.println("FillValue or missing_data not found!\nUsing "+NODATA);
                        }
                        
                        magnitudeVar=
                            new Variable(ncdf, aVar.getParentGroup(), aVar.getParentStructure(), 
                                        magName, aVar.getDataType(), aVar.getDimensionsString());

                        // setting attributes
                        magnitudeVar.addAttribute(new Attribute(_NO_DATA, noData));
                                
                        /*
                         * try to get the unit from the model
                         * if it is not found no one is set
                         */
                        String unit=null;
                        unit=aVar.getUnitsString();
                        if (unit!=null)
                            magnitudeVar.addAttribute(new Attribute(_UNITS, aVar.getUnitsString()));
                        else {
System.out.println("WARNING: Unable to find unit, will not be used");
                        }
                            
                        magnitudeVar.addAttribute(new Attribute(_LONG_NAME,magLName));

                        ncdf.addVariable(aVar.getParentGroup(),magnitudeVar);
                        ///////////////////////////////////////////////////////////////
                        // recursion
                        ///////////////////////////////////////////////////////////////
                        if (scan(i,ncdf)){
                            /*
                             * 
                             */
                            Array magArray=null;

                            // calculate Array
                            magArray=magnitude(v, aVar, bVar);

                            if (magArray!=null){   
                                try {    
                                    ncdf.write(magName, magArray);
                                }
                                catch (InvalidRangeException e) {
                                    throw e;
                                }        
                            }
                            else {
                                throw new Exception("No Action is performed, magnitude creation returns error");
                            }
                        }
                        else
                            return true;
                        
                    } // if (magnitudeVar==null && aVar != null && bVar != null)
                    else {
                    /*
                     * This file already contain a variable of this type
                     */
    System.out.println("No Action is performed, magnitude "+magName+
            " is already present in this file or"+
            "\nUnable to find vectors variables!");
                        return false;
                    }
                    return true; // add the var
                } // if matches
                else {
                    /*
                     * do not add var 
                     * only apply recursion on the next variable
                     */
                    scan(i, ncdf);
                }
	    }
	    else {
	        /*
	         * If we are here Variables are all added to the 
	         * Netcdf definition so we can set Redefinition to
	         * false
	         */
                // redefine file off
                ncdf.setRedefineMode(false);
                /*
                 * return true to end recursion and starting writing arrays 
                 */
	        return true; // end recursion
	    }
	    
	    return true;

	}
	
	/**
	 * 
	 * @param file
	 * @return
	 */
	private int calculate(File file){
	    NetcdfFileWriteable ncdf=null;
            try{
                ncdf=NetcdfFileWriteable.openExisting(file.getAbsolutePath());
                

                
                List<Variable> varList=ncdf.getVariables();
                java.util.Iterator<Variable> i=varList.iterator();
                // go
                try {
                    scan(i,ncdf);
                } catch (Exception e) {
                   e.printStackTrace();
                }
                
            }
            catch(IOException ioe){
                System.err.println("IOException ERROR: "+ioe.getLocalizedMessage());
                return -4;
            }
            finally{
                try {
                    ncdf.close();
                }
                catch(IOException ioe){
                    System.err.println("IOException ERROR: "+ioe.getLocalizedMessage());
                    return -5;
                }
            }

            return 1;
	}
	
	
	
	
	
	
	/**
	 * @param file an existing netcdf file to open and modify
	 * @param magName the name of the magnitude
	 * @param magLName the long name of the magnitude
	 * @param vectA the name of the vector A
	 * @param vectB the name of the vector B
	 * @return an integer:
	 * 	0  - if no action is performed
	 *  >0  - if action are successful completed
	 *  <0 - if an error occurred
	 * @note vectA and vectB must have the same shape and unit
	 */
	private Array magnitude(Variable magnitudeVar, Variable aVar, Variable bVar){
	    try{
	        Array a = null;
	        Array b = null;
	        Array mag = null;
	        if (magnitudeVar!=null && aVar!=null && bVar!=null){
    	            mag = magnitudeVar.read();
                    a = aVar.read();
                    b = bVar.read();
	        }
	        else {
System.err.println("Problems getting variables");
	            return null;
	        }
	            
		if (a != null && b != null && mag!=null) {    
			            // get shape from the model
		    int [] shape=aVar.getShape();
	        	
	            Section s=new Section(shape);
	            Iterator i=s.getIterator(shape);
			            
	            while(i.hasNext()){
	            	int index=i.next();
	            	double uValue = a.getDouble(index);
	                double vValue = b.getDouble(index);
	                
	                if (Math.abs(uValue-noDataVal)>EPS && Math.abs(vValue-noDataVal)>EPS){
	                	mag.setDouble(index,
	                			Math.sqrt(Math.pow(uValue, 2)+ Math.pow(vValue, 2)));
	                }
	                else {
	                	mag.setDouble(index,noData);
	                }
	            }
	            a = null;
	            b = null;
	            return mag;
		} // a != null && b != null
		else {
System.err.println("Unable to find vectors values!\nNo action is performed");
		    return null;
		 }
            }
            catch(IOException ioe)
            {

            }
            return null;
	}

}
