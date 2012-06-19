package it.geosolutions.geobatch.geoserver;

public enum UploadMethod {
	DIRECT, EXTERNAL;
	
	public static UploadMethod getDefault(){
		return DIRECT;
	}
}
