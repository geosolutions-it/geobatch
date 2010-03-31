package it.geosolutions.geobatch.ftpserver.server;

import it.geosolutions.geobatch.ftpserver.dao.FtpServerConfigDAO;
import it.geosolutions.geobatch.ftpserver.ftp.GeoBatchUserManager;
import it.geosolutions.geobatch.ftpserver.model.FtpServerConfig;
import it.geosolutions.geobatch.users.dao.DAOException;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.springframework.beans.factory.InitializingBean;

public class GeoBatchServer implements InitializingBean {

	private final static Logger LOGGER = Logger.getLogger(GeoBatchServer.class.getName());

	private FtpServer ftpServer;
	private FtpServerConfig lastConfig;
	private FtpServerConfigDAO serverConfigDAO;
	private GeoBatchUserManager userManager;

	public void afterPropertiesSet() throws Exception {
		setLastConfig(serverConfigDAO.load());
		userManager.setServerConfig(getLastConfig());
		ftpServer = create(getLastConfig(), userManager);

		if(getLastConfig().isAutoStart())
			ftpServer.start();
	}


	public static FtpServer create(FtpServerConfig config, UserManager userManager) { 
		// base configuration
		final FtpServerFactory serverFactory = new FtpServerFactory();

		final ConnectionConfigFactory configFactory= new ConnectionConfigFactory();
		configFactory.setAnonymousLoginEnabled(config.isAnonEnabled());
		configFactory.setLoginFailureDelay(config.getLoginFailureDelay());
		configFactory.setMaxAnonymousLogins(config.getMaxAnonLogins());
		configFactory.setMaxLoginFailures(config.getMaxLoginFailures());
		configFactory.setMaxLogins(config.getMaxLogins());
		serverFactory.setConnectionConfig(configFactory.createConnectionConfig());

		//change port
		final ListenerFactory factory = new ListenerFactory();
		factory.setPort(config.getPort());
		factory.setImplicitSsl(config.isSsl());
		serverFactory.addListener("default", factory.createListener());

		// user management
        serverFactory.setUserManager(userManager);

        // callback
		final Map<String, Ftplet> map = new HashMap<String, Ftplet>();
		map.put("GB-Ftplet", new GeoBatchFtplet());
		serverFactory.setFtplets(map);

		return serverFactory.createServer();
	}


	public void suspend() {
		ftpServer.suspend();
	}

	public void stop() {
		ftpServer.stop();
	}

	public synchronized void start() throws FtpException {
		if( ! ftpServer.isStopped()) {
			LOGGER.log(Level.WARNING, "FTP server is already running and will not be started again.");
			return;
		}

		try {
			FtpServerConfig config = serverConfigDAO.load();
			if(true) { // !config.equals(lastConfig)) {
				// config has changed: recreate server with new config
				setLastConfig(config);
                userManager.setServerConfig(getLastConfig());                
				ftpServer = create(getLastConfig(), userManager);
			}
		} catch(DAOException ex) {
			LOGGER.log(Level.WARNING, "Could not retrieve server config. Using old server instance", ex);
		}

		ftpServer.start();
	}

	public void resume() {
		ftpServer.resume();
	}

	public boolean isSuspended() {
		return ftpServer.isSuspended();
	}

	public boolean isStopped() {
		return ftpServer.isStopped();
	}

    public FtpServerConfig getLastConfig() {
        return lastConfig.clone();
    }

	/**
	 * @param lastConfig the lastConfig to set
	 * @throws DAOException 
	 */
	public void setLastConfig(FtpServerConfig lastConfig) throws DAOException {
		this.lastConfig = serverConfigDAO.load();
	}


	/**
	 * @param ftpServer
	 *            the ftpServer to set
	 */
	public void setFtpServer(FtpServer ftpServer) {
		this.ftpServer = ftpServer;
	}

	/**
	 * @return the ftpServer
	 */
	public synchronized FtpServer getFtpServer() {
		return ftpServer;
	}

	public FtpServerConfigDAO getServerConfigDAO() {
		return serverConfigDAO;
	}

	public void setServerConfigDAO(FtpServerConfigDAO serverConfigDAO) {
		this.serverConfigDAO = serverConfigDAO;
	}

	public void setUserManager(GeoBatchUserManager userManager) {
		this.userManager = userManager;
	}

	public GeoBatchUserManager getUserManager() {
		return userManager;
	}

}
