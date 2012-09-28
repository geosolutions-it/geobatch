package it.geosolutions.geobatch.geoserver;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

public abstract class GeoServerActionConfig extends ActionConfiguration implements Cloneable {

    protected String geoserverPWD;

    protected String geoserverUID;

    protected String geoserverURL;

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
        geoserverURL = url;
        geoserverUID = usr;
        geoserverPWD = pwd;
    }

}