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



package it.geosolutions.geobatch.ctd;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.ctd.configuration.CTDActionConfiguration;
import it.geosolutions.geobatch.ctd.configuration.CTDConfiguratorAction;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.postgresql.Driver;


/**
 * 
 * Public class to insert CTD data file (CTD measurements) into DB 
 * 
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 *  
 */
public class CTDFileConfigurator extends
        CTDConfiguratorAction<FileSystemMonitorEvent>{
	
	// //////////////////////////
	// JDBC data fields  
	// //////////////////////////
    
    private Connection conTarget = null;
    private boolean isConnected = false;   
    

    protected CTDFileConfigurator(CTDActionConfiguration configuration)
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

			File[] ctdfList;
			ctdfList = handleCTDfile(events);

			if(ctdfList == null)
				throw new Exception("Error while processing the netcdf file set");
			
			// /////////////////////////////////////////////
			// Look for the main netcdf file in the set
			// /////////////////////////////////////////////
			
			File ctdFile = null;
			for (File file : ctdfList) {
				if(FilenameUtils.getExtension(file.getName()).equalsIgnoreCase("dat")) {
					ctdFile = file;
					break;
				}
			}

			if(ctdFile == null) {
                LOGGER.log(Level.SEVERE, "netcdf file not found in fileset.");
                throw new IllegalStateException("netcdf file not found in fileset.");
			}

			// ///////////////////////
        	// Inserting data 
        	// ///////////////////////
        	
            initJDBCConnection();
            
	        if (isConnected()) {
                insertCTDdata(ctdFile);
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
	private File[] handleCTDfile(Queue<FileSystemMonitorEvent> events) {
		File ret[] = new File[events.size()];
		int idx = 0;
		for (FileSystemMonitorEvent event : events) {
			ret[idx++] = event.getSource();
		}
		return ret;
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
     * 
     */
    private void insertCTDdata(File ctdFile) throws Exception{
        
    	PreparedStatement psTmp = null;
    	PreparedStatement ps = null;
    	
    	ResultSet rs = null;
    	
        try {
        	
            // //////////////////////////////////////////////////////////
            //
            // Parsing file structure...
            //
            // //////////////////////////////////////////////////////////
        	
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(ctdFile)));
            SimpleDateFormat sf = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
            
            String line = "";
            String shipName = "";
            String cruiseName = "";
            String stationName = "";
            String startDate = "";
            String startTime = "";
            Double latitude = null;
            Double longitude = null;
            String waterDepth = "";
            
            while (!(line = br.readLine().trim()).equalsIgnoreCase("%")) {
            	
                if (line.lastIndexOf("Ship") > 0) {
                    shipName = line.substring(line.indexOf(":") + 2).trim();
                } else if (line.lastIndexOf("Cruise") > 0) {
                    cruiseName = line.substring(line.indexOf(":") + 2).trim();
                }else if (line.lastIndexOf("File Name") > 0) {
                    stationName = line.substring(line.indexOf(":") + 2).trim();
                    
                    if(stationName.indexOf(".HEX") != -1)
                    	stationName = stationName.replaceAll(".HEX", "");
                    
                    if(stationName.indexOf("CTD") != -1)
                    	stationName = stationName.replaceAll("CTD", "CTD_");
                    
                } else if (line.lastIndexOf("Start Date") > 0
                        || line.lastIndexOf("Date") > 0) {
                	
                    startDate = line.substring(line.indexOf(":") + 2).trim();
                    
                } else if (line.lastIndexOf("Start Time") > 0
                        || line.lastIndexOf("Time") > 0) {
                	
                    startTime = line.substring(line.indexOf(":") + 2).trim();                    
                    startTime = startTime.substring(0, 5);
                    
                } else if (line.lastIndexOf("Latitude") > 0) {
                	
                    final String[] lat = line.substring(line.indexOf(":") + 2).trim().split(" ");
                    
                    if (lat[2].equalsIgnoreCase("S")) {
                        latitude = new Double(0.0
                                - Double.parseDouble(lat[0])
                                - Double.parseDouble(lat[1]) / 60.0);
                    } else if (lat[2].equalsIgnoreCase("N")) {
                        latitude = new Double(Double
                                .parseDouble(lat[0])
                                + Double.parseDouble(lat[1]) / 60.0);
                    }
                    
                } else if (line.lastIndexOf("Longitude") > 0) {
                    final String[] lon = line.substring(line.indexOf(":") + 2).trim().split(" ");
                    
                    if (lon.length >= 3 && lon[2].equalsIgnoreCase("W")) {
                        longitude = new Double(0.0
                                - Double.parseDouble(lon[0])
                                - Double.parseDouble(lon[1]) / 60.0);
                    }else{
                        longitude = new Double(Double.parseDouble(lon[0])
                                + Double.parseDouble(lon[1]) / 60.0);
                    }
                    
                } else if (line.lastIndexOf("Water Depth") > 0
                        || line.lastIndexOf("Depth") > 0) {
                	
                    try {
                        waterDepth = line.substring(
                                line.indexOf(":") + 2).trim();
                    } catch (StringIndexOutOfBoundsException e) {
                        waterDepth = "0";
                    }
                }
            }

            final String[] paramStrings = br.readLine()
                    .substring(1).trim().split(" ");
            final String[] unitStrings = br.readLine().substring(1)
                    .trim().split(" ");
            
            /* Stirng */
            final ArrayList<String> params = new ArrayList<String>();
            
            /* Stirng */
            final ArrayList<String> units = new ArrayList<String>();

            int len = paramStrings.length;
            for (int p = 0; p < len; p++) {
                if (paramStrings[p].length() > 0
                        && !paramStrings[p].equalsIgnoreCase(" "))
                    params.add(paramStrings[p].trim());
            }

            len = unitStrings.length;
            for (int u = 0; u < len; u++) {
                if (unitStrings[u].length() > 0
                        && !unitStrings[u].equalsIgnoreCase(" "))
                    units.add(unitStrings[u].substring(1,
                            unitStrings[u].lastIndexOf(')')));
            }

            br.readLine();
            
            /* Integer zpos, ArrayList values */            
            final Map<Integer, ArrayList<Double>> paramValues = new HashMap<Integer, ArrayList<Double>>();
            line = "";
            int zpos = 0;
            
            while ((line = br.readLine()) != null) {
                final String[] valueStrings = line.trim().split(" ");
                
                /* Double */
                final ArrayList<Double> values = new ArrayList<Double>();
                
                len = valueStrings.length;
                if (line.trim().length() > 0) {
                    for (int v = 0; v < len; v++) {
                        if (valueStrings[v].length() > 0
                                && !valueStrings[v]
                                        .equalsIgnoreCase(" ")) {
                            values.add(valueStrings[v].equalsIgnoreCase("nan") ? -9999.0 : Double.valueOf(valueStrings[v]));
                        }
                    }

                    paramValues.put(new Integer(zpos), values);
                    zpos++;
                }
            }
            
            // ////////////////////////////////////////////////////////////////////////////
            //
            // Inserting info into DB ...
            //
            // ////////////////////////////////////////////////////////////////////////////
            /**
             * Step 1: checking if exists Observation Type, Ship,
             * Cruise and retrieve the IDs.
             */
            long type_id = -1;
            long ship_id = -1;
            long cruise_id = -1;
            long mission_id = -1;

            /** OBSERVATION TYPE **/
            
            String sqlString = "select * from observation_type where name = ? ";
            conTarget.setAutoCommit(false);

            ps = conTarget.prepareStatement(sqlString);
            ps.setString(1, "CTD/XBT");
            rs = ps.executeQuery();

            if (!rs.next()) {
                    sqlString = "insert into observation_type(name) values(?)";
                    psTmp = conTarget.prepareStatement(sqlString);
                    psTmp.setString(1, "CTD/XBT");
                    psTmp.execute();
                    psTmp.close();

                    sqlString = "select * from observation_type where name = ? ";
                    psTmp = conTarget.prepareStatement(sqlString);
                    psTmp.setString(1, "CTD/XBT");
                    rs = psTmp.executeQuery();
                    
                    if (rs.next()){
                        type_id = rs.getLong("obs_type_id");
                   		if(psTmp != null)psTmp.close();
                    }else{
                        LOGGER.log(Level.SEVERE,
                                new StringBuffer(
                                        "Can't retrieve OBS_TYPE_ID for observation_type ")
                                        .append(shipName)
                                        .append(". Aborting ingestion... file ")
                                        .append(ctdFile.getName())
                                        .append(" not ingested!")
                                        .toString());
                        
                        if(psTmp != null)psTmp.close();
                        throw new Exception(new StringBuffer(
	                        "Can't retrieve OBS_TYPE_ID for observation_type ")
	                        .append(shipName)
	                        .append(". Aborting ingestion... file ")
	                        .append(ctdFile.getName())
	                        .append(" not ingested!")
	                        .toString());                                      
                    }
                    
            } else {
                type_id = rs.getLong("obs_type_id");
            }

            /** CRUISE **/
            
        	if(rs != null){rs.close(); rs = null;}
        	if(ps != null){ps.close(); ps = null;}
            
            sqlString = "select * from cruise where LOWER(ext_name) = ? ";
            ps = conTarget.prepareStatement(sqlString);
            ps.setString(1, cruiseName.toLowerCase());
            rs = ps.executeQuery();
            
            if (!rs.next()) {
            	
                sqlString = "insert into cruise(sic_id, start_date, end_date, ext_name, description) values(?,?,?,?,?)";
                psTmp = conTarget.prepareStatement(sqlString);
                psTmp.setLong(1, 31);
	  	        
                Calendar start_calendar = Calendar.getInstance();
                start_calendar.set(2008, 10, 11);
                
                Calendar end_calendar = Calendar.getInstance();
                end_calendar.set(2008, 11, 11);

                psTmp.setDate(2, new Date(start_calendar.getTimeInMillis()));
                psTmp.setDate(3, new Date(end_calendar.getTimeInMillis()));
                psTmp.setString(4, cruiseName);
                psTmp.setString(5, "");

                psTmp.execute();
            	if(psTmp != null){psTmp.close(); psTmp = null;}


                sqlString = "select * from cruise where ext_name = ? ";
                psTmp = conTarget.prepareStatement(sqlString);
                psTmp.setString(1, cruiseName);
                rs = psTmp.executeQuery();
                
                if (rs.next()){
                	cruise_id = rs.getLong("cruise_id");
                	if(psTmp != null){psTmp.close(); psTmp = null;}
                }else{
                    LOGGER.log(Level.SEVERE,
                    		new StringBuffer(
	                            "Can't retrieve CRUISE_ID for cruise ")
	                            .append(cruiseName)
	                            .append(". Aborting ingestion... file ")
	                            .append(ctdFile.getName())
	                            .append(" not ingested!")
	                            .toString());
                    
                	if(psTmp != null){psTmp.close(); psTmp = null;}

                    throw new Exception(new StringBuffer(
	                    "Can't retrieve CRUISE_ID for cruise ")
	                    .append(cruiseName)
	                    .append(". Aborting ingestion... file ")
	                    .append(ctdFile.getName())
	                    .append(" not ingested!")
	                    .toString());                                    
                }
                                    
            } else {
                cruise_id = rs.getLong("cruise_id");
            }

            /** MISSION **/

        	if(rs != null){rs.close(); rs = null;}
        	if(ps != null){ps.close(); ps = null;}
            
            sqlString = "select * from mission where LOWER(ext_name) = ? ";
            ps = conTarget.prepareStatement(sqlString);
            ps.setString(1, cruiseName.toLowerCase().concat("-2008-10-11"));
            rs = ps.executeQuery();
            
            if (!rs.next()) {            	
                sqlString = "insert into mission(cruise_id,start_date,end_date,ext_name) values(?,?,?,?)";
                psTmp = conTarget.prepareStatement(sqlString);
                psTmp.setLong(1, cruise_id);
                
                Calendar start_calendar = Calendar.getInstance();
                start_calendar.set(2008, 10, 11);
                
                Calendar end_calendar = Calendar.getInstance();
                end_calendar.set(2008, 11, 11);

                psTmp.setDate(2, new Date(start_calendar.getTimeInMillis()));
                psTmp.setDate(3, new Date(end_calendar.getTimeInMillis()));

                psTmp.setString(4, cruiseName.toLowerCase().concat("-2008-10-11"));

                psTmp.execute();
            	if(psTmp != null){psTmp.close(); psTmp = null;}

                sqlString = "select * from mission where ext_name = ? ";
                psTmp = conTarget.prepareStatement(sqlString);
                psTmp.setString(1, cruiseName.toLowerCase().concat("-2008-10-11"));
                rs = psTmp.executeQuery();
                
                if (rs.next()){
                	mission_id = rs.getLong("mission_id");
                	if(psTmp != null){psTmp.close(); psTmp = null;}
	            }else{
	                LOGGER.log(Level.SEVERE,
                            new StringBuffer(
                                    "Can't retrieve MISSION_ID for cruise ")
                                    .append(cruiseName.toLowerCase().concat("-2008-10-11"))
                                    .append(". Aborting ingestion... file ")
                                    .append(ctdFile.getName())
                                    .append(" not ingested!")
                                    .toString());
	                
	            	if(psTmp != null){psTmp.close(); psTmp = null;}

	                throw new Exception(new StringBuffer(
	                    "Can't retrieve MISSION_ID for cruise ")
	                    .append(cruiseName.toLowerCase().concat("-2008-10-11"))
	                    .append(". Aborting ingestion... file ")
	                    .append(ctdFile.getName())
	                    .append(" not ingested!")
	                    .toString());                                   
	            }

            } else {
            	mission_id = rs.getLong("mission_id");
            }

            /** SHIP **/

        	if(rs != null){rs.close(); rs = null;}
        	if(ps != null){ps.close(); ps = null;}
            
            sqlString = "select * from ship where LOWER(name) = ? ";
            ps = conTarget.prepareStatement(sqlString);
            ps.setString(1, shipName.toLowerCase());
            rs = ps.executeQuery();
            
            if (!rs.next()) {
                  sqlString = "insert into ship(name) values(?)";
                  psTmp = conTarget.prepareStatement(sqlString);
                  psTmp.setString(1, shipName);
                  
                  psTmp.execute();
              	  if(psTmp != null){psTmp.close(); psTmp = null;}

                  sqlString = "select * from ship where LOWER(name) = ? ";
                  psTmp = conTarget.prepareStatement(sqlString);
                  psTmp.setString(1, shipName.toLowerCase());
                  rs = psTmp.executeQuery();
                  
                  if (rs.next()) {
                      ship_id = rs.getLong("ship_id");

                  	  if(psTmp != null){psTmp.close(); psTmp = null;}
                      
                      sqlString = "insert into ship_cruise(cruise_id, ship_id) values(?, ?)";
                      psTmp = conTarget.prepareStatement(sqlString);
                      psTmp.setLong(1, cruise_id);
                      psTmp.setLong(2, ship_id);
                      
                      psTmp.execute();
                  	  if(psTmp != null){psTmp.close(); psTmp = null;}

  	              }else{
                       LOGGER.log(Level.SEVERE,
                              new StringBuffer(
                                      "Can't retrieve SHIP_ID for ship ")
                                      .append(shipName)
                                      .append(". Aborting ingestion... file ")
                                      .append(ctdFile.getName())
                                      .append(" not ingested!")
                                      .toString());
		                
                   	   if(psTmp != null){psTmp.close(); psTmp = null;}

		               throw new Exception(new StringBuffer(
	                        "Can't retrieve SHIP_ID for ship ")
	                        .append(shipName)
	                        .append(". Aborting ingestion... file ")
	                        .append(ctdFile.getName())
	                        .append(" not ingested!")
	                        .toString());                               
	              }

            } else {
                ship_id = rs.getLong("ship_id");
            }
            
        	if(psTmp != null){psTmp.close(); psTmp = null;}
        	if(rs != null){rs.close(); rs = null;}
        	if(ps != null){ps.close(); ps = null;}
        	
            /**
             * Step 2: creating the Ocean Profile record and
             * retrieving its ID.
             */
            if (paramValues.size() > 0) {
                long ctd_id = -1;
                
                sqlString = "select * from observation where ext_name = ? and type_id = ? and cruise_id = ? and ship_id = ? and mission_id = ?";
                ps = conTarget.prepareStatement(sqlString);

                ps.setString(1, stationName);
                ps.setLong(2, type_id);
                ps.setLong(3, cruise_id);
                ps.setLong(4, ship_id);
                ps.setLong(5, mission_id);
                rs = ps.executeQuery();

                if (!rs.next()) {
                	if(ps != null){ps.close(); ps = null;}
                	
                    sqlString = "insert into observation(type_id, cruise_id, ship_id, sens_id, ext_name, lat, lon, obs_date, obs_time, w_depth, max_depth, mission_id, bbox, the_geom) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                            + "'((" + longitude + "," + latitude
                            + "),(" + longitude + "," + latitude
                            + "))'," + "geometryFromText('POINT("
                            + longitude + " " + latitude
                            + ")', 4326)) ";
                    ps = conTarget.prepareStatement(sqlString);

                    ps.setLong(1, type_id);
                    ps.setLong(2, cruise_id);
                    ps.setLong(3, ship_id);
                    ps.setLong(4, 0);
                    ps.setString(5, stationName);
                    ps.setDouble(6, latitude.doubleValue());
                    ps.setDouble(7, longitude.doubleValue());
                    ps.setDate(8, new Date(sf.parse(startDate).getTime()));                    
                    ps.setString(9, startTime);
                    ps.setDouble(10, Double.parseDouble(waterDepth));
                    ps.setDouble(11, Double.parseDouble(waterDepth));
                    ps.setDouble(12, mission_id);
                    
                    ps.execute();
                	if(rs != null){rs.close(); rs = null;}
                	if(ps != null){ps.close(); ps = null;}
                    
                    sqlString = "select * from observation where ext_name = ? and type_id = ? and cruise_id = ? and ship_id = ? and mission_id = ?";
                    ps = conTarget.prepareStatement(sqlString);

                    ps.setString(1, stationName);
                    ps.setLong(2, type_id);
                    ps.setLong(3, cruise_id);
                    ps.setLong(4, ship_id);
                    ps.setLong(5, mission_id);
                    rs = ps.executeQuery();

                    if (!rs.next()) {
                    	if(rs != null){rs.close(); rs = null;}
                    	if(ps != null){ps.close(); ps = null;}
                    	
                        LOGGER.log(Level.SEVERE, new StringBuffer(
                                "CTD ").append(stationName).append(
                                " doesn't exist in DB").append(". Aborting ingestion... file ")
                                .append(ctdFile.getName()).append(" not ingested!").toString());
                        
                        throw new Exception(new StringBuffer(
	                        "CTD ").append(stationName).append(
	                        " doesn't exist in DB").append(". Aborting ingestion... file ")
	                        .append(ctdFile.getName()).append(" not ingested!").toString());
                        
                    } else {
                        ctd_id = rs.getLong("obs_id");
                    }
                } else {
                    ctd_id = rs.getLong("obs_id");
                }
                
            	if(rs != null){rs.close(); rs = null;}
            	if(ps != null){ps.close(); ps = null;}
                
                /**
                 * Step 3: for each parameter checking if exists and
                 * if his unit of measure is correctly inserted.
                 */
                len = params.size();
                for (int p = 0; p < len; p++) {
                	
                    long param_id = -1;
                    long uom_id = -1;

                    final String paramName = (String) params.get(p);
                    final String uomName = (String) units.get(p);

                    // /////////////////////////////////////////////
                    //
                    // Check if parameter exists
                    //
                    // /////////////////////////////////////////////
                    
                    sqlString = "select * from parameter where ? = ANY(label)";
                    ps = conTarget.prepareStatement(sqlString);
                    ps.setString(1, paramName);
                    rs = ps.executeQuery();

                    if (!rs.next()) {
                            sqlString = "select * from unit_of_mesure where ? = ANY(label)";
                            psTmp = conTarget.prepareStatement(sqlString);
                            psTmp.setString(1, uomName);
                            rs = psTmp.executeQuery();

                            if (rs.next()) {
                                uom_id = rs.getLong("uom_id");
                            } else {
                            	if(psTmp != null){psTmp.close(); psTmp = null;}
                            	if(rs != null){rs.close(); rs = null;}

                                sqlString = "insert into unit_of_mesure(default_label, label, description) values(?, ARRAY['"
                                        + uomName + "'], ?)";
                                psTmp = conTarget.prepareStatement(sqlString);
                                
                                psTmp.setString(1, uomName);
                                psTmp.setString(2, uomName);
                                psTmp.execute();

                            	if(psTmp != null){psTmp.close(); psTmp = null;}

                                sqlString = "select * from unit_of_mesure where ? = ANY(label)";
                                psTmp = conTarget.prepareStatement(sqlString);
                                
                                psTmp.setString(1, uomName);
                                rs = psTmp.executeQuery();

                                if (rs.next()) {
                                    uom_id = rs.getLong("uom_id");
                                } else {
                                    LOGGER.log(Level.SEVERE,
                                                    new StringBuffer("Can't retrieve UOM_ID for unit of mesure ")
                                                            .append(uomName)
                                                            .append(". Aborting ingestion... file ")
                                                            .append(ctdFile.getName())
                                                            .append(" not ingested!")
                                                            .toString());
                                	if(psTmp != null){psTmp.close(); psTmp = null;}
                                	if(rs != null){rs.close(); rs = null;}
                                    
                                    throw new Exception(new StringBuffer("Can't retrieve UOM_ID for unit of mesure ")
	                                    .append(uomName)
	                                    .append(". Aborting ingestion... file ")
	                                    .append(ctdFile.getName())
	                                    .append(" not ingested!")
	                                    .toString());
                                }
                            }

                        	if(psTmp != null){psTmp.close(); psTmp = null;}
                        	if(rs != null){rs.close(); rs = null;}
                            
                            sqlString = "insert into parameter(default_label, label, uom_id) values(?, ARRAY['"
                                    + paramName + "'], ?)";
                            psTmp = conTarget.prepareStatement(sqlString);
                            
                            psTmp.setString(1, paramName);
                            psTmp.setLong(2, uom_id);
                            
                            psTmp.execute();
                        	if(psTmp != null){psTmp.close(); psTmp = null;}

                            sqlString = "select * from parameter where ? = ANY(label)";
                            psTmp = conTarget.prepareStatement(sqlString);
                            
                            psTmp.setString(1, paramName);
                            rs = psTmp.executeQuery();
                            
                            if (rs.next()){
                                param_id = rs.getLong("param_id");
                            }else{
                                LOGGER.log(Level.SEVERE,
                                                new StringBuffer("Can't retrieve PARAM_ID for parameter ")
                                                        .append(paramName)
                                                        .append(". Aborting ingestion... file ")
                                                        .append(ctdFile.getName())
                                                        .append(" not ingested!")
                                                        .toString());
                                
                            	if(psTmp != null){psTmp.close(); psTmp = null;}
                            	if(rs != null){rs.close(); rs = null;}
                                
                                throw new Exception(new StringBuffer("Can't retrieve PARAM_ID for parameter ")
	                                .append(paramName)
	                                .append(". Aborting ingestion... file ")
	                                .append(ctdFile.getName())
	                                .append(" not ingested!")
	                                .toString());
                            }
                    } else {
                        param_id = rs.getLong("param_id");
                    }
                }

            	if(psTmp != null){psTmp.close(); psTmp = null;}
            	if(rs != null){rs.close(); rs = null;}
            	if(ps != null){ps.close(); ps = null;}

                // /////////////////////////////////////////////////////////////////////
                //
                // Building zpos ARRAY for Sql...
                //
                // /////////////////////////////////////////////////////////////////////
                
                final StringBuffer zPos = new StringBuffer();

                Set keySet = paramValues.keySet();
                for (Iterator k_iT = keySet.iterator(); k_iT.hasNext();){
                    final Integer key = (Integer) k_iT.next();
                    zPos.append(key).append(",");
                }
                
                if (zPos.lastIndexOf(",") > 0) {
                	zPos.deleteCharAt(zPos.lastIndexOf(","));
                	
                    // /////////////////////////////////////////////////////////////////////
                    //
                    // ... and inserting values on DB.
                    //
                    // /////////////////////////////////////////////////////////////////////

                    sqlString = "insert into measurement(obs_id, zpos, tpos, depth) values(?, ARRAY["
                            + zPos.toString()
                            + "], ?, ?)";
                    ps = conTarget.prepareStatement(sqlString);
                    ps.setLong(1, ctd_id);
                    
      	            Date date = new Date(sf.parse(startDate).getTime());
		            Calendar calendar = Calendar.getInstance();		  
		          
		            String[] time_string = startTime.split(":");
		            calendar.setTime(date);
		          
		            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 
		        		  Integer.parseInt(time_string[0]), Integer.parseInt(time_string[1]));	
		            calendar.set(Calendar.SECOND, 0);		          

		            ps.setTimestamp(2, new Timestamp(calendar.getTimeInMillis())); 		
		            ps.setDouble(3, 0);												                     
                    ps.execute();

                	if(ps != null){ps.close(); ps = null;}
                }
                                
                for (int k = 0; k < len; k++) {
                	
                    long param_id = -1;
                    final String paramName = (String) params.get(k);
                	
                    // /////////////////////////////////////////////////////////////////////
                    //
                    // Building valus ARRAY for Sql...
                    //
                    // /////////////////////////////////////////////////////////////////////
                    
                    final StringBuffer values = new StringBuffer();

                    for (Iterator k_iT = keySet.iterator(); k_iT.hasNext();){
                        final Integer key = (Integer) k_iT.next();
                        values.append(((ArrayList) paramValues.get(key)).get(k)).append(",");
                    }
                    
                    if (values.lastIndexOf(",") > 0) {
                        values.deleteCharAt(values.lastIndexOf(","));
                        
                        sqlString = "select * from parameter where ? = ANY(label)";
                        ps = conTarget.prepareStatement(sqlString);
                        
                        ps.setString(1, paramName);
                        rs = ps.executeQuery();

                        if (rs.next())
                            param_id = rs.getLong("param_id");
                        else {
                            LOGGER.log(Level.SEVERE,
                                            new StringBuffer("Can't retrieve PARAM_ID for parameter ")
                                                    .append(paramName)
                                                    .append(". Aborting ingestion... file ")
                                                    .append(ctdFile.getName())
                                                    .append(" not ingested!")
                                                    .toString());
                            
                        	if(rs != null){rs.close(); rs = null;}
                        	if(ps != null){ps.close(); ps = null;}
                            
                            throw new Exception(new StringBuffer("Can't retrieve PARAM_ID for parameter ")
	                            .append(paramName)
	                            .append(". Aborting ingestion... file ")
	                            .append(ctdFile.getName())
	                            .append(" not ingested!")
	                            .toString());
                        }

                    	if(rs != null){rs.close(); rs = null;}
                    	if(ps != null){ps.close(); ps = null;}
                        
                        sqlString = "select max(measurement_id) as measurement_id from measurement where obs_id = ?";
                        ps = conTarget.prepareStatement(sqlString);
                        ps.setLong(1, ctd_id);
                        rs = ps.executeQuery();
                        
                        if (rs.next()) {
	              	          sqlString = "insert into measurement_values(measurement_id, values, param_id) values(?, ARRAY["
	                                            + values.toString() + "], ?)";
	            	          psTmp = conTarget.prepareStatement(sqlString);
	            	          
	            	          psTmp.setLong(1, rs.getLong("measurement_id"));   
	            	          psTmp.setLong(2, param_id); 					  
	            	          
	            	          psTmp.execute();
	                      	  if(psTmp != null){psTmp.close(); psTmp = null;}
                        }

                    	if(rs != null){rs.close(); rs = null;}
                    	if(ps != null){ps.close(); ps = null;}
                    }                    
                }
            }           

        }catch(Exception exc){
        	conTarget.rollback();
        	throw new Exception("EXCEPTION -> " + exc.getLocalizedMessage());
        }finally{        	
        	if(psTmp != null){psTmp.close(); psTmp = null;}
        	if(rs != null){rs.close(); rs = null;}
        	if(ps != null){ps.close(); ps = null;}
	 	}
    }
}
