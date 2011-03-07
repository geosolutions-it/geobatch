package it.geosolutions.geobatch.lamma.base;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;

public abstract class LammaBaseConfiguration extends ActionConfiguration
		implements Configuration {

	private String baseOutputDir;

	private String lammaServiceURL;

	public LammaBaseConfiguration(String id, String name, String description) {
		super(id, name, description);
	}

	/**
	 * @param baseOutputDir
	 *            the baseOutputDir to set
	 */
	public void setBaseOutputDir(String baseOutputDir) {
		this.baseOutputDir = baseOutputDir;
	}

	/**
	 * @return the baseOutputDir
	 */
	public String getBaseOutputDir() {
		return baseOutputDir;
	}

	@Override
	public LammaBaseConfiguration clone() { // throws CloneNotSupportedException
											// {
		try {
			return (LammaBaseConfiguration) BeanUtils.cloneBean(this);
		} catch (IllegalAccessException e) {
			final RuntimeException cns = new RuntimeException();
			cns.initCause(e);
			throw cns;
		} catch (InstantiationException e) {
			final RuntimeException cns = new RuntimeException();
			cns.initCause(e);
			throw cns;
		} catch (InvocationTargetException e) {
			final RuntimeException cns = new RuntimeException();
			cns.initCause(e);
			throw cns;
		} catch (NoSuchMethodException e) {
			final RuntimeException cns = new RuntimeException();
			cns.initCause(e);
			throw cns;
		}
	}

	/**
	 * @param lammaServiceURL
	 *            the lammaServiceURL to set
	 */
	public void setLammaServiceURL(String lammaServiceURL) {
		this.lammaServiceURL = lammaServiceURL;
	}

	/**
	 * @return the lammaServiceURL
	 */
	public String getLammaServiceURL() {
		return lammaServiceURL;
	}

}