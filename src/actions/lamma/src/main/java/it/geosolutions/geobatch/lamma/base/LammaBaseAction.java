package it.geosolutions.geobatch.lamma.base;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.geobatch.flow.event.action.Action;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.opensdi.lamma.model.LammaLog;
import it.geosolutions.opensdi.lamma.services.LammaService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.TimeZone;

import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;

public abstract class LammaBaseAction extends
		BaseAction<FileSystemMonitorEvent> implements
		Action<FileSystemMonitorEvent> {

	protected final LammaBaseConfiguration configuration;

	protected static final TimeZone LAMMA_TZ = TimeZone.getTimeZone("GMT+2");

	protected final LammaService esbClient;

	protected final LammaLog logMessage = new LammaLog();

	public LammaBaseAction(LammaBaseConfiguration actionConfiguration) {
		super(actionConfiguration);
		this.configuration = actionConfiguration;
		this.esbClient = initializeClient();
	}

	/**
	 * 
	 * @return
	 */
	private LammaService initializeClient() {
		// ////////////////////////////////////
		// Client initialization
		// ////////////////////////////////////

		if (this.configuration.getLammaServiceURL() != null) {
			JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
			factory.getInInterceptors().add(new LoggingInInterceptor());
			factory.getOutInterceptors().add(new LoggingOutInterceptor());
			factory.setServiceClass(LammaService.class);
			factory.setAddress(this.configuration.getLammaServiceURL());
			LammaService client = (LammaService) factory.create();

			return client;
		}

		return null;
	}

	/**
	 * 
	 * @param logMessage
	 * @return
	 */
	protected boolean logToESB(LammaLog logMessage) {
		if (esbClient != null) {
			return esbClient.log(logMessage);
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param scriptPath
	 * @param tagName
	 * @return
	 * @throws IOException
	 */
	protected static String getScriptArguments(final String scriptPath,
			final String tagName) throws IOException {
		String value = null;

		// Create FileReader Object
		FileReader inputFileReader = new FileReader(scriptPath);

		try {
			// Create Buffered/PrintWriter Objects
			BufferedReader inputStream = new BufferedReader(inputFileReader);

			String inLine = null;

			while ((inLine = inputStream.readLine()) != null) {
				// Handle KeyWords

				if (inLine.trim().startsWith("<" + tagName + ">")) {
					if (inLine.trim().endsWith("</" + tagName + ">")) {
						int beginIndex = inLine.indexOf("<" + tagName + ">")
								+ ("<" + tagName + ">").length();
						int endIndex = inLine.length()
								- ("</" + tagName + ">").length();
						value = inLine.substring(beginIndex, endIndex);
					} else {
						while ((inLine = inputStream.readLine()) != null) {
							if (!inLine.trim().endsWith("</" + tagName + ">"))
								value = inLine;
							else
								break;
						}
					}
				}
			}

		} catch (IOException e) {
		} finally {
			inputFileReader.close();
		}

		return value;
	}
}