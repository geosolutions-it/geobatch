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
package it.geosolutions.geobatch.lamma.build;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.lamma.base.LammaBaseAction;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.measure.unit.Unit;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;

import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.Category;
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridCoverageWriter;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.util.NumberRange;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Comments here ...
 * 
 * @author Alessio Fabiani, GeoSolutions
 */
public class LammaGribBuilderAction extends LammaBaseAction {

    protected final static Logger LOGGER = Logger.getLogger(LammaGribBuilderAction.class.toString());
    protected final LammaGribBuilderConfiguration configuration;

	private static CoordinateReferenceSystem wgs84;

	private static final int DEFAULT_TILE_SIZE = 256;

	private static final double DEFAULT_COMPRESSION_RATIO = 0.75;

	private static final String DEFAULT_COMPRESSION_TYPE = "LZW";
	private static final double NaN = 9.999e+20;
	private static final double DEFAULT_LEVEL_NUMBER = 10.0;

    /**
     *
     * @param configuration
     */
    @SuppressWarnings("static-access")
	public LammaGribBuilderAction(LammaGribBuilderConfiguration configuration) throws IOException {
        super(configuration);
        this.configuration = configuration;
        try {
			this.wgs84 = CRS.decode("EPSG:4326", true);
		} catch (NoSuchAuthorityCodeException e) {
			throw new IllegalStateException(e);
		} catch (FactoryException e) {
			throw new IllegalStateException(e);
		}
    }

    /**
     * 
     * @param events
     * @return
     * @throws ActionException
     */
    public Queue<FileSystemMonitorEvent> execute(Queue<FileSystemMonitorEvent> events)
            throws ActionException {
        try {
            listenerForwarder.started();

            // //
            //
            // data flow configuration and dataStore name must not be null.
            //
            // //
            if (configuration == null) {
                throw new IllegalStateException("DataFlowConfig is null.");
            }

            Queue<FileSystemMonitorEvent> outEvents = new LinkedList<FileSystemMonitorEvent>();
            
			// Logging to ESB ...
            logMessage.setMessage("Going to create " + events.size() + " GeoTIFFs...");
            logMessage.setMessageTime(new Date());
			logToESB(logMessage);

            while(events.size() > 0) {
                // get the first event
                final FileSystemMonitorEvent event = events.remove();
                final File inputFile = event.getSource();
                
                final File datFile = new File(getScriptArguments(inputFile.getAbsolutePath(), "o"));

            	Envelope envelope = exctractEnvelope(datFile);
            	Scanner scanner = null;
                if (datFile.exists() && datFile.isFile()) {
                	try {
                    	scanner = new Scanner(datFile);
                    	String[] widhtHeight = scanner.nextLine().split(" ");

                    	final int width  = Integer.parseInt(widhtHeight[0]);
                    	final int height = Integer.parseInt(widhtHeight[1]);
                    	
                    	final SampleModel outSampleModel = getSampleModel(width, height,1);
                    	
                    	WritableRaster userRaster = Raster.createWritableRaster(outSampleModel, null);
                    	
                    	double[] extrema = new double[2];
                    	extrema[0] = Double.POSITIVE_INFINITY;
                    	extrema[1] = Double.NEGATIVE_INFINITY;
                    	for (int yPos = 0; yPos < height; yPos++) {
                    		for (int xPos = 0; xPos < width; xPos++) {
                    			double sample = Double.parseDouble(scanner.nextLine());
								userRaster.setSample(xPos, height - yPos - 1, 0, sample);
								
								if (sample < extrema[0] && sample != NaN)
									extrema[0] = sample;
								
								if (sample > extrema[1] && sample != NaN)
									extrema[1] = sample;
                    		}
                    	}
                    	
        				final File geoTiffFile = storeCoverageAsGeoTIFF(
                    			inputFile.getParentFile(), 
                    			FilenameUtils.getBaseName(datFile.getName()), 
                    			FilenameUtils.getBaseName(datFile.getName()).split("_")[0], 
                    			userRaster, 
                    			NaN, 
                    			envelope, 
                    			DEFAULT_COMPRESSION_TYPE, 
                    			DEFAULT_COMPRESSION_RATIO, 
                    			DEFAULT_TILE_SIZE
                    	);
                    	
        				final File contourFile = buildContourXMLFile(geoTiffFile, extrema);
        				outEvents.add(new FileSystemMonitorEvent(contourFile, FileSystemMonitorNotifications.FILE_ADDED));
                	} catch (Exception e) {
                		LOGGER.severe("Errors occurred reading DAT file: " + e.getLocalizedMessage());
                    	throw new IllegalStateException("Errors occurred reading DAT file: " + e.getLocalizedMessage());
                	} finally {
                		if (scanner != null)
                			scanner.close();
                	}

                } else {
        			// Logging to ESB ...
                    logMessage.setMessage("[ERROR] " + "DAT file is invalid.");
                    logMessage.setMessageTime(new Date());
        			logToESB(logMessage);

                	throw new IllegalStateException("DAT file is invalid.");
                }
            }
            
            listenerForwarder.completed();
            
            return outEvents;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            }
			// Logging to ESB ...
            logMessage.setMessage("[ERROR] " + t.getLocalizedMessage());
            logMessage.setMessageTime(new Date());
			logToESB(logMessage);

            listenerForwarder.failed(t);
            throw new ActionException(this, t.getMessage(), t);
        }

    }

    /**
     * 
     * @param geoTiffFile
     * @param extrema 
     * @return
     */
    private static File buildContourXMLFile(final File geoTiffFile, double[] extrema) {
    	double level = DEFAULT_LEVEL_NUMBER;
    	
    	if (extrema != null && extrema.length == 2 && extrema[0] != Double.POSITIVE_INFINITY && extrema[1] != Double.NEGATIVE_INFINITY) {
    		level = (Math.abs(extrema[1]) - Math.abs(extrema[0])) / level;
    	}
    	
    	FileWriter outW = null;
    	PrintWriter out = null;
    	
    	/**
    	 * Building up taskExecutor XML input file for the next action...
    	 */
    	final String baseName = FilenameUtils.getBaseName(geoTiffFile.getAbsolutePath());
    	final File prjFile = new File(geoTiffFile.getParentFile(), baseName+".prj");
    	final File destFile = new File(geoTiffFile.getParentFile(), baseName+".shp");
    	final File taskExecutorInput = new File(geoTiffFile.getParentFile(), baseName+"-contour.xml");
    	try {
    		outW = new FileWriter(prjFile);
    		out = new PrintWriter(outW);

    		// Write text to file
    		out.println(wgs84.toWKT());
    	} catch (IOException e){
    		LOGGER.severe(e.getLocalizedMessage());
    	} finally {
    		if (out != null)
    			out.close();

    		if (outW != null)
    			try {
    				outW.close();
    			} catch (IOException e) {
    				LOGGER.severe(e.getLocalizedMessage());
    			}
    	}
    	
    	try {
    		outW = new FileWriter(taskExecutorInput);
    		out = new PrintWriter(outW);

    		// Write text to file
    		out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
    		out.println("<GdalContour>");
    		out.println("  <a>elev</a>");
			out.println("  <i>"+level+"</i>");
    		out.println("  <srcfile>"+geoTiffFile.getAbsolutePath()+"</srcfile>");
    		out.println("  <dstfile>"+destFile.getAbsolutePath()+"</dstfile>");
    		out.println("</GdalContour>");
    	} catch (IOException e){
    		LOGGER.severe(e.getLocalizedMessage());
    	} finally {
    		if (out != null)
    			out.close();

    		if (outW != null)
    			try {
    				outW.close();
    			} catch (IOException e) {
    				LOGGER.severe(e.getLocalizedMessage());
    			}
    	}
    	
		return taskExecutorInput;
    }

	/**
     * 
     * @param datFile
     * @return
     */
	private static Envelope exctractEnvelope(File datFile) {
		Envelope envelope = null;
		
		Scanner scanner = null;
		try {
			scanner = new Scanner(new File(datFile.getAbsolutePath().replaceAll("\\.dat", "\\.header")));
			
			String lonLine = null;
			String latLine = null;
			
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				
				if (line.contains("latlon")) {
					latLine = line.substring(line.lastIndexOf("lat") + "lat".length(), line.indexOf("by")).trim();
				}
				
				if (line.trim().startsWith("long")) {
					lonLine = line.substring(line.indexOf("long") + "long".length(), line.indexOf("by")).trim();
				}
				
				if (lonLine != null && latLine != null)
					break;
			}
			
			if (lonLine != null && latLine != null) {
				double minX, minY;
				double maxX, maxY;
				
				minX = Double.parseDouble(lonLine.split(" ")[0]);
				minY = Double.parseDouble(latLine.split(" ")[0]);
				
				maxX = Double.parseDouble(lonLine.split(" ")[2]);
				maxY = Double.parseDouble(latLine.split(" ")[2]);
				
				envelope = new GeneralEnvelope(new double[]{minX, minY}, new double[] {maxX, maxY});
				((GeneralEnvelope)envelope).setCoordinateReferenceSystem(wgs84);
			} else {
				throw new IllegalStateException("Invalid header file.");
			}
			
		} catch (Exception e) {
			LOGGER.severe("Errors extracting envelope: ");
		} finally {
			if (scanner != null)
				scanner.close();
		}
		
		return envelope;
	}

	/**
	 * 
	 * @param outDir 
	 * @param fileName 
	 * @param varName 
	 * @param userRaster
	 * @param envelope 
	 * @param compressionType 
	 * @param compressionRatio 
	 * @param tileSize 
	 * @return 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@SuppressWarnings("deprecation")
	public static File storeCoverageAsGeoTIFF(
			final File outDir, 
			final String coverageName, 
			final CharSequence varName, 
			WritableRaster userRaster,
			final double inNoData,
			Envelope envelope, 
			final String compressionType, final double compressionRatio, final int tileSize) 
	throws IllegalArgumentException, IOException {
		// /////////////////////////////////////////////////////////////////////
		//
		// PREPARING A WRITE
		//
		// /////////////////////////////////////////////////////////////////////
		if (LOGGER.isLoggable(Level.FINE))
			LOGGER.fine("Writing down the file in the decoded directory...");
		final GeoTiffFormat wformat = new GeoTiffFormat();
		final GeoTiffWriteParams wp = new GeoTiffWriteParams();
		if (!Double.isNaN(compressionRatio)) {
			wp.setCompressionMode(GeoTiffWriteParams.MODE_EXPLICIT);
			wp.setCompressionType(compressionType);
			wp.setCompressionQuality((float) compressionRatio);
		}
		wp.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
		wp.setTiling(tileSize, tileSize);
		final ParameterValueGroup wparams = wformat.getWriteParameters();
		wparams.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);

		// keep original name
		final File outFile = new File(outDir, coverageName.toString() + ".tiff");

		// /////////////////////////////////////////////////////////////////////
		//
		// ACQUIRING A WRITER AND PERFORMING A WRITE
		//
		// /////////////////////////////////////////////////////////////////////
		final Hints hints = new Hints(Hints.TILE_ENCODING, "raw");
        final GridCoverageFactory factory = CoverageFactoryFinder.getGridCoverageFactory(hints);
        
        final SampleModel iSampleModel = userRaster.getSampleModel();
		final ColorModel iColorModel = PlanarImage.createColorModel(iSampleModel);
		TiledImage image = new TiledImage(0, 0, userRaster.getWidth(), userRaster.getHeight(), 0, 0, iSampleModel, iColorModel);
		image.setData(userRaster);
		
		Unit<?> uom = null;
		final Category nan;
		final Category values;
		if (Double.isNaN(inNoData)) {
			nan = new Category(
					Vocabulary.formatInternational(VocabularyKeys.NODATA), 
					new Color(0, 0, 0, 0), 0);
			values = new Category("values", new Color[] { new Color(255, 0, 0, 0) }, NumberRange.create(1,255), NumberRange.create(0, 9000));

		} else {
			nan = new Category(
					Vocabulary.formatInternational(VocabularyKeys.NODATA),
					new Color[] { new Color(0, 0, 0, 0) }, 
					NumberRange.create(0, 0),
					NumberRange.create(inNoData, inNoData));
			values = new Category(
					"values", 
					new Color[] { new Color(255, 0, 0, 0) },
					NumberRange.create(1, 255), 
					NumberRange.create(inNoData + Math.abs(inNoData) * 0.1, inNoData + Math.abs(inNoData) * 10)
			);

		}
		
		// ///////////////////////////////////////////////////////////////////
		//
		// Sample dimension
		//
		//
		// ///////////////////////////////////////////////////////////////////
		final GridSampleDimension band = new GridSampleDimension(coverageName, new Category[] { nan, values }, uom).geophysics(true);
		final Map<String, Double> properties = new HashMap<String, Double>();
		properties.put("GC_NODATA", new Double(inNoData));
		
		// /////////////////////////////////////////////////////////////////////
		//
		// Coverage
		//
		// /////////////////////////////////////////////////////////////////////
        GridCoverage coverage = null;
        if (iColorModel != null)
        	coverage = factory.create(varName, image, envelope, new GridSampleDimension[] { band }, null, properties);
        else
        	coverage = factory.create(varName, userRaster, envelope, new GridSampleDimension[] { band });
        
		final AbstractGridCoverageWriter writer = (AbstractGridCoverageWriter) new GeoTiffWriter(outFile);
		writer.write(coverage, (GeneralParameterValue[]) wparams.values().toArray(new GeneralParameterValue[1]));

		// /////////////////////////////////////////////////////////////////////
		//
		// PERFORMING FINAL CLEAN UP AFTER THE WRITE PROCESS
		//
		// /////////////////////////////////////////////////////////////////////
		writer.dispose();
		
		return outFile;
	}
	
	/**
	 * 
	 * @param varDataType
	 * @param width
	 * @param height
	 * @param numBands
	 * @return
	 */
	public static SampleModel getSampleModel(final int width, final int height, final int numBands) {
		final int dataType = DataBuffer.TYPE_DOUBLE;
		return RasterFactory.createBandedSampleModel(
				dataType, //data type
				width, //width
				height, //height
				numBands); //num bands
	}
	
	@Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());
        builder.append(" [");
        if (configuration != null) {
            builder.append("configuration=").append(configuration);
        }
        builder.append("]");
        return builder.toString();
    }
}
