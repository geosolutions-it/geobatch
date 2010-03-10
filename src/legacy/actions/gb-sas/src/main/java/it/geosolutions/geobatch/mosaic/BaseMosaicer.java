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
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geobatch.geotiff.overview.GeoTiffOverviewsEmbedderConfiguration;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageWriteParam;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.MosaicDescriptor;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.gce.geotiff.GeoTiffFormat;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.geotiff.GeoTiffWriteParams;
import org.geotools.gce.geotiff.GeoTiffWriter;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.spatial.PixelTranslation;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;

/**
 * Main Mosaicer class.
 * 
 * @author Daniele Romagnoli, GeoSolutions
 */
public abstract class BaseMosaicer extends BaseAction<FileSystemMonitorEvent> implements Action<FileSystemMonitorEvent> {

    
    private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(BaseMosaicer.class.toString());
    
    /**
     * 
     * @author Simone Giannecchini, GeoSolutions SAS
     *
     */
    private class OverviewsEmbedderTask implements Callable<String> {

    	private String fileName;
    	private GeoTiffOverviewsEmbedderConfiguration gtovConfiguration;
    	
    	public OverviewsEmbedderTask(final String fileOverviews,
				final GeoTiffOverviewsEmbedderConfiguration gtovConfiguration) {
	    		this.fileName = fileOverviews;
	    		try {
					this.gtovConfiguration = gtovConfiguration.clone();
				} catch (CloneNotSupportedException e) {
					throw new RuntimeException("Unable to clone the provided configuration" , e);
				}
	    		
		}
		public String call() throws Exception {
    		try{
    			Utils.addOverviews(fileName, gtovConfiguration);
	    		return fileName;
    		}finally{

        		// decrement latch
        		concurrentLatch.countDown();
    		}
    		
    		
    	}

    }    
	/**
	 * 
	 * @author Simone Giannecchini, GeoSolutions SAS
	 *
	 */
    private class TileWriter implements Callable<String> {
		
		private Rectangle sourceRegion;
		
		private int numTileX;
		
		private int numTileY;
		
		private int tileWidth;
		
		private int tileHeight;
		
		private GridCoverage2D gc;
		
		private String compressionScheme;
		
		private float compressionRatio;

		private int row;

		private int column;

		private String fileName;

		
		public TileWriter(final GridCoverage2D gc, final Rectangle sourceRegion, final int row, final int column,
				final int numTileX, final int numTileY, final int tileWidth, final int tileHeight, 
				final String fileName, final String compressionScheme, final float compressionRatio){
			this.gc = gc;
			this.sourceRegion = sourceRegion;
			this.numTileX = numTileX;
			this.numTileY = numTileY;
			this.tileHeight = tileHeight;
			this.tileWidth = tileWidth;
			this.fileName = fileName;
			this.row = row;
			this.column = column;
			this.compressionRatio = compressionRatio;
			this.compressionScheme = compressionScheme;
		}
		
