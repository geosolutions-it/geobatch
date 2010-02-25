/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.geosolutions.geobatch.ftp.client.configuration;

import it.geosolutions.geobatch.catalog.Configuration;
import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;

import org.springframework.beans.BeanUtils;


/**
 * This class represent a basic configuration to FTP action.
 * 
 * @author Tobia Di Pisa (tobia.dipisa@geo-solutions.it)
 */
public class FTPActionConfiguration  extends ActionConfiguration implements Configuration {

    public static final String DEFAULT_PORT = "21";
    
    public static final int defaultTimeout = 5000;

	private String ftpserverHost;

    private String ftpserverPWD;

    private String ftpserverUSR;

    private int ftpserverPort;

    private String workingDirectory;

    private String dataTransferMethod;
    
    private int timeout;
    
    private boolean zipInput;
    
    private String zipFileName;
    
	private FTPConnectMode connectMode;
	
	private String localTempDir;    

	public enum FTPConnectMode{
		ACTIVE,PASSIVE;
	}
    
	/**
	 * Default constructor
	 */
	public FTPActionConfiguration() {
        super();
    }

    /**
     * @return the localTempDir
     */
	public String getLocalTempDir() {
		return localTempDir;
	}

    /**
     * @param localTempDir  the localTempDir to set
     */
	public void setLocalTempDir(String localTempDir) {
		this.localTempDir = localTempDir;
	}
    
    /**
     * @return the connectMode
     */
    public FTPConnectMode getConnectMode() {
		return connectMode;
	}

    /**
     * @param connectMode  the connectMode to set
     */
	public void setConnectMode(FTPConnectMode connectMode) {
		this.connectMode = connectMode;
	}

    /**
     * @return the zipFileName
     */
	public String getZipFileName() {
		return zipFileName;
	}

    /**
     * @param zipFileName  the zipFileName to set
     */
	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}

    /**
     * @return the zipInput.
     */
	public boolean isZipInput() {
		return zipInput;
	}

    /**
     * @param zipInput  the zipInput to set
     */
	public void setZipInput(boolean zipInput) {
		this.zipInput = zipInput;
	}

    /**
     * @return the timeout
     */
	public int getTimeout() {
		return timeout;
	}

    /**
     * @param timeout  the timeout to set
     */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

    /**
     * @return the ftpserverHost
     */
    public String getFtpserverHost() {
        return ftpserverHost;
    }

    /**
     * @param ftpserverHost  the ftpserverHost to set
     */
    public void setFtpserverHost(String ftpserverHost) {
        this.ftpserverHost = ftpserverHost;
    }

    /**
     * @return the ftpserverUSR
     */
    public String getFtpserverUSR() {
        return ftpserverUSR;
    }

    /**
     * @param ftpserverUSR  the ftpserverUSR to set
     */
    public void setFtpserverUSR(String ftpserverUSR) {
        this.ftpserverUSR = ftpserverUSR;
    }

    /**
     * @return the ftpserverPWD
     */
    public String getFtpserverPWD() {
        return ftpserverPWD;
    }

    /**
     * @param ftpserverPWD  the ftpserverPWD to set
     */
    public void setFtpserverPWD(String ftpserverPWD) {
        this.ftpserverPWD = ftpserverPWD;
    }

    /**
     * @return the ftpserverPort
     */
    public int getFtpserverPort() {
        return ftpserverPort;
    }

    /**
     * @param ftpserverPort  the ftpserverPort to set
     */
    public void setFtpserverPort(int ftpserverPort) {
        this.ftpserverPort = ftpserverPort;
    }

    /**
     * @return the workingDirectory
     */
    public String getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @param workingDirectory  the workingDirectory to set
     */
    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * @return the dataTransferMethod
     */
    public String getDataTransferMethod() {
        return dataTransferMethod;
    }

    /**
     * @param dataTransferMethod  the dataTransferMethod to set
     */
    public void setDataTransferMethod(String dataTransferMethod) {
        this.dataTransferMethod = dataTransferMethod;
    }

	@Override
	public FTPActionConfiguration clone() throws CloneNotSupportedException {
		final FTPActionConfiguration configuration= new FTPActionConfiguration();
		BeanUtils.copyProperties(this, configuration);
		return configuration;
	}
}
