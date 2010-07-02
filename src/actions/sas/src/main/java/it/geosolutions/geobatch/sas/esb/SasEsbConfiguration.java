package it.geosolutions.geobatch.sas.esb;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

public class SasEsbConfiguration extends ActionConfiguration implements Configuration {
	
	private String serverURL;
	
	private String geoserverURL;
	
	 protected SasEsbConfiguration(String id, String name, String description, boolean dirty) {
			super(id, name, description, dirty);
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

    /**
	 * @param geoserverURL the geoserverURL to set
	 */
	public void setGeoserverURL(String geoserverURL) {
		this.geoserverURL = geoserverURL;
	}

	/**
	 * @return the geoserverURL
	 */
	public String getGeoserverURL() {
		return geoserverURL;
	}

	@Override
    public SasEsbConfiguration clone() { // throws CloneNotSupportedException {
    	try {
			return (SasEsbConfiguration) BeanUtils.cloneBean(this);
		} catch (IllegalAccessException e) {
			final RuntimeException cns= new RuntimeException();
			cns.initCause(e);
			throw cns;
		} catch (InstantiationException e) {
			final RuntimeException cns= new RuntimeException();
			cns.initCause(e);
			throw cns;
		} catch (InvocationTargetException e) {
			final RuntimeException cns= new RuntimeException();
			cns.initCause(e);
			throw cns;
		} catch (NoSuchMethodException e) {
			final RuntimeException cns= new RuntimeException();
			cns.initCause(e);
			throw cns;
		}
    }
}