		public String call() throws Exception {
		    // //
	        //
	        // building gridgeometry for the read operation with the actual
	        // envelope
	        //
	        // //
	        final File fileOut = new File(fileName);
	        // remove an old output file if it exists
	        if (fileOut.exists())
	            fileOut.delete();

	        // //
	        //
	        // Write this coverage out as a geotiff
	        //
	        // //
	        final AbstractGridFormat outFormat = new GeoTiffFormat();
	        GeoTiffWriter writerWI=null;
	        try {

	            final GeoTiffWriteParams wp = new GeoTiffWriteParams();
	            wp.setTilingMode(GeoToolsWriteParams.MODE_EXPLICIT);
	            wp.setTiling(tileWidth, tileHeight);
	            wp.setSourceRegion(sourceRegion);
	            if (compressionScheme != null&& !Double.isNaN(compressionRatio)) {
	                wp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	                wp.setCompressionType(compressionScheme);
	                wp.setCompressionQuality((float) compressionRatio);
	            }
	            final ParameterValueGroup params = outFormat.getWriteParameters();
	            params.parameter(AbstractGridFormat.GEOTOOLS_WRITE_PARAMS.getName().toString()).setValue(wp);

	            if (LOGGER.isLoggable(Level.INFO))
	            	LOGGER.info(new StringBuilder("Writing tile: ").append(column+1).append(" of ")
	            			.append(numTileX).append(" [X] -- ").append(row+1).append(" of ").
	            			append(numTileY).append(" [Y]").toString());
	            
	            writerWI= new GeoTiffWriter(fileOut);
	            writerWI.write(gc, (GeneralParameterValue[]) params.values().toArray(new GeneralParameterValue[1]));
	           
	        } catch (Error e) {
	        	 if (LOGGER.isLoggable(Level.SEVERE))
	             	LOGGER.log(Level.SEVERE,"Exception occurred whilst writing tiles:"+e.getLocalizedMessage(),e );
	        	 final IOException ioe= new IOException();
	        	 ioe.initCause(e);
	        	 throw ioe;
	        }finally{
	        	if(writerWI!=null){
	        		try{
	        			writerWI.dispose();
	        		}catch (Throwable e) {
	        			if (LOGGER.isLoggable(Level.FINEST))
	                     	LOGGER.log(Level.FINEST,"Exception occurred whilst writing tiles:"+e.getLocalizedMessage(),e );
					}
	        	}
	        	

	    		// decrement latch
	    		concurrentLatch.countDown();
	        }
	        
	        return fileName;
		}
	}	

	private  CountDownLatch concurrentLatch;
	
    protected MosaicerConfiguration configuration;

    public BaseMosaicer(MosaicerConfiguration configuration) throws IOException {
        this.configuration = configuration;
    }

    public Queue<FileSystemMonitorEvent> execute(
            Queue<FileSystemMonitorEvent> events) throws Exception {
    	GeoTiffReader reader = null;
    	try {
        	
            // looking for file
            // if (events.size() != 1)
            // throw new IllegalArgumentException(
            // "Wrong number of elements for this action: "
            // + events.size());
            //
            // // get the first event
            // final FileSystemMonitorEvent event = events.peek();
            // final File inputFile = event.getSource();
            //            
            // ////////////////////////////////////////////////////////////////////
            //
            // Checking input files.
            //
            // ////////////////////////////////////////////////////////////////////

            GeneralEnvelope globEnvelope = null;
            double xscale=0.025;
            double yscale=0.015;

            final String directory = configuration.getWorkingDirectory();
            final double compressionRatio = configuration.getCompressionRatio();
            final String compressionType = configuration.getCompressionScheme();
            final int tileW = configuration.getTileW();
            final int tileH = configuration.getTileH();
            final int chunkW = configuration.getChunkWidth();
            final int chunkH = configuration.getChunkHeight();

            final File fileDir = new File(directory);
            if (fileDir != null && fileDir.isDirectory()) {
                final File[] files = fileDir.listFiles();
                
                // //
                //
                // Setting directories hierarchy
                //
                // //
                final String outputDirectory = buildOutputDirName(directory);
                final File dir = new File(outputDirectory);
                configuration.setMosaicDirectory(outputDirectory);

                if (!dir.exists())
                    dir.mkdir();

                if (files != null) {
                    final int numFiles = files.length;
                    for (int i = 0; i < numFiles; i++) {
                        final String path = files[i].getAbsolutePath()
                                .toLowerCase();
                        if (!path.endsWith("tif"))
                            continue;

                        // get a reader
                        final File file = files[i];
                        reader = new GeoTiffReader(file, null);

                        // //
                        //
                        // Updating the global mosaic's envelope
                        //
                        // //
                        GeneralEnvelope envelope = (GeneralEnvelope) reader.getOriginalEnvelope();
                        AffineTransform at = (AffineTransform)reader.getOriginalGridToWorld(PixelInCell.CELL_CENTER);
                        if (globEnvelope == null) {
                            globEnvelope = new GeneralEnvelope(envelope);
                            globEnvelope.setCoordinateReferenceSystem(envelope.getCoordinateReferenceSystem());
                            xscale = XAffineTransform.getScaleX0(at);
                            yscale = XAffineTransform.getScaleY0(at);
                        } else
                            globEnvelope.add(envelope);
                        double tempXscale = XAffineTransform.getScaleX0(at);
                        double tempYscale = XAffineTransform.getScaleY0(at);
                        if ((tempXscale < tempYscale && tempXscale < xscale) ||(tempYscale < tempXscale && tempYscale < yscale)){
                        	yscale = tempYscale;
                    		xscale = tempXscale;
                        }
                        reader.dispose();
                    }

                    // //
                    // computing the final g2w
                    // //
                    final MathTransform world2GridTransform = computeWorldToGridTransform(xscale,yscale,globEnvelope);
                    final GridCoverageFactory coverageFactory = CoverageFactoryFinder.getGridCoverageFactory(null);

                    // read them all
                    final List<GridCoverage2D> coverages = new LinkedList<GridCoverage2D>();
                    
                    final Map<String,File> sortedFiles = sortFilesByPing(files);
                    
                    final Iterator<String> it = sortedFiles.keySet().iterator();
                    while (it.hasNext()){
                        final File file = sortedFiles.get(it.next());
                        try{
	                        reader = new GeoTiffReader(file, null);
	                        final GridCoverage2D gc = (GridCoverage2D) reader.read(null);
	                        coverages.add(gc);
	                        updates(gc);
                        }finally{
                        	if(reader!=null)
                        		try{
                        			reader.dispose();
                        		}catch (Throwable e) {
									if(LOGGER.isLoggable(Level.FINE))
										LOGGER.log(Level.FINE,e.getLocalizedMessage(),e);
								}
                        }
                    }
                    
                    final RenderedImage mosaicImage = createMosaic(coverages,world2GridTransform);
                    final RenderedImage balancedMosaic = processMosaic(mosaicImage);
                    
                    final GridCoverage2D balancedGc = coverageFactory.create("balanced", balancedMosaic, globEnvelope);
                    if (LOGGER.isLoggable(Level.INFO))
                    	LOGGER.info("Retiling the balanced mosaic");
                    
                    retileMosaic(balancedGc, chunkW, chunkH, tileW, tileH, compressionRatio, compressionType, outputDirectory);
                }
            }

            return events;
        } catch (Throwable t) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, t.getLocalizedMessage(), t);
            return null;
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

