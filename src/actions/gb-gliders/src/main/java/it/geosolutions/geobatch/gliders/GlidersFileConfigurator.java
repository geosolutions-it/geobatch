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



package it.geosolutions.geobatch.gliders;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.gliders.configuration.GlidersActionConfiguration;
import it.geosolutions.geobatch.gliders.configuration.GlidersConfiguratorAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.postgresql.Driver;

import ucar.ma2.Array;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.simplify.DouglasPeuckerLineSimplifier;


/**
 * 
 * Public class to insert Gliders netcdf data file (gliders measurements) into DB 
 *  
 */
public class GlidersFileConfigurator extends
        GlidersConfiguratorAction<FileSystemMonitorEvent>{
	
	// //////////////////////////
	// JDBC data fields  
	// //////////////////////////
    
    private Connection conTarget = null;
    private boolean isConnected = false;   
    

    protected GlidersFileConfigurator(GlidersActionConfiguration configuration)
            throws IOException {
        super(configuration);
    }

	public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws Exception {

        try {
        	
        	// ///////////////////////////////////
            // Initializing input variables
            // ///////////////////////////////////
        	
            if (configuration == null) {
                LOGGER.log(Level.SEVERE, "ActionConfig is null.");
                throw new IllegalStateException("ActionConfig is null.");
            }

            // ///////////////////////////////////
            // Initializing input variables
            // ///////////////////////////////////
            
            final File workingDir = IOUtils.findLocation(configuration.getWorkingDirectory(),
                    new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));

            // ///////////////////////////////////
            // Checking input files.
            // ///////////////////////////////////
            
            if (workingDir == null) {
                LOGGER.log(Level.SEVERE, "Working directory is null.");
                throw new IllegalStateException("Working directory is null.");
            }

            if ( !workingDir.exists() || !workingDir.isDirectory()) {
                LOGGER.log(Level.SEVERE, "Working directory does not exist ("+workingDir.getAbsolutePath()+").");
                throw new IllegalStateException("Working directory does not exist ("+workingDir.getAbsolutePath()+").");
            }

			File[] netcdfList;
			netcdfList = handleNetCDFfile(events);

			if(netcdfList == null)
				throw new Exception("Error while processing the netcdf file set");
			
			// /////////////////////////////////////////////
			// Look for the main netcdf file in the set
			// /////////////////////////////////////////////
			
			File netcdfFile = null;
			for (File file : netcdfList) {
				if(FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("nc")) {
					netcdfFile = file;
					break;
				}
			}

			if(netcdfFile == null) {
                LOGGER.log(Level.SEVERE, "netcdf file not found in fileset.");
                throw new IllegalStateException("netcdf file not found in fileset.");
			}

			NetcdfDataset dataset = NetcdfDataset.openDataset((netcdfFile).getPath()); 
			
			List<Variable> list_variables = dataset.getVariables();
			String[] variables_name = new String[list_variables.size()];
			
			ListIterator<Variable> iterator = list_variables.listIterator();
			for(int j=0;list_variables.listIterator().hasNext() && j<list_variables.size(); j++){
				Variable var = (Variable)iterator.next();
				variables_name[j] = var.getName();
			}
 		
        	// ///////////////////////
        	// Inserting data 
        	// ///////////////////////
        	
            initJDBCConnection();
            
	        if (isConnected()) {
                insertData(dataset, variables_name);
                closeConnections();
	        }

        	return events;        	
        	
        } catch (Throwable t) {
			LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        } finally {
			cleanup();
		}
    }

	private void cleanup() {
		try{
	        if (isConnected) {
	        	if(conTarget != null && !conTarget.isClosed()){
	                conTarget.close();
	                conTarget = null;                
	        	}
	        	
	        	isConnected = false;
	        }
		}catch(SQLException e){
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage());
		}  
	}

	/**
	 * Pack the files received in the events into an array.
	 * 
	 *
	 * @param events The received event queue
	 * @return
	 */
	private File[] handleNetCDFfile(Queue<FileSystemMonitorEvent> events) {
		File ret[] = new File[events.size()];
		int idx = 0;
		for (FileSystemMonitorEvent event : events) {
			ret[idx++] = event.getSource();
		}
		return ret;
	}
	    
	/**
	 * Utility function to read variables from NetCDF file
	 * 
	 *  @param dataset The NetCDF dataset 
	 *  @param name The variable name to read 
	 * 
	 * 	@return array
	 */
    private static Array readVariables(final NetcdfDataset dataset, final String name)throws IOException{
    	Array array = null;
    	
    	try{
        	Variable v = dataset.findVariable(name);
        	
        	if(v != null)
        		array = v.read(); 
        	
        	return array;
        	
    	}catch(IOException exc){
    		throw new IOException("EXCEPTION -> " + exc.getLocalizedMessage());
    	}    	
    }
    
    /**
     * @throws SQLException 
     * 
     */
    private void closeConnections() throws SQLException {
        if (isConnected) {
        	if(conTarget != null && !conTarget.isClosed()){
        		conTarget.commit();
                conTarget.close();
                conTarget = null;                
        	}
        	
        	isConnected = false;
        }
    }

    /**
     * @throws SQLException 
     * 
     */
    private void initJDBCConnection() throws SQLException {
        DriverManager.registerDriver(new Driver());
        
        // /////////////////////////////
        // Connecting to the DataBase
        // /////////////////////////////

        StringBuffer conString = new StringBuffer("jdbc:postgresql://");
        conString.append(this.getConfiguration().getDbServerIp()).append(":").append(this.getConfiguration()
        		.getDbPort()).append("/").append(this.getConfiguration().getDbName());
        conTarget = DriverManager.getConnection(conString.toString(), this.getConfiguration()
        		.getDbUID(), this.getConfiguration().getDbPWD());

        isConnected = true;
    }

    /**
     * @return the isConnected
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Insert function to insert the data into MEASUREMENT and MEASUREMENT_VALUES tables
     * 
     * @params input The NetCDF file
     * @throws SQLException 
     * 
     */
    private void insertData(final NetcdfDataset dataset, final String[] variables_name) throws SQLException {
    	
    	dataset.sort();
    	
		// ///////////////////////////////////////////
		// Managing JDBC-PostGIS Connection 
		// ///////////////////////////////////////////
    	
    	PreparedStatement stat = null;        	
	    ResultSet rs_glider_ms = null;
	    
        try{        	
	    	Map<String, Array> map_variables = new HashMap<String, Array>();
        	
        	for(int c=0; c<variables_name.length; c++){
        		if(variables_name[c].equalsIgnoreCase("ptime") || variables_name[c].equalsIgnoreCase("lon") 
        				|| variables_name[c].equalsIgnoreCase("lat")) continue;
        		
        		Array array = readVariables(dataset, variables_name[c]);
        		map_variables.put(variables_name[c], array);
        	}
	    	
        	Array pTime, lonValues, latValues;	              	
        	pTime = readVariables(dataset, "ptime");
        	
        	conTarget.setAutoCommit(false);
        	
            if(pTime == null)throw new IOException("EXCEPTION -> Missing ptime variable!");
            else{

            	lonValues = readVariables(dataset, "lon");
            	latValues = readVariables(dataset, "lat");
            	
            	if(lonValues == null || latValues == null)throw new IOException("EXCEPTION -> Missing lon or lat variable!");
            	else{
                	Attribute platform_code = dataset.findGlobalAttribute("platform_code");
                	
                	Long size = pTime.getSize();
                	
                	Point[] positions = new Point[size.intValue()];
                	GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
                	
                	for(int k=0; k<positions.length; k++){     		
                		
                		WKTReader reader = new WKTReader( geometryFactory );
                		Point point = (Point) reader.read("POINT("+ lonValues.getDouble(lonValues.getIndex().set(k)) + " " + latValues.getDouble(latValues.getIndex().set(k)) + " 0)");
                		point.setSRID(4326);     		
                		
                		positions[k] = point;
                	} 

                	Coordinate[] coordinate = new Coordinate[positions.length];
                	for(int l=0; l<positions.length; l++)            			
             			coordinate[l] = new Coordinate (positions[l].getCoordinate());

                	coordinate = DouglasPeuckerLineSimplifier.simplify(coordinate, this.getConfiguration().getSimplyTollerance());

                	CoordinateSequence sequence = geometryFactory.getCoordinateSequenceFactory().create(coordinate);
                	LineString course = new LineString(sequence, geometryFactory);                	

    	          	String sqlString = "insert into mission(cruise_id,start_date,end_date,ext_name,the_geom) " +
    	          			"values(?,?,?,?," + "geometryFromText('" + course.toText() + "', 4326) " + ")";
    	          	stat = conTarget.prepareStatement(sqlString);
	
    	          	stat.setLong(1, 157);

    	          	Date start_date = new Date(pTime.getLong(pTime.getIndex().set(0))*1000);
    	          	stat.setDate(2, start_date);    	      	          	
    	          	Date end_date = new Date(pTime.getLong(pTime.getIndex().set(positions.length - 1))*1000);
    	          	stat.setDate(3, end_date);
    	          	
    	          	stat.setString(4, "BP09-" + new Timestamp(start_date.getTime()));
    	          	
    	          	stat.execute();  
    	          	stat.close();
            	     
            	    int mission_id_max = 0;           	                	    
                	
                	sqlString = "SELECT MAX(mission_id) FROM mission";
                  	stat = conTarget.prepareStatement(sqlString);
                  	rs_glider_ms = stat.executeQuery();
                    if(rs_glider_ms.next())	mission_id_max = rs_glider_ms.getInt(1);
                    
                    rs_glider_ms.close();
                    stat.close();

                	for(int l=0; l<size.intValue(); l++){
//                		Double lat = latValues.getDouble(latValues.getIndex().set(l));
//                		Double lon = lonValues.getDouble(lonValues.getIndex().set(l));
                		
//                		sqlString = "insert into observation(ship_id,mission_id,type_id,cruise_id,sens_id,ext_name,obs_date,obs_time,lat,lon,the_geom) " +
//        					"values(?,?,?,?,?,?,?,?,?,?,"+ "geometryFromText('POINT(" + positions[l].getX() + " " + positions[l].getY() + ")', 4326) " + ")";
//                		sqlString = "insert into observation(ship_id,mission_id,type_id,cruise_id,sens_id,ext_name,obs_date,obs_time,lat,lon,the_geom) " +
//                				"values(?,?,?,?,?,?,?,?,?,?,"+ "geometryFromText('POINT(" + lon.doubleValue() + " " + lat.doubleValue() + ")', 4326) " + ")";
                		sqlString = "insert into observation(ship_id,mission_id,type_id,cruise_id,sens_id,ext_name,obs_date,obs_time,lat,lon,the_geom) " +
        					"values(?,?,?,?,?,?,?,?,?,?,"+ "geometryFromText('" + positions[l].toText() + "', 4326) " + ")";
                		stat = conTarget.prepareStatement(sqlString);
        	          	
        	          	stat.setLong(1, 1);
        	          	stat.setLong(2, mission_id_max);
        	          	stat.setLong(3, 2);
        	          	stat.setLong(4, 157);
        	          	stat.setLong(5, 0);
        	          	
        	    		if(platform_code != null)
        	    			stat.setString(6, platform_code.getStringValue());
        	    		else
        	    			stat.setString(6, "Not Available");
                		
        	        	Date date = new Date(pTime.getLong(pTime.getIndex().set(l))*1000);
        	        	Time time = new Time(pTime.getLong(pTime.getIndex().set(l))*1000);
        	        	
        	        	stat.setDate(7, date);
        	        	stat.setString(8, time.toString());
        	        	stat.setDouble(9, positions[l].getY());
        	        	stat.setDouble(10, positions[l].getX());
        	        	
        	          	stat.execute();  
        	          	stat.close();
                	}

                	int obs_id_min = 0;
                	int obs_id_max = 0;     	

                	sqlString = "SELECT MAX(obs_id) FROM observation";
                  	stat = conTarget.prepareStatement(sqlString);
                  	rs_glider_ms = stat.executeQuery();
                    if(rs_glider_ms.next())	obs_id_max = rs_glider_ms.getInt(1);
                    rs_glider_ms.close();
                    stat.close();
                    
                    obs_id_min = obs_id_max - size.intValue();
                    obs_id_min++;

        	    	for(int i=obs_id_min, j=0; i<=obs_id_max && j<size; j++, i++){	     	
        	    		StringBuffer zPos = new StringBuffer();	     	    		
        	    		
        	    		Double depth_value = null;
        	    		if(map_variables.containsKey("depth")){
        	    			depth_value = map_variables.get("depth").getDouble(map_variables.get("depth").getIndex().set(j));
        	    			
            	    		if(depth_value.isNaN())
            	    			zPos.append(0.0);
            	    		else
            	    			zPos.append(map_variables.get("depth").getDouble(map_variables.get("depth").getIndex().set(j)));  
        	    		}else{
        	    			LOGGER.log(Level.INFO, "Missing depth variable!");
        	    			
        	    			depth_value = Double.NaN;
        	    			zPos.append(0.0);
        	    		}
        	        	
        	        	Date date = new Date(pTime.getLong(pTime.getIndex().set(j))*1000);
        	        	
        	          	sqlString = "insert into measurement(zpos,tpos,obs_id,depth) values(ARRAY[" + zPos.toString() + "],?,?,?)";
        	          	stat = conTarget.prepareStatement(sqlString);
        	          	stat.setTimestamp(1, new Timestamp(date.getTime()));
        	          	stat.setLong(2, i);
        	          	
        	          	if(depth_value.isNaN())	
        	          		stat.setDouble(3, 0.0);
        	          	else
        	          		stat.setDouble(3, depth_value.doubleValue()*-1);
        	          	
        	          	stat.execute();  
        	          	stat.close();
                	}	
        	    	
                	int measurement_id_min = 0;
                	int measurement_id_max = 0;
                    
                  	sqlString = "SELECT MAX(measurement_id) FROM measurement";
                  	stat = conTarget.prepareStatement(sqlString);
                  	rs_glider_ms = stat.executeQuery();
                    if(rs_glider_ms.next())	measurement_id_max = rs_glider_ms.getInt(1);
                    rs_glider_ms.close();
                    stat.close();	          

                    measurement_id_min = measurement_id_max - size.intValue();
                    measurement_id_min++;
                    
                    ArrayList<Long> param_id = new ArrayList<Long>();
                    
                    Map<Long, Array> check_var = new HashMap<Long, Array>();
                    
                    for(int k=0; k<variables_name.length; k++){
                		if(variables_name[k].equalsIgnoreCase("ptime") || variables_name[k].equalsIgnoreCase("lon") 
                				|| variables_name[k].equalsIgnoreCase("lat") || variables_name[k].equalsIgnoreCase("depth")) continue;

                      	sqlString = "select * from parameter where default_label='" + variables_name[k] + "'";
                      	
                      	stat = conTarget.prepareStatement(sqlString);
                      	rs_glider_ms = stat.executeQuery();
                      	
                        if(rs_glider_ms.next()){
                        	check_var.put(rs_glider_ms.getLong("param_id"), map_variables.get(variables_name[k]));
                        	param_id.add(rs_glider_ms.getLong("param_id"));
                        }else{
                        	LOGGER.log(Level.INFO, "The " + variables_name[k] + " variable won't be inserted " +
                        			"because the data base is not properly configured");
                        }                    	
                        
                        rs_glider_ms.close();
                        stat.close();
                    }
                    
            		for(int y=measurement_id_min, h=0; y<=measurement_id_max; y++, h++){            		    
            			for(int p=0; p<param_id.size(); p++){       
            				if(check_var.containsKey(param_id.get(p))){
            					Double mValue;
            					
            					if(param_id.get(p) == 3)
            						mValue = check_var.get(param_id.get(p)).getDouble(check_var.get(param_id.get(p)).getIndex().set(h))*100000;
            					else
            						mValue = check_var.get(param_id.get(p)).getDouble(check_var.get(param_id.get(p)).getIndex().set(h));
            					
                	            sqlString = "insert into measurement_values(measurement_id,values,param_id) values(" + 
                	            	y + ", ARRAY['" + mValue.doubleValue() + "']," + param_id.get(p) + ")";   
                	            
                	          	stat = conTarget.prepareStatement(sqlString);
                	          	stat.execute();
                	          	stat.close();
            				}
            			}
            		}
            	}	
            }
		}catch(Exception exc){
			throw new SQLException("EXCEPTION -> " + exc.getLocalizedMessage());
    	}finally{
	        try{
	        	if(rs_glider_ms != null){
	        		rs_glider_ms.close();
	        		rs_glider_ms = null;
        		}
        		
        		if(stat != null){
        			stat.close();
        			stat = null;
        		}
        		
            }catch(SQLException exc){
        		throw new SQLException("EXCEPTION -> " + exc.getLocalizedMessage());
            }
	 	}
    }
}
