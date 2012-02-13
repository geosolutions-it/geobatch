/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2011 GeoSolutions S.A.S.
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
package it.geosolutions.geobatch.imagemosaic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import com.thoughtworks.xstream.InitializationException;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * 
 * A serializable bean used to set the ImageMosaic command list.
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @note: on 28 Feb 2011 discussion: carlo: do we need to clean this bean from
 *        Xstream specific methods and annotations? simone: leave it here since
 *        this class is actually used only by geobatch
 * @note: This is public to make it usable from other packages
 * @see metoc actions
 */

@XStreamInclude(ImageMosaicCommand.class)
@XStreamAlias("ImageMosaic")
public class ImageMosaicCommand implements Serializable {

	/**
	 * Serial version id
	 */
	@XStreamOmitField
	private static final long serialVersionUID = 7592430220578935089L;

	@XStreamAlias("base")
	private File baseDir;

	@XStreamImplicit(itemFieldName = "del")
	private List<File> delFiles;

	@XStreamImplicit(itemFieldName = "add")
	private List<File> addFiles;

	@XStreamOmitField
	private static XStream stream;
	static {
		init();
	}
	
	public ImageMosaicCommand(){
		init();
	}

	/**
	 * initialize the XStream env
	 * 
	 * @throws InitializationException
	 *             - in case of an initialization problem
	 */
	private static void init() throws InitializationException {
		stream = new XStream();
		stream.processAnnotations(ImageMosaicCommand.class);
	}

	/**
	 * Try to deserialize the command, return null if some goes wrong
	 * 
	 * @param file
	 *            the file to deserialize
	 * @return the deserialized ImageMosaicCommand object or null
	 * @throws FileNotFoundException
	 *             - if the file exists but is a directory rather than a regular
	 *             file, does not exist but cannot be created, or cannot be
	 *             opened for any other reason
	 * @throws SecurityException
	 *             - if a security manager exists and its checkWrite method
	 *             denies write access to the file
	 * @throws XStreamException
	 *             - if the object cannot be serialized
	 */
	public static File serialize(ImageMosaicCommand cmd, String path)
			throws FileNotFoundException, SecurityException {
		final File outFile = new File(path);
		final FileOutputStream fos = new FileOutputStream(outFile);
		if (stream == null)
			init();
		stream.toXML(cmd, fos);
		return outFile;
	}

	/**
	 * Try to deserialize the command, return null if some goes wrong
	 * 
	 * @param file
	 *            the file to deserialize
	 * @return the deserialized ImageMosaicCommand object or null
	 * @throws FileNotFoundException
	 *             - if the file exists but is a directory rather than a regular
	 *             file, does not exist but cannot be created, or cannot be
	 *             opened for any other reason
	 * @throws SecurityException
	 *             - if a security manager exists and its checkWrite method
	 *             denies write access to the file
	 * @throws XStreamException
	 *             - if the object cannot be serialized
	 */
	public static ImageMosaicCommand deserialize(File file)
			throws FileNotFoundException, SecurityException {
		// try {
		final InputStream is = new FileInputStream(file);
		if (stream == null)
			init();
		final ImageMosaicCommand cmd = (ImageMosaicCommand) stream.fromXML(is);
		return cmd;
		// } catch (XSException e) {
		// // LOGGER.trace(e.getMessage(), e);
		// e.printStackTrace();
		// } catch (FileNotFoundException e) {
		// // LOGGER.trace(e.getMessage(), e);
		// e.printStackTrace();
		// }
		// return null;
	}

	public ImageMosaicCommand(final File baseDir, final List<File> addFiles,
			final List<File> delFiles) {
		super();
		this.baseDir = baseDir;
		this.addFiles = addFiles;
		this.delFiles = delFiles;
	}

	public ImageMosaicCommand(final String baseDir,
			final List<String> addFiles, final List<String> delFiles) {
		super();
		this.baseDir = new File(baseDir);
		if (addFiles != null) {
			this.addFiles = new ArrayList<File>();
			for (String fileName : addFiles) {
				this.addFiles.add(new File(fileName));
			}
		}
		if (delFiles != null) {
			this.delFiles = new ArrayList<File>();
			for (String fileName : delFiles) {
				this.delFiles.add(new File(fileName));
			}
		}
	}

	public File getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	public List<File> getAddFiles() {
		return addFiles;
	}

