package it.geosolutions.geobatch.beam;

import it.geosolutions.geobatch.geoserver.GeoServerActionConfiguration;

public class HarvestGeoServerActionConfiguration extends GeoServerActionConfiguration {

    private String outputFolder;
    
    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public HarvestGeoServerActionConfiguration(String id, String name, String description) {
        super(id, name, description);
    }

}
