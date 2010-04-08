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

package it.geosolutions.geobatch.convert;

import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geotiff.overview.GeoTiffOverviewsEmbedderConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.Interpolation;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.UnknownFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 * Comments here ...
 * 
 * @author AlFa
 * 
 * @version $ DetectionManagerConfiguratorAction.java $ Revision: 0.1 $ 12/feb/07 12:07:06
 */
@SuppressWarnings("deprecation")
public abstract class FormatConverterConfiguratorAction<T extends EventObject>
	extends BaseAction<T>
	implements Action<T> {
	/**
     * Default logger
     */
    protected final static Logger LOGGER = Logger.getLogger(FormatConverterConfiguratorAction.class.toString());

    protected final FormatConverterConfiguration configuration;

    protected final static Map<String, Format> formats= new HashMap<String, Format>();
    
    static{
    	GridFormatFinder.scanForPlugins();
        final Format[] formatsArray = GridFormatFinder.getFormatArray();
        if(formatsArray!=null)
        	for(Format f:formatsArray)
        		formats.put(f.getName(), f);
    }

    /**
     * Constructs a producer.
	 * The operation name will be the same than the parameter descriptor name.
     *
     * @throws IOException
     */
    public FormatConverterConfiguratorAction(FormatConverterConfiguration configuration) {
		super(configuration);
        this.configuration = configuration;
    }

    /**
     * 
     * @return
     */
    public FormatConverterConfiguration getConfiguration() {
        return configuration;
    }

    protected GeoTiffOverviewsEmbedderConfiguration initGeotiffOverviewsEmbedderConfiguration() {
    	final GeoTiffOverviewsEmbedderConfiguration gtovConfiguration = new GeoTiffOverviewsEmbedderConfiguration();
    	gtovConfiguration.setDownsampleStep(configuration.getDownsampleStep());
    	gtovConfiguration.setNumSteps(configuration.getNumSteps());
    	gtovConfiguration.setScaleAlgorithm(configuration.getScaleAlgorithm());
    	gtovConfiguration.setCompressionScheme(configuration.getCompressionScheme());
    	gtovConfiguration.setCompressionRatio(configuration.getCompressionRatio());
    	gtovConfiguration.setInterp(Interpolation.INTERP_NEAREST);
    	gtovConfiguration.setTileW(configuration.getTileW());
    	gtovConfiguration.setTileH(configuration.getTileH());
    	gtovConfiguration.setLogNotification(false);
    	return gtovConfiguration;
		
	}

    /**
     * Convert the specified file and write it to the specified output file name.
     * 
     * @param file
     * @param outputFile
     * @throws IllegalArgumentException
     * @throws IOException
     */	
    protected boolean convert(final File file, final File outputFile)
            throws IllegalArgumentException, IOException {

    	boolean converted = false;
        // //
        //
        // Getting a GridFormat
        // 
        // //
        final AbstractGridFormat gridFormat = (AbstractGridFormat) GridFormatFinder.findFormat(file);
        if (gridFormat != null && !(gridFormat instanceof UnknownFormat)) {

            final int tileW = configuration.getTileW();
            final int tileH = configuration.getTileH();
           
            // //
            //
            // Reading the coverage
            // 
            // //
            GridCoverageReader reader = null;
            try{
            	reader = gridFormat.getReader(file, null);
	            final GridCoverage2D gc = (GridCoverage2D) reader.read(null);
	            final String outputFormatType = configuration.getOutputFormat();
	
	            // //
	            // Acquire required writer
	            // //
	            final AbstractGridFormat writerFormat = (AbstractGridFormat) acquireFormatByName(outputFormatType);
	
	            if (!(writerFormat instanceof UnknownFormat)) {
	                GridCoverageWriter writer = writerFormat.getWriter(outputFile);
	
	                 GeoToolsWriteParams params = null;
	                ParameterValueGroup wparams = null;
	                try {
	                    wparams = writerFormat.getWriteParameters();
	                    params = writerFormat.getDefaultImageIOWriteParameters();
	                } catch (UnsupportedOperationException uoe) {
	                    params = null;
	                    wparams = null;
	                }
	                if (params != null) {
	                    params.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
	                    params.setTiling(tileW, tileH);
	                    wparams.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName()
	                                    .toString()).setValue(params);
	                }
	
	                // //
	                //
	                // Write the converted coverage
	                //
	                // //
	                try{
	                	final GeneralParameterValue[] writeParams = wparams != null ? (GeneralParameterValue[]) wparams.values().toArray(new GeneralParameterValue[1]): null;
		                writer.write(gc,writeParams);
	                }finally{
	                	try{
	    	                writer.dispose();
	                	}catch (Throwable e) {
							// eat me
						}
	                	
	                	gc.dispose(true);
	                	
	                	try{
			                reader.dispose();
	                	}catch (Throwable e) {
							// eat me
						}
	                }
	                converted = true;
	                
	            }
	            else{
	            	if (LOGGER.isLoggable(Level.WARNING))
	            		LOGGER.warning("No Writer found for this format: " + outputFormatType);
	            }
            } catch (Throwable t){
            	if (LOGGER.isLoggable(Level.SEVERE))
            		LOGGER.severe(t.getLocalizedMessage());
            } finally {
            	 if (reader != null) {
                     try {
                         reader.dispose();
                     } catch (Throwable e) {
                         if (LOGGER.isLoggable(Level.FINEST))
                             LOGGER.log(Level.FINEST, e.getLocalizedMessage(), e);
                     }
                 }
            }
        }
        return converted;
    }

    /**
     * Get a proper {@link Format} for the requested format name.
     * 
     * @param formatName
     * @return the proper instance of {@link Format} or an {@link UnknownFormat} 
     * instance in case no format is found.
     */
    protected static Format acquireFormatByName(final String formatName) {
    	// TODO: formats are now statically initialized: Check it
    	
        if(formats.containsKey(formatName))
        	return formats.get(formatName);
        
        // we did not find it
        return new UnknownFormat();
    }
    
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DetectionManagerConfiguratorAction [");
		if (configuration != null)
			builder.append("configuration=").append(configuration);
		builder.append("]");
		return builder.toString();
	}

}
