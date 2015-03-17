package it.geosolutions.geobatch.geoserver.reload;

import it.geosolutions.geobatch.annotations.Action;
import it.geosolutions.geobatch.annotations.CheckConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;
import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.global.CatalogHolder;

import javax.management.BadAttributeValueExpException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

@Action(configurationClass=GeoServerReloadConfiguration.class)
public class GeoServerReload extends BaseAction<EventObject> {

	protected final static Logger LOGGER = LoggerFactory
			.getLogger(GeoserverReload.class);

	final public static String PASS = "pass";
	final public static String USER = "user";
	final public static String URL = "url";

	final GeoServerReloadConfiguration conf;

	final ExecutorService es;

	public GeoServerReload(GeoServerReloadConfiguration actionConfiguration) {
		super(actionConfiguration);
		conf = actionConfiguration;
		es = Executors.newScheduledThreadPool(conf.getExecutorSize());
	}

    @Override
	@CheckConfiguration
	public boolean checkConfiguration() {
		// TODO Auto-generated method stub
		return true;
	}
	
    private String getAbsolutePath(String working_dir) {
		if (working_dir == null)
			return null;
		final File working_dirFile = new File(working_dir);
		if (working_dirFile.isAbsolute() || working_dirFile.isFile()
				|| working_dirFile.isDirectory()) {
			try {
				return working_dirFile.getCanonicalPath();
			} catch (IOException e) {
				return null;
			}
		}

		final FileBaseCatalog c = (FileBaseCatalog) CatalogHolder.getCatalog();
		if (c == null)
			return null;

		try {
			File fo = it.geosolutions.tools.commons.file.Path.findLocation(
					working_dir, c.getConfigDirectory());
			if (fo != null) {
				return fo.toString();
			}
		} catch (Exception e) {
			// eat
		}
		return null;
	}
    
	@Override
	public Queue<EventObject> execute(Queue<EventObject> events)
			throws ActionException {

		listenerForwarder.started();
		listenerForwarder.setTask("Checking conf");
		final String geoserverListName = getAbsolutePath(conf
				.getGeoserverList());
		final File geoserverListFile;
		if (geoserverListName != null) {
			geoserverListFile = new File(geoserverListName);
		} else {
			final String message = "Unable to resolve the geoserverList file: "
					+ conf.getGeoserverList();
			if (LOGGER.isErrorEnabled())
				LOGGER.error(message);
			final ActionException ae = new ActionException(this, message);
			listenerForwarder.failed(ae);
			throw ae;
		}

		try {
			listenerForwarder.setTask("Deserialize list");
			final List<Map<String, String>> list = deserialize(geoserverListFile);
			final List<Future<Boolean>> futureList = new ArrayList<Future<Boolean>>();
			listenerForwarder.setTask("Running reload on the server list");
			if (!es.isShutdown() && !es.isTerminated()) {
				if (list != null && list.size() > 0) {
					for (Map<String, String> map : list) {
						final String pass = map.get(PASS);
						final String user = map.get(USER);
						final String url = map.get(URL);
						futureList.add(es.submit(new GeoserverReload(url, user,
								pass)));
					}

					for (Future<Boolean> future : futureList) {
						if (!future.get()) {
							// if we need to check returns...
							if (!isFailIgnored()) {
								// failed!
								final String message = "Some geoserver in the list filed to reload";
								if (LOGGER.isErrorEnabled())
									LOGGER.error(message);
								final ActionException ae = new ActionException(
										this, message);
								listenerForwarder.failed(ae);
								throw ae;
							}
						}
					}

					listenerForwarder.setTask("Reload is completed");
					listenerForwarder.completed();
				} else {
					final String message = "The geoserver reload list is null or empty";
					if (LOGGER.isErrorEnabled())
						LOGGER.error(message);
					final ActionException ae = new ActionException(this,
							message);
					listenerForwarder.failed(ae);
					throw ae;
				}
			} else {
				final String message = "The executor service is not started";
				if (LOGGER.isErrorEnabled())
					LOGGER.error(message);
				final ActionException ae = new ActionException(this, message);
				listenerForwarder.failed(ae);
				throw ae;
			}
		} catch (BadAttributeValueExpException e) {
			listenerForwarder.failed(e);
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getLocalizedMessage(), e);
			}
			final ActionException ae = new ActionException(this,
					e.getLocalizedMessage());
			throw ae;
		} catch (InterruptedException e) {
			listenerForwarder.failed(e);
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getLocalizedMessage(), e);
			}
			final ActionException ae = new ActionException(this,
					e.getLocalizedMessage());
			throw ae;
		} catch (ExecutionException e) {
			listenerForwarder.failed(e);
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(e.getLocalizedMessage(), e);
			}
			final ActionException ae = new ActionException(this,
					e.getLocalizedMessage());
			throw ae;
		} finally {
			if (es != null)
				es.shutdown();
		}
		return events;
	}

	/**
	 * deserialize the xml file containing the list of geoserver to reload
	 * 
	 * @param xml
	 * @return
	 * @throws BadAttributeValueExpException
	 */
	private List<Map<String, String>> deserialize(final File xml)
			throws BadAttributeValueExpException {

		if (xml == null)
			throw new IllegalArgumentException("input file is null");

		XStream stream = new XStream();

		FileInputStream input = null;
		try {
			input = new FileInputStream(xml);
			Object o = stream.fromXML(input);
			if (o == null)
				throw new NullPointerException("Serialized object is null");
			if (o instanceof List<?>) {
				// TODO better checks
				return (List<Map<String, String>>) o;
			} else
				throw new BadAttributeValueExpException("Bad file content:"
						+ o.toString());

		} catch (IOException e) {
			if (LOGGER.isErrorEnabled())
				LOGGER.error(e.getLocalizedMessage(), e);
		} finally {
			IOUtils.closeQuietly(input);
		}
		return null;
	}

	class GeoserverReload implements Callable<Boolean> {

		final String pass;
		final String user;
		final String url;

		public GeoserverReload(final String url, final String user,
				final String pass) {
			this.pass = pass;
			this.user = user;
			this.url = url;
		}

		@Override
		public Boolean call() throws Exception {
			final GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(url, user, pass);

			if (!publisher.reload()) {
				if (LOGGER.isWarnEnabled())
					LOGGER.warn("Failed reload GS:" + url + " user " + user
							+ " pass " + pass);
				return false;
			} else {
				if (LOGGER.isInfoEnabled())
					LOGGER.info("Succesfully reloaded GS: " + url);
				return true;
			}
		}
	}



}
