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

package it.geosolutions.geobatch.mosaic;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.base.Utils;
import it.geosolutions.geobatch.flow.event.action.Action;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;

import org.geotools.coverage.grid.GridCoverage2D;

/**
 * Comments here ...
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public class Mosaicer extends BaseMosaicer implements Action<FileSystemMonitorEvent> {

    private final static boolean IMAGE_IS_LINEAR;

    static{
        final String cl = System.getenv("SAS_COMPUTE_LOG");
        if (cl!=null && cl.trim().length()>0)
        	IMAGE_IS_LINEAR = !Boolean.parseBoolean(cl);
        else 
        	IMAGE_IS_LINEAR = false;
    }
    
    public static final String MOSAIC_PREFIX = "balm_";
    
    private double extrema[] = new double[]{Double.MAX_VALUE,Double.MIN_VALUE} ;

    public Mosaicer(MosaicerConfiguration configuration) throws IOException {
        super(configuration);
    }


    protected void updates(GridCoverage2D gc) {
        RenderedImage sourceImage = gc.getRenderedImage();
        if (IMAGE_IS_LINEAR){
            sourceImage = computeLog(sourceImage);
        }
        
        final ROI roi = new ROI(sourceImage, 0);
        final ParameterBlock pb = new ParameterBlock();
        pb.addSource(sourceImage); // The source image
        if (roi != null)
            pb.add(roi); // The region of the image to scan

        // Perform the extrema operation on the source image
        final RenderedOp ex = JAI.create("extrema", pb);

        // Retrieve both the maximum and minimum pixel value
        final double[][] ext = (double[][]) ex.getProperty("extrema");
        
        if(extrema[0]>ext[0][0])
            extrema[0]=ext[0][0];
        if (extrema[1]<ext[1][0])
            extrema[1]=ext[1][0];
    }

    private RenderedImage computeLog(RenderedImage sourceImage) {
        final ParameterBlockJAI pbLog = new ParameterBlockJAI("Log");
        pbLog.addSource(sourceImage);
        RenderedOp logarithm = JAI.create("Log", pbLog);

        // //
        //
        // Applying a rescale to handle Decimal Logarithm.
        //
        // //
        final ParameterBlock pbRescale = new ParameterBlock();
        
        // Using logarithmic properties 
        final double scaleFactor = 20 / Math.log(10);

        final double[] scaleF = new double[] { scaleFactor };
        final double[] offsetF = new double[] { 0 };

        pbRescale.add(scaleF);
        pbRescale.add(offsetF);
        pbRescale.addSource(logarithm);

        return JAI.create("Rescale", pbRescale);
    }

    protected RenderedImage processMosaic(RenderedImage mosaicImage) {
        RenderedImage inputImage = mosaicImage;
        if (IMAGE_IS_LINEAR){
            inputImage = computeLog(inputImage);
        }
        
        final double[] scale = new double[] { (255) / (extrema[1] - extrema[0]) };
        final double[] offset = new double[] { ((255) * extrema[0])/ (extrema[0] - extrema[1]) };

        // Preparing to rescaling values
        final ParameterBlock pbRescale = new ParameterBlock();
        pbRescale.add(scale);
        pbRescale.add(offset);
        pbRescale.addSource(inputImage);
        RenderedOp rescaledImage = JAI.create("Rescale", pbRescale);

        final ParameterBlock pbConvert = new ParameterBlock();
        pbConvert.addSource(rescaledImage);
        pbConvert.add(DataBuffer.TYPE_BYTE);
        RenderedOp destImage = JAI.create("format", pbConvert);
        
        return destImage;
    }
    
    /**
     * 
     * @param outputLocation
     * @return
     */
    protected String buildOutputDirName(final String outputLocation){
    	return Utils.buildRunName(outputLocation, configuration.getTime(), MOSAIC_PREFIX);
    }
    
    protected String buildFileName(final String outputLocation, final int i,
			final int j, final int chunkWidth) {
		final String name = new StringBuilder(outputLocation).append(
				Utils.SEPARATOR).append("m_").append(
				Integer.toString(i * chunkWidth + j)).append(".").append("tif")
				.toString();
		return name;
	}
}