    /**
     * Setup a proper global WorldToGrid transformation
     * @param xscale
     * @param yscale
     * @param globEnvelope
     * @return
     * @throws NoninvertibleTransformException
     */
    private static MathTransform computeWorldToGridTransform(
    		final double xscale, 
    		final double yscale, 
    		final GeneralEnvelope globEnvelope) throws NoninvertibleTransformException {
    	
    	final GeneralMatrix gm = new GeneralMatrix(3);
        
        gm.setElement(0, 0, xscale);
        gm.setElement(1, 1, -yscale);
        gm.setElement(0, 1, 0);
        gm.setElement(1, 0, 0);
        gm.setElement(0, 2, globEnvelope.getLowerCorner().getOrdinate(0));
        gm.setElement(1, 2, globEnvelope.getUpperCorner().getOrdinate(1));
        final MathTransform mosaicTransform = ProjectiveTransform.create(gm);
        final MathTransform tempTransform = PixelTranslation.translate(mosaicTransform, PixelInCell.CELL_CORNER, PixelInCell.CELL_CENTER);
        
       return tempTransform.inverse();
	}

    /**
     * Sort files by ping
     * @param files
     * @return
     */
	private static Map<String,File> sortFilesByPing(final File[] files) {
        final Map<String,File> treeMap = new TreeMap<String, File>(java.util.Collections.reverseOrder());
        final DecimalFormat nf = new DecimalFormat("0000000000");
        
        for (File file : files) {
            final String path = file.getAbsolutePath().toLowerCase();
            if (!path.endsWith("tif"))
                continue;
            
            final String name = file.getName();
            final String[] dashes = name.split("_");
            
            //TODO: Files are always in the form as depicted by Francesco: 
            // MUSCLE_COL2_090316_1_1_p_2_143_40_150
            // Improve this ordering logic, leveraging on metadata
            
            final String number = nf.format(Integer.parseInt(dashes[6]));
            treeMap.put(number,file);
        } 
        return treeMap;
    }

