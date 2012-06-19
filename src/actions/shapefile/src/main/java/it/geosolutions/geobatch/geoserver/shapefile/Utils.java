package it.geosolutions.geobatch.geoserver.shapefile;

import org.geotools.data.shapefile.ShapefileDataStoreFactory;

final class Utils {
	
	static final String DEFAULT_POLYGON_STYLE = "polygon";
	static final String DEFAULT_LINE_STYLE = "line";
	static final String DEFAULT_POINT_STYLE = "point";
	final static ShapefileDataStoreFactory SHP_FACTORY= new ShapefileDataStoreFactory();

	private Utils(){
		
	}

}
