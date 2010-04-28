package it.geosolutions.geobatch.sas.esb;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

public class SasEsbConfiguration extends ActionConfiguration implements Configuration {
	
	private String serverURL;
	
	 protected SasEsbConfiguration(String id, String name,
				String description, boolean dirty) {
			super(id, name, description, dirty);
			// TODO Auto-generated constructor stub
		}

	public SasEsbConfiguration() {
		super();
	}

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

}