    /** Update some internal machinery to optimize balancing computations */
    protected abstract void updates(GridCoverage2D gc);

    protected abstract RenderedImage processMosaic (RenderedImage ri);
    
    protected abstract String buildOutputDirName(String directory) ;

    /**
     * Create the ImageMosaic from the list of coverages, using the provided 
     * World2Grid transformation to place them on the image
     * @param coverages
     * @param world2GridTransform
     * @return the produced Mosaic
     */
    private static RenderedImage createMosaic(
            final List<GridCoverage2D> coverages,
            final MathTransform world2GridTransform) {
        final int nCov = coverages.size();

        final ParameterBlockJAI pbMosaic = new ParameterBlockJAI("Mosaic");
        pbMosaic.setParameter("mosaicType", MosaicDescriptor.MOSAIC_TYPE_OVERLAY);

        if (LOGGER.isLoggable(Level.INFO))
        	LOGGER.info(new StringBuffer("Found ").append(nCov).append(" tiles").toString());
        
        // applying an affine transform to the single granules
        for (int i = 0; i < nCov; i++) {
            final GridCoverage2D coverage = coverages.get(i);
            final ParameterBlockJAI pbAffine = new ParameterBlockJAI("Affine");
            pbAffine.addSource(coverage.getRenderedImage());
            final AffineTransform at = (AffineTransform) coverage.getGridGeometry().getGridToCRS2D();
            AffineTransform chained = (AffineTransform) at.clone();
            chained.preConcatenate((AffineTransform) world2GridTransform);
            pbAffine.setParameter("transform", chained);
            final RenderedOp affine = JAI.create("Affine", pbAffine);
            pbMosaic.addSource(affine);
        }

        // building up the final mosaic
        final RenderedOp mosaicImage = JAI.create("Mosaic", pbMosaic);
        return mosaicImage;
    }