	public void setAddFiles(List<File> addFiles) {
		this.addFiles = addFiles;
	}

	public List<File> getDelFiles() {
		return delFiles;
	}

	public void setDelFiles(List<File> delFiles) {
		this.delFiles = delFiles;
	}

	@Override
	public String toString() {
		if (stream == null)
			init();

		return stream.toXML(this);
		// "ImageMosaicCommand [baseDir=" + baseDir + ", addFiles=" + addFiles +
		// ", delFiles="
		// + delFiles + "]";
	}

	/**
	 * clone
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		
		try {
			return BeanUtils.cloneBean(this);
		} catch (IllegalAccessException e) {
			throw new CloneNotSupportedException(e.getLocalizedMessage());
		} catch (InstantiationException e) {
			throw new CloneNotSupportedException(e.getLocalizedMessage());
		} catch (InvocationTargetException e) {
			throw new CloneNotSupportedException(e.getLocalizedMessage());
		} catch (NoSuchMethodException e) {
			throw new CloneNotSupportedException(e.getLocalizedMessage());
		}
//		List<File> addList = null;
//		List<File> getAddList = this.getAddFiles();
//		if (getAddList != null) {
//			addList = new ArrayList<File>();
//			for (File add : getAddList) {
//				addList.add(new File(add.getAbsolutePath()));
//			}
//		}
//
//		List<File> delList = null;
//		List<File> getDelList = this.getDelFiles();
//		if (getDelList != null) {
//			delList = new ArrayList<File>();
//			for (File del : getDelList) {
//				delList.add(new File(del.getAbsolutePath()));
//			}
//		}
//		// TODO clone
//
//		ImageMosaicCommand cmd = new ImageMosaicCommand(new File(this
//				.getBaseDir().getAbsolutePath()), addList, delList);
//
//		return cmd;
	}

	/*
	 * Carlo added on 26Jul2011 due to: #110 ImageMosaicAction - Should be able
	 * to set a different mosaic conf using incoming the ImageMosaicCommand
	 */
	/**
	 * override passed ImageMosaicConfiguration with values from this instance.
	 * 
	 */
	public void overrideImageMosaicConfiguration(
			final ImageMosaicConfiguration conf) {
		
		if (defaultStyle!=null)
			conf.setDefaultStyle(defaultStyle);

		if (crs != null)
			conf.setCrs(getCrs());

		// wins the one which set different from default (false)
		if ((allowMultithreading != false)
				|| (conf.isAllowMultithreading() != false))
			conf.setAllowMultithreading(true);

		if (backgroundValue != null)
			conf.setBackgroundValue(getBackgroundValue());

		if (datastorePropertiesPath != null)
			conf.setDatastorePropertiesPath(getDatastorePropertiesPath());

		if (elevationPresentationMode != null)
			conf.setElevationRegex(getElevationRegex());

		if (elevDimEnabled != null)
			conf.setElevDimEnabled(getElevDimEnabled());
		if (elevationPresentationMode != null)
			conf.setElevationPresentationMode(getElevationPresentationMode());
		if (inputTransparentColor != null)
			conf.setInputTransparentColor(getInputTransparentColor());
		if (latLonMaxBoundingBoxX != null)
			conf.setLatLonMaxBoundingBoxX(getLatLonMaxBoundingBoxX());
		if (latLonMaxBoundingBoxY != null)
			conf.setLatLonMaxBoundingBoxY(getLatLonMaxBoundingBoxY());
		if (latLonMinBoundingBoxX != null)
			conf.setLatLonMinBoundingBoxX(getLatLonMinBoundingBoxX());
		if (latLonMinBoundingBoxY != null)
			conf.setLatLonMinBoundingBoxY(getLatLonMinBoundingBoxY());
		if (NativeMaxBoundingBoxX != null)
			conf.setNativeMaxBoundingBoxX(getNativeMaxBoundingBoxX());
		if (NativeMaxBoundingBoxY != null)
			conf.setNativeMaxBoundingBoxY(getNativeMaxBoundingBoxY());
		if (NativeMinBoundingBoxX != null)
			conf.setNativeMinBoundingBoxX(getNativeMinBoundingBoxX());
		if (NativeMinBoundingBoxY != null)
			conf.setNativeMinBoundingBoxY(getNativeMinBoundingBoxY());
		if (outputTransparentColor != null)
			conf.setOutputTransparentColor(getOutputTransparentColor());
		if (projectionPolicy != null)
			conf.setProjectionPolicy(getProjectionPolicy());
		if (runtimeRegex != null)
			conf.setRuntimeRegex(getRuntimeRegex());

		// wins the one which set different from default (0)
		if (tileSizeH != 0)
			conf.setTileSizeH(getTileSizeH());

		// wins the one which set different from default (0)
		if ((tileSizeW != 0))
			conf.setTileSizeW(getTileSizeW());

		if (timeDimEnabled != null)
			conf.setTimeDimEnabled(getTimeDimEnabled());
		if (timePresentationMode != null)
			conf.setTimePresentationMode(getTimePresentationMode());
		if (timeRegex != null)
			conf.setTimeRegex(getTimeRegex());

		// wins the one which set different from default (false)
		if ((useJaiImageRead != false) || (conf.isUseJaiImageRead() != false))
			conf.setUseJaiImageRead(true);
	}

