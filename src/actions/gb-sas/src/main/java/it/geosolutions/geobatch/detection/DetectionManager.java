/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
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

package it.geosolutions.geobatch.detection;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.base.Utils;
import it.geosolutions.geobatch.base.Utils.FolderContentType;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;
import it.geosolutions.geobatch.geoserver.shapefile.ShapeFileGeoServerConfigurator;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.task.TaskExecutor;
import it.geosolutions.geobatch.task.TaskExecutorConfiguration;
import it.geosolutions.geobatch.utils.IOUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Action which allows to run a script to convert a detection file to a shapefile 
 * and ingest it on geoserver via rest
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class DetectionManager extends BaseAction<FileSystemMonitorEvent> implements
        Action<FileSystemMonitorEvent> {

	public static class ScriptParams{
		public final static String PATH = "shapeGeneratorScript";
		
		public final static String INPUT = "inputDir";
		
		public final static String OUTPUT = "outputDir";
		
		public final static String LOGDIR = "loggingDir";
		
		public final static String CRS = "crsDefinitionsDir";
	}
	
    private DetectionManagerConfiguration configuration;

    private final static Logger LOGGER = Logger.getLogger(DetectionManager.class.toString());
    
    final static FileFilter FILEFILTER = createFilter();
    
    protected DetectionManager(DetectionManagerConfiguration configuration)
            throws IOException {
        this.configuration = configuration;
    }

    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws Exception {
        try {
            
            // looking for file
            if (events.size() != 1)
                throw new IllegalArgumentException("Wrong number of elements for this action: "+ events.size());
            
            if (this.configuration == null) {
                throw new IllegalStateException("DataFlowConfig is null.");
            }
            
            final FileSystemMonitorEvent event = events.remove();
            final File inputFile = event.getSource();
            
            // //
            //
            // Get the directory containing the data from the specified
            // XML file
            //
            // //
            final List<String> missionDirs = Utils.getDataDirectories(inputFile, FolderContentType.DETECTIONS);
            
            if (missionDirs==null || missionDirs.isEmpty()){
            	LOGGER.warning("Unable to find target data location from the specified file: "+inputFile.getAbsolutePath());
            	return events;
            }
            final int nMissions = missionDirs.size();
            if (LOGGER.isLoggable(Level.INFO))
            	LOGGER.info(new StringBuilder("Found ").append(nMissions).append(" mission").append(nMissions>1?"s":"").toString());
            
            for (String mission : missionDirs){
            	String initTime = null;
            	if (LOGGER.isLoggable(Level.INFO))
                	LOGGER.info("Processing Mission: " + mission);
            	
            	final String directory = mission;
	            final File fileDir = new File(directory); //Mission dir
	            if (fileDir != null && fileDir.isDirectory()) {
	                final File[] foundFiles = fileDir.listFiles(FILEFILTER);
	                if (foundFiles != null && foundFiles.length>0){
	                	initTime = Utils.setInitTime(directory,2);
	                    final String subDir = buildDetectionsSubDir(initTime, fileDir);
	                    ingestDetection(fileDir, subDir);
	                }
	            }
	            if (LOGGER.isLoggable(Level.INFO))
	            	LOGGER.info("Done");
            }            
            if (LOGGER.isLoggable(Level.INFO))
            	LOGGER.info("End Of processing");
            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
        }
    }

    /**
     * Setup a proper subDirectory path containing the mission time and mission name
     * @param initTime
     * @param fileDir
     * @return
     */
    private String buildDetectionsSubDir(final String initTime, final File fileDir) {
    	StringBuilder sb = new StringBuilder(initTime).append(Utils.SEPARATOR);
    	String missionName = fileDir.getName();
    	sb.append(missionName);
		return sb.toString();
	}

    /**
     * 
     * @param inputDir
     * @param subDir
     * @throws Exception
     */
	private void ingestDetection(final File inputDir, final String subDir) throws Exception {
//        final String baseName = FilenameUtils.getBaseName(inputDir.getAbsolutePath());
        final String baseName = FilenameUtils.getBaseName(inputDir.getAbsolutePath());
        // //
        //
        // Prepare a TaskExecutor to run a conversion script on the provided detection input
        // 
        // //
   		final TaskExecutor executor = configureExecutor();
		final Queue<FileSystemMonitorEvent> events = new LinkedBlockingQueue<FileSystemMonitorEvent>();
		final String outputDir = new StringBuilder(configuration.getDetectionsOutputDir()).append(Utils.SEPARATOR).append(subDir).toString();
		
		// Generate an XML File containing the script parameters 
		final File xmlFile = generateXML(inputDir, outputDir);
		
		// Invoke the taskExecutor to run the script and convert the detection to a shape file
		FileSystemMonitorEvent fse = new FileSystemMonitorEvent(xmlFile, FileSystemMonitorNotifications.FILE_ADDED);
		events.add(fse);
		executor.execute(events);
		if (events == null){
			throw new RuntimeException("Task Execution went wrong");
		}

		IOUtils.deleteFile(xmlFile);
		
		final String dataPrefix = new StringBuilder(outputDir)
		.append(Utils.SEPARATOR).append("target_").append(baseName)
		.append(Utils.SEPARATOR).append("target_").append(baseName).toString();
		
		final String prjFile = new StringBuilder(dataPrefix).append(".prj").toString();
		
		// Does additional checks on the prj file.
		checkPrj(prjFile);
		zipShape(dataPrefix);
		sendShape(dataPrefix + ".zip");
		
	}

    /**
     * Produce an XML file containing the configuration for a script to be launched
     * by the Task Executor.
     * @param inputFile 
     * @param initTime 
     * 
     * @return
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws IOException
     */
	private File generateXML(final File inputFile, final String outputDir) throws ParserConfigurationException, TransformerException, IOException {
		final DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
	    
		//Get the DocumentBuilder
	    DocumentBuilder parser = dfactory.newDocumentBuilder();
	    //Create blank DOM Document
	    Document doc = parser.newDocument();
	    
	    Element root = doc.createElement("PythonShapeGenerator");
	    doc.appendChild(root);
	    
	    Element element = doc.createElement(ScriptParams.PATH);
	    root.appendChild(element);
	    
	    final File converterPath = IOUtils.findLocation(configuration.getDetectionConverterPath(),
                new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
	    
	    // Add a text node to the beginning of the element
	    element.insertBefore(doc.createTextNode(converterPath.getAbsolutePath()), null);
	    
	    element = doc.createElement(ScriptParams.INPUT);
	    root.appendChild(element);
	    element.insertBefore(doc.createTextNode(inputFile.getAbsolutePath()), element.getFirstChild());

	    final File outDir = new File(outputDir);
	    if (!outDir.exists()) {
            Utils.makeDirectories(outputDir);
        }
	    element = doc.createElement(ScriptParams.OUTPUT);
	    root.appendChild(element);
	    element.insertBefore(doc.createTextNode(outputDir), null);
	    
	    final String crsDir = configuration.getCrsDefinitionsDir();
	    if (crsDir != null && crsDir.trim().length()>0){
	    	 final File crsPath = IOUtils.findLocation(crsDir,
	                 new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
	    	if (crsPath != null && crsPath.exists() && crsPath.isDirectory()){
		    	element = doc.createElement(ScriptParams.CRS);
			    root.appendChild(element);
			    element.insertBefore(doc.createTextNode(crsPath.getAbsolutePath()), null);
	    	}
	    }
	    
	    final String logDir = configuration.getLoggingDir();
	    if (logDir != null && logDir.trim().length()>0){
	    	 final File logPath = IOUtils.findLocation(logDir,
	                 new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
	    	if (logPath != null && logPath.exists() && logPath.isDirectory()){
		    	element = doc.createElement(ScriptParams.LOGDIR);
			    root.appendChild(element);
			    element.insertBefore(doc.createTextNode(logPath.getAbsolutePath()), null);
	    	}
	    }
	    	    
	    final TransformerFactory factory = TransformerFactory.newInstance();
	    final Transformer transformer = factory.newTransformer();
	    final File file = File.createTempFile("shapegen", ".xml");
	    final Result result = new StreamResult(file);
	    final Source xmlSource = new DOMSource(doc);
	    transformer.transform(xmlSource, result);
	    return file;
	}

	/**
	 * Use a proper {@link ShapeFileGeoServerConfigurator} action to send the zipped detection
	 * shapefile 
	 * @param fileToBeSent 
	 * @throws Exception
	 */
	private void sendShape(final String fileToBeSent) throws Exception {
    	final GeoServerActionConfiguration gsConfig = new GeoServerActionConfiguration();
    	gsConfig.setGeoserverURL(configuration.getGeoserverURL());
    	gsConfig.setGeoserverUID(configuration.getGeoserverUID());
    	gsConfig.setGeoserverPWD(configuration.getGeoserverPWD());
    	gsConfig.setDataTransferMethod(configuration.getGeoserverUploadMethod());
    	gsConfig.setWorkingDirectory(configuration.getWorkingDirectory());
    	gsConfig.setDefaultNamespace(configuration.getDefaultNamespace());
    	gsConfig.setWmsPath(buildWmsPath(fileToBeSent));
    	gsConfig.setDefaultStyle(configuration.getDetectionStyle());
    	ShapeFileGeoServerConfigurator gsGenerator = new ShapeFileGeoServerConfigurator(gsConfig);
		Queue<FileSystemMonitorEvent> events = new LinkedBlockingQueue<FileSystemMonitorEvent>();
		FileSystemMonitorEvent fse = new FileSystemMonitorEvent(new File(fileToBeSent), FileSystemMonitorNotifications.FILE_ADDED);
		events.add(fse);
    	gsGenerator.execute(events);
	}

	/**
	 * Create a zip archive by filling it with a set of file defining a shapefile dataset.
	 * @param baseFile
	 * @return
	 * @throws IOException
	 */
	private String zipShape(final String baseFile) throws IOException{
        final String prjFile = new StringBuilder(baseFile).append(".prj").toString();
        final String shapeFile = new StringBuilder(baseFile).append(".shp").toString();
        final String dbfFile = new StringBuilder(baseFile).append(".dbf").toString();
        final String shxFile = new StringBuilder(baseFile).append(".shx").toString();
        final String zipFile = new StringBuilder(baseFile).append(".zip").toString();

        final String files[] = new String[] { shapeFile, dbfFile, shxFile, prjFile };
        final byte[] buffer = new byte[4096]; // Create a buffer for copying
        int bytesRead;

        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                zipFile));
        try {
	        for (String file : files) {
	            final File f = new File(file);
	            if (f.isDirectory())
	                continue; // Ignore directory
	            final FileInputStream in = new FileInputStream(f); // Stream to read file
	            final ZipEntry entry = new ZipEntry(f.getName()); // Make a ZipEntry
	            out.putNextEntry(entry); // Store entry
	            try {
	            	while ((bytesRead = in.read(buffer)) != -1)
	            		out.write(buffer, 0, bytesRead);
	            } finally{
	            	if (in != null){
	            		try{
	            			in.close();
	            		} catch (Throwable t){
	            			//Eat me
	            		}
	            		
	            	}
	            }
	        }
        } catch (IOException e) {
			if (LOGGER.isLoggable(Level.FINE))
				LOGGER.log(Level.FINE, e.getLocalizedMessage());
		} finally{
			if (out!=null){
				try{
					out.close();
				}catch(Throwable t){
					if (LOGGER.isLoggable(Level.FINE))
						LOGGER.log(Level.FINE, t.getLocalizedMessage());
				}
			}
		}
        return zipFile;
	}

	/**
	 * Check whether the PRJ file contains a real WKT definition or a single line
	 * indicating an EPSG code such as EPSG:XXXcode (As an instance: EPSG:32632).
	 * In the latter case, rewrite that PRJ with the proper WKT definition.
	 * That case may happen when the converting utility didn't successfully find a
	 * valid WKT definition to be set for the related shape file.
	 * 
	 * @param prjFile
	 */
	private void checkPrj(final String prjFile) {
    	final File prj = new File(prjFile);
    	if (prj == null || !prj.exists())
    		throw new IllegalArgumentException("Prj File is missing: "+prjFile);
    	FileInputStream fis = null;
    	String epsgCode = null; 
    	try{
    		fis = new FileInputStream(prj);
    		byte[] headerEPSG = new byte[12];
    		final int len = fis.read(headerEPSG);
    		
    		//Checking whether it contains the EPSG code directly instead of
    		//a proper WKT
    		if (headerEPSG[0] == (byte)'E' &&
    				headerEPSG[1] == (byte)'P' &&
    				headerEPSG[2] == (byte)'S' &&
    				headerEPSG[3] == (byte)'G' &&
    				headerEPSG[4] == (byte)':'){
    			final StringBuilder sb = new StringBuilder("EPSG:");
    			for (int i=5;i<len;i++){
    				sb.append(headerEPSG[i]-48);
    			}
    			epsgCode = sb.toString();
    			
    			//Try parsing the provided EPSG code to setup a valid CRS
    			final CoordinateReferenceSystem crs = CRS.decode(epsgCode);
    			if (crs != null){
    				
					String s = crs.toWKT();
					s = s.replaceAll("\n", "").replaceAll("  ", "");
					
					//Write out the proper PRJ file
					FileWriter out = new FileWriter(prj);
					try{
						if (fis != null)
							fis.close();	
					}catch(Throwable t){
						//Eat me
					}
					
					try {
					    out.write(s);
					} finally {
					    out.close();
					}
    			}
    		}
    	} catch (FileNotFoundException e) {
    		throw new IllegalArgumentException("Unable to decode the provided epsg "+epsgCode,e);
		} catch (IOException e) {
			throw new IllegalArgumentException("Unable to decode the provided epsg "+epsgCode,e);
		} catch (NoSuchAuthorityCodeException e) {
			throw new IllegalArgumentException("Unable to decode the provided epsg "+epsgCode,e);
		} catch (FactoryException e) {
			throw new IllegalArgumentException("Unable to decode the provided epsg "+epsgCode,e);
		} finally {
    		try{
    			if (fis != null)
    				fis.close();
    		}catch(Throwable t){
    			//Eat exception
    		}
    	}
	}

	/**
	 * Initialize a {@link TaskExecutor} for the shapefile generation.
	 * @return
	 * @throws IOException
	 */
	private TaskExecutor configureExecutor() throws IOException {
		final TaskExecutorConfiguration taskConfig = new TaskExecutorConfiguration();
		
		// //
		//
		// 1) Setting the crsDefintionDir which represents a folder containing WKT defintions
		// available as prj files named "crsXXXX.prj" where XXXX is an EPSG code
		//
		// //
    	final String crsDefintionDir = configuration.getCrsDefinitionsDir();
    	File crsDefDir = null;  
        if (crsDefintionDir != null && crsDefintionDir.trim().length()>0){
        	 crsDefDir = IOUtils.findLocation(crsDefintionDir,
	                 new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
        	if(crsDefDir == null || ! crsDefDir.exists() || !crsDefDir.isDirectory()){
        		throw new IllegalArgumentException("The provided CRS WKT Definitions folder isn't valid" + crsDefintionDir);
        	}
        }
        
		// //
		//
		// 2) Setting the errorLog file which will contains error occurred during task execution
		//
		// //
		final String errorLog = configuration.getDetectionsErrorLog();
        if (errorLog != null && errorLog.trim().length()>0){
        	 final File errorLogFile = IOUtils.findLocation(errorLog,
	                 new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
        	if(errorLogFile != null && errorLogFile.exists()){
        		taskConfig.setErrorFile(errorLogFile.getAbsolutePath());
        	}
        }
        
    	// //
		//
		// 3) Setting the xsl path which will contains xsl needed to setup a proper XML file 
		//
		// //
        final String xslPath = configuration.getXslPath();
        if (xslPath != null && xslPath.trim().length()>0){
        	 final File xslFile = IOUtils.findLocation(xslPath,
	                 new File(((FileBaseCatalog) CatalogHolder.getCatalog()).getBaseDirectory()));
        	if (xslFile != null && xslFile.exists()){
        		taskConfig.setXsl(xslFile.getAbsolutePath());
        	}
        	else {
        		throw new IllegalArgumentException("Invalid xsl file: " + xslPath);
        	}
        }

        taskConfig.setExecutable(configuration.getExecutablePath());
        taskConfig.setTimeOut(new Long(configuration.getConverterTimeout()));
        final Map<String,String> variables = new HashMap<String, String>();
		variables.put("GDAL_DATA", configuration.getGdalData());
		variables.put("PATH", configuration.getPath());
		
		taskConfig.setVariables(variables);
		
		final TaskExecutor executor = new TaskExecutor(taskConfig);
		return executor;
	}

	public ActionConfiguration getConfiguration() {
        return configuration;
    }
	
	
	private static IOFileFilter createFilter() {
		IOFileFilter fileFilter = includeFilters(
				FileFilterUtils.suffixFileFilter("mat"));
		return fileFilter;
	}
	
	static IOFileFilter excludeFilters(final IOFileFilter inputFilter,
			IOFileFilter ...filters) {
		IOFileFilter retFilter=inputFilter;
		for(IOFileFilter filter:filters){
			retFilter=FileFilterUtils.andFileFilter(
					retFilter, 
					FileFilterUtils.notFileFilter(filter));
		}
		return retFilter;
	}
	
	static IOFileFilter includeFilters(final IOFileFilter inputFilter,
			IOFileFilter ...filters) {
		IOFileFilter retFilter=inputFilter;
		for(IOFileFilter filter:filters){
			retFilter=FileFilterUtils.orFileFilter(retFilter, filter);
		}
		return retFilter;
	}
	
	/**
     * Build a WMSPath from the specified inputFile 
     * Input names are in the form: /DATE/MISSION/target_MISSION/target_completefilename

     * 
     * @param name
     * @return
     */
	public static String buildWmsPath(final String inputFileName) {
		if (inputFileName==null || inputFileName.trim().length()==0)
			return "";
		
		//Will be something like 
		//target_MUSCLE_CAT2_091002_1_12_s_6506_6658_40_150_det029_r127_dt032.shp
		final File file = new File(inputFileName);
		
		//will refer to /MISSIONDIR
		final File missionDir = file.getParentFile().getParentFile();
		
		//will refer to /DATE
		final File timeDir = missionDir.getParentFile();
		String time = FilenameUtils.getBaseName(timeDir.getAbsolutePath());
		
		String missionName = FilenameUtils.getBaseName(missionDir.getAbsolutePath());
		final int missionIndex = missionName.lastIndexOf("_");
	    if (missionIndex!=-1){
	     	final String missionCollapsed = missionName.substring(0,missionIndex).replace("_", "-");
	        missionName = new StringBuilder("mission").append(missionCollapsed).append(missionName.substring(missionIndex+1)).toString();
	    }
	    else {
	     	missionName = new StringBuilder("mission").append(missionName).toString();
	    }
			
        final String wmsPath = new StringBuilder("/").append(time).append("/").append(missionName).toString();
        return wmsPath;
	}
}
