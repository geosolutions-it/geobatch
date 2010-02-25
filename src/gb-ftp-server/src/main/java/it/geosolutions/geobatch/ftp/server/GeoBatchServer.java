package it.geosolutions.geobatch.ftp.server;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.impl.DefaultFtpServer;
import org.springframework.beans.factory.InitializingBean;

public class GeoBatchServer implements InitializingBean {

	private FtpServer ftpServer;

	public void afterPropertiesSet() throws Exception {
		((GeoBatchUserManager) ((DefaultFtpServer) ftpServer).getUserManager())
				.setFtpServer((DefaultFtpServer) ftpServer);
		this.ftpServer.start();
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
}