    /**
     * Retile the produced mosaic due to the TIFF Files size limit.
     * Also internally retile the images
     * 
     * @param gc
     * @param chunkWidth
     * @param chunkHeight
     * @param internalTileWidth
     * @param internalTileHeight
     * @param compressionRatio
     * @param compressionScheme
     * @param outputLocation
     */
    private void retileMosaic(
    		final GridCoverage2D gc, 
    		int chunkWidth,
    		int chunkHeight, 
    		final int internalTileWidth, 
    		final int internalTileHeight,
            final double compressionRatio, 
            final String compressionScheme,
            final String outputLocation) {

        // //
        //
        // getting source size and checking tile dimensions to be not
        // bigger than the original coverage size
        //
        // //
        final RenderedImage rImage = gc.getRenderedImage();
        final int w = rImage.getWidth();
        final int h = rImage.getHeight();
        chunkWidth = chunkWidth > w ? w : chunkWidth;
        chunkHeight = chunkHeight > h ? h : chunkHeight;

        // ///////////////////////////////////////////////////////////////////
        //
        // MAIN LOOP
        //
        // ///////////////////////////////////////////////////////////////////
        if (LOGGER.isLoggable(Level.INFO))
        	LOGGER.info( "Retiling mosaic to separated files");
        final int numTileX = w!=chunkWidth? (int) (w / (chunkWidth * 1.0) + 1):1;
        final int numTileY = h!=chunkHeight? (int) (h / (chunkHeight * 1.0) + 1):1;
        final List<String> filesToAddOverviews = new ArrayList<String>(numTileX*numTileY);
        
        // ///////////////////////////////////////////////////////////////////
        //
        // RETILING MOSAIC
        //
        // ///////////////////////////////////////////////////////////////////
        
        // //
        //
        // Creating the thread pool using the configuration parameters.
        //
        // //
        final int corePoolSize = configuration.getCorePoolSize();
        final int maxPoolSize = configuration.getMaxPoolSize();
        final long keepAliveTime = configuration.getMaxWaitingTime();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        executor.prestartAllCoreThreads();
        
        // tasks that we are going to submit to the thread pool.
        final List<Future<String>> tasks= new ArrayList<Future<String>>();
        
        
        // //
        //
        // Queueing tasks for execution
        //
        // //
        // number of tiles to create
        final int numTiles = numTileX*numTileY;
        // set the latch so that we wait for the taks to complete
        concurrentLatch= new CountDownLatch(numTiles);
        
        boolean terminated = false;
        for (int tileIndex = 0; tileIndex < numTiles; tileIndex++) {
        	final int row = tileIndex/numTileX;
    		final int column = tileIndex%numTileX;
        	final String fileName = buildFileName(outputLocation,row,column,chunkWidth);
        	final Rectangle sourceRegion = new Rectangle(column * chunkWidth, row * chunkHeight, chunkWidth, chunkHeight);
        	if(sourceRegion.isEmpty())
        	{
        		LOGGER.warning("Empty regione when writing down mosaic tile:"+sourceRegion);
        		continue;
        	}
        	// create task as a future
        	final Future<String> task = executor.submit(
        				new TileWriter(
        						gc, 
        						sourceRegion, 
        						row, 
        						column, 
        						numTileX, 
        						numTileY, 
        						internalTileWidth, 
        						internalTileHeight, 
        						fileName,
        						compressionScheme, 
        						(float)compressionRatio));
        	tasks.add(task);
        	
        	// contextually create the overview tasks queue
        	filesToAddOverviews.add(fileName);
        }
        
        // execute the tasks we have queued
        try {
        	terminated = concurrentLatch.await(keepAliveTime, TimeUnit.SECONDS);

//        	executor.shutdown();
//			terminated = executor.awaitTermination(keepAliveTime, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			if(LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING,e.getLocalizedMessage(),e);
			
		} 
		
    	if(!terminated)
    	{
    		if(LOGGER.isLoggable(Level.SEVERE))
    				LOGGER.severe("Writing down mosaic in tiles timed out!!!");
    		return;
    	}
        
        // ///////////////////////////////////////////////////////////////////
        //
        // ADDING OVERVIEWS
        //
        // ///////////////////////////////////////////////////////////////////
		
        //Overviews are added as a last step to minimize TileCache updates
		final List<Future<String>> overviewsTasks= new ArrayList<Future<String>>();
//    	executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize ,keepAliveTime,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());
		
        int nOverviewsDone = 1;
        concurrentLatch= new CountDownLatch(numTiles);
        final GeoTiffOverviewsEmbedderConfiguration gtovConfiguration = initGeotiffOverviewsEmbedderConfiguration();        
        
        for (String fileOverviews: filesToAddOverviews){
            // TODO: Leverage on GeoTiffOverviewsEmbedder when involving
            // no more FileSystemEvent only
            // Or merge retiling and overviews adding to a single step
            if (LOGGER.isLoggable(Level.INFO))
            	LOGGER.info( new StringBuilder("Adding overviews: File ").append(nOverviewsDone).append(" of ").append(numTiles).toString());
            nOverviewsDone++;
            final Future<String> task = executor.submit(new OverviewsEmbedderTask(fileOverviews, gtovConfiguration));
            overviewsTasks.add(task);
        }
        
//        try {
//        	executor.shutdown();
//			terminated = executor.awaitTermination(keepAliveTime, TimeUnit.SECONDS);
//		} catch (InterruptedException e) {
//			//TODO: Handle this
//			
//		} finally{
//			
//			if (!terminated)
//				executor.shutdownNow();
//			//TODO: Check threads terminated
//			executor = null;
//		}
		
        // execute the tasks we have queued
        try {
        	terminated = concurrentLatch.await(keepAliveTime, TimeUnit.SECONDS);

//        	executor.shutdown();
//			terminated = executor.awaitTermination(keepAliveTime, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			if(LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING,e.getLocalizedMessage(),e);
			
		} 
		
    	if(!terminated)
    	{
    		if(LOGGER.isLoggable(Level.SEVERE))
    				LOGGER.severe("Writing down mosaic in tiles timed out!!!");
    		return;
    	}
    }

    protected abstract String buildFileName(String outputLocation, int i, int j,
            int chunkWidth) ;

    public ActionConfiguration getConfiguration() {
        return configuration;
    }
    
    private GeoTiffOverviewsEmbedderConfiguration initGeotiffOverviewsEmbedderConfiguration() {
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
    
}