	// from GeoserverActionConfiguration
	private String crs;
	private List<String> styles;
	private String defaultStyle;

	// from ImageMosaicActionConfiguration
	private String datastorePropertiesPath;
	private String timeRegex;
	private String elevationRegex;
	private String runtimeRegex;
	private String backgroundValue;// NoData
	private String projectionPolicy;// NONE, REPROJECT_TO_DECLARED,
									// FORCE_DECLARED
	private Double NativeMinBoundingBoxX;// BoundingBox
	private Double NativeMinBoundingBoxY;// BoundingBox
	private Double NativeMaxBoundingBoxX;// BoundingBox
	private Double NativeMaxBoundingBoxY;// BoundingBox
	private Double latLonMinBoundingBoxX;// BoundingBox
	private Double latLonMinBoundingBoxY;// BoundingBox
	private Double latLonMaxBoundingBoxX;// BoundingBox
	private Double latLonMaxBoundingBoxY;// BoundingBox
	private String outputTransparentColor;
	private String inputTransparentColor;
	private boolean allowMultithreading;
	private boolean useJaiImageRead;
	private int tileSizeH;
	private int tileSizeW;
	private String timeDimEnabled;
	private String elevDimEnabled;
	private String timePresentationMode;
	private String elevationPresentationMode;

    public String getDefaultStyle() {
        return defaultStyle;
    }

    public void setDefaultStyle(String defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}

	/**
	 * @return the styles
	 */
	public List<String> getStyles() {
		return styles;
	}

	/**
	 * @param styles
	 *            the styles to set
	 */
	public void setStyles(List<String> styles) {
		this.styles = styles;
	}

	public void addStyle(String style) {
		if (this.styles == null) {
			this.styles = new ArrayList<String>();
		}
		this.styles.add(style);
	}

	public String getProjectionPolicy() {
		return projectionPolicy;
	}

	public void setProjectionPolicy(String projectionPolicy) {
		this.projectionPolicy = projectionPolicy;
	}

	public String getElevDimEnabled() {
		return elevDimEnabled;
	}

	public void setElevDimEnabled(String elevationDimEnabled) {
		this.elevDimEnabled = elevationDimEnabled;
	}

	public String getElevationPresentationMode() {
		return elevationPresentationMode;
	}

	public void setElevationPresentationMode(String elevationPresentationMode) {
		this.elevationPresentationMode = elevationPresentationMode;
	}

	public Double getNativeMinBoundingBoxX() {
		return NativeMinBoundingBoxX;
	}

	public void setNativeMinBoundingBoxX(Double nativeMinBoundingBoxX) {
		NativeMinBoundingBoxX = nativeMinBoundingBoxX;
	}

	public Double getNativeMinBoundingBoxY() {
		return NativeMinBoundingBoxY;
	}

	public void setNativeMinBoundingBoxY(Double nativeMinBoundingBoxY) {
		NativeMinBoundingBoxY = nativeMinBoundingBoxY;
	}

	public Double getNativeMaxBoundingBoxX() {
		return NativeMaxBoundingBoxX;
	}

	public void setNativeMaxBoundingBoxX(Double nativeMaxBoundingBoxX) {
		NativeMaxBoundingBoxX = nativeMaxBoundingBoxX;
	}

	public Double getNativeMaxBoundingBoxY() {
		return NativeMaxBoundingBoxY;
	}

	public void setNativeMaxBoundingBoxY(Double nativeMaxBoundingBoxY) {
		NativeMaxBoundingBoxY = nativeMaxBoundingBoxY;
	}

	public Double getLatLonMinBoundingBoxX() {
		return latLonMinBoundingBoxX;
	}

	public void setLatLonMinBoundingBoxX(Double latLonMinBoundingBoxX) {
		this.latLonMinBoundingBoxX = latLonMinBoundingBoxX;
	}

	public Double getLatLonMinBoundingBoxY() {
		return latLonMinBoundingBoxY;
	}

	public void setLatLonMinBoundingBoxY(Double latLonMinBoundingBoxY) {
		this.latLonMinBoundingBoxY = latLonMinBoundingBoxY;
	}

	public Double getLatLonMaxBoundingBoxX() {
		return latLonMaxBoundingBoxX;
	}

	public void setLatLonMaxBoundingBoxX(Double latLonMaxBoundingBoxX) {
		this.latLonMaxBoundingBoxX = latLonMaxBoundingBoxX;
	}

	public Double getLatLonMaxBoundingBoxY() {
		return latLonMaxBoundingBoxY;
	}

	public void setLatLonMaxBoundingBoxY(Double latLonMaxBoundingBoxY) {
		this.latLonMaxBoundingBoxY = latLonMaxBoundingBoxY;
	}

	public String getTimeDimEnabled() {
		return timeDimEnabled;
	}

	public void setTimeDimEnabled(String timeDimEnabled) {
		this.timeDimEnabled = timeDimEnabled;
	}

	public String getTimePresentationMode() {
		return timePresentationMode;
	}

	public void setTimePresentationMode(String timePresentationMode) {
		this.timePresentationMode = timePresentationMode;
	}

	public String getOutputTransparentColor() {
		return outputTransparentColor;
	}

	public void setOutputTransparentColor(String outputTransparentColor) {
		this.outputTransparentColor = outputTransparentColor;
	}

	public String getInputTransparentColor() {
		return inputTransparentColor;
	}

	public void setInputTransparentColor(String inputTransparentColor) {
		this.inputTransparentColor = inputTransparentColor;
	}

	public boolean isAllowMultithreading() {
		return allowMultithreading;
	}

	public void setAllowMultithreading(boolean allowMultithreading) {
		this.allowMultithreading = allowMultithreading;
	}

	public boolean isUseJaiImageRead() {
		return useJaiImageRead;
	}

	public void setUseJaiImageRead(boolean useJaiImageRead) {
		this.useJaiImageRead = useJaiImageRead;
	}

	public int getTileSizeH() {
		return tileSizeH;
	}

	public void setTileSizeH(int tileSizeH) {
		this.tileSizeH = tileSizeH;
	}

	public int getTileSizeW() {
		return tileSizeW;
	}

	public void setTileSizeW(int tileSizeW) {
		this.tileSizeW = tileSizeW;
	}

	public String getBackgroundValue() {
		return backgroundValue;
	}

	public void setBackgroundValue(String backgroundValue) {
		this.backgroundValue = backgroundValue;
	}

	/**
	 * @param datastorePropertiesPath
	 *            the datastorePropertiesPath to set
	 */
	public void setDatastorePropertiesPath(String datastorePropertiesPath) {
		this.datastorePropertiesPath = datastorePropertiesPath;
	}

	/**
	 * @return the datastorePropertiesPath
	 */
	public String getDatastorePropertiesPath() {
		return datastorePropertiesPath;
	}

	/**
	 * @param timeRegex
	 *            the timeRegex to set
	 */
	public void setTimeRegex(String timeRegex) {
		this.timeRegex = timeRegex;
	}

	/**
	 * @return the timeRegex
	 */
	public String getTimeRegex() {
		return timeRegex;
	}

	/**
	 * @param elevationRegex
	 *            the elevationRegex to set
	 */
	public void setElevationRegex(String elevationRegex) {
		this.elevationRegex = elevationRegex;
	}

	/**
	 * @return the elevationRegex
	 */
	public String getElevationRegex() {
		return elevationRegex;
	}

	/**
	 * @param runtimeRegex
	 *            the runtimeRegex to set
	 */
	public void setRuntimeRegex(String runtimeRegex) {
		this.runtimeRegex = runtimeRegex;
	}

	/**
	 * @return the runtimeRegex
	 */
	public String getRuntimeRegex() {
		return runtimeRegex;
	}

}
