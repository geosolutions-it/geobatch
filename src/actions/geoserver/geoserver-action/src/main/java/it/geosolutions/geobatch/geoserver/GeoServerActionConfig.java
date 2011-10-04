package it.geosolutions.geobatch.geoserver;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

public abstract class GeoServerActionConfig extends ActionConfiguration {

	protected String workingDirectory;
	
	protected String geoserverPWD;
	protected String geoserverUID;
	protected String geoserverURL;

    /**
     * @return the workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @param workingDirectory
     *            the workingDirectory to set
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }
    

	/**
	 * @return the geoserverPWD
	 */
	public final String getGeoserverPWD() {
		return geoserverPWD;
	}

	/**
	 * @param geoserverPWD the geoserverPWD to set
	 */
	public final void setGeoserverPWD(String geoserverPWD) {
		this.geoserverPWD = geoserverPWD;
	}

	/**
	 * @return the geoserverUID
	 */
	public final String getGeoserverUID() {
		return geoserverUID;
	}

	/**
	 * @param geoserverUID the geoserverUID to set
	 */
	public final void setGeoserverUID(String geoserverUID) {
		this.geoserverUID = geoserverUID;
	}

	/**
	 * @return the geoserverURL
	 */
	public final String getGeoserverURL() {
		return geoserverURL;
	}

	/**
	 * @param geoserverURL the geoserverURL to set
	 */
	public final void setGeoserverURL(String geoserverURL) {
		this.geoserverURL = geoserverURL;
	}

	public GeoServerActionConfig(String id, String name, String description) {
		super(id, name, description);
	}
	
	public GeoServerActionConfig(GeoServerActionConfig conf, String url, String pwd, String usr) {
		super(conf.getId(), conf.getName(), conf.getDescription());
		geoserverURL=url;
		geoserverUID=usr;
		geoserverPWD=pwd;
	}
	
    @Override
    public GeoServerActionConfig clone() { 
        final GeoServerActionConfiguration configuration = (GeoServerActionConfiguration) super
                .clone();

        configuration.setGeoserverPWD(geoserverPWD);
        configuration.setGeoserverUID(geoserverUID);
        configuration.setGeoserverURL(geoserverURL);
//        configuration.setServiceID(getServiceID());
        configuration.setWorkingDirectory(workingDirectory);

        return configuration;
    }

}