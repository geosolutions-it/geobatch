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

package it.geosolutions.geobatch.ui.mvc.data;

/**
 * @author Alessio Fabiani
 * 
 */
public class FtpConfigDataBean {

    private int id;

    private int maxLogins;

    private boolean anonEnabled;

    private int maxAnonLogins;

    private int maxLoginFailures;

    private int loginFailureDelay;

    private int port;

    private boolean ssl;

    private boolean autoStart;

    private String ftpBaseDir;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the maxLogins
     */
    public int getMaxLogins() {
        return maxLogins;
    }

    /**
     * @param maxLogins
     *            the maxLogins to set
     */
    public void setMaxLogins(int maxLogins) {
        this.maxLogins = maxLogins;
    }

    /**
     * @return the anonEnabled
     */
    public boolean isAnonEnabled() {
        return anonEnabled;
    }

    /**
     * @param anonEnabled
     *            the anonEnabled to set
     */
    public void setAnonEnabled(boolean anonEnabled) {
        this.anonEnabled = anonEnabled;
    }

    /**
     * @return the maxAnonLogins
     */
    public int getMaxAnonLogins() {
        return maxAnonLogins;
    }

    /**
     * @param maxAnonLogins
     *            the maxAnonLogins to set
     */
    public void setMaxAnonLogins(int maxAnonLogins) {
        this.maxAnonLogins = maxAnonLogins;
    }

    /**
     * @return the maxLoginFailures
     */
    public int getMaxLoginFailures() {
        return maxLoginFailures;
    }

    /**
     * @param maxLoginFailures
     *            the maxLoginFailures to set
     */
    public void setMaxLoginFailures(int maxLoginFailures) {
        this.maxLoginFailures = maxLoginFailures;
    }

    /**
     * @return the loginFailureDelay
     */
    public int getLoginFailureDelay() {
        return loginFailureDelay;
    }

    /**
     * @param loginFailureDelay
     *            the loginFailureDelay to set
     */
    public void setLoginFailureDelay(int loginFailureDelay) {
        this.loginFailureDelay = loginFailureDelay;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port
     *            the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the ssl
     */
    public boolean isSsl() {
        return ssl;
    }

    /**
     * @param ssl
     *            the ssl to set
     */
    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    /**
     * @return the autoStart
     */
    public boolean isAutoStart() {
        return autoStart;
    }

    /**
     * @param autoStart
     *            the autoStart to set
     */
    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    /**
     * @return the ftpBaseDir
     */
    public String getFtpBaseDir() {
        return ftpBaseDir;
    }

    /**
     * @param ftpBaseDir
     *            the ftpBaseDir to set
     */
    public void setFtpBaseDir(String ftpBaseDir) {
        this.ftpBaseDir = ftpBaseDir;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("FtpConfigDataBean [anonEnabled=").append(anonEnabled)
                .append(", autoStart=").append(autoStart).append(", ");
        if (ftpBaseDir != null)
            builder.append("ftpBaseDir=").append(ftpBaseDir).append(", ");
        builder.append("id=").append(id).append(", loginFailureDelay=").append(loginFailureDelay)
                .append(", maxAnonLogins=").append(maxAnonLogins).append(", maxLoginFailures=")
                .append(maxLoginFailures).append(", maxLogins=").append(maxLogins)
                .append(", port=").append(port).append(", ssl=").append(ssl).append("]");
        return builder.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (anonEnabled ? 1231 : 1237);
        result = prime * result + (autoStart ? 1231 : 1237);
        result = prime * result + ((ftpBaseDir == null) ? 0 : ftpBaseDir.hashCode());
        result = prime * result + id;
        result = prime * result + loginFailureDelay;
        result = prime * result + maxAnonLogins;
        result = prime * result + maxLoginFailures;
        result = prime * result + maxLogins;
        result = prime * result + port;
        result = prime * result + (ssl ? 1231 : 1237);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FtpConfigDataBean)) {
            return false;
        }
        FtpConfigDataBean other = (FtpConfigDataBean) obj;
        if (anonEnabled != other.anonEnabled) {
            return false;
        }
        if (autoStart != other.autoStart) {
            return false;
        }
        if (ftpBaseDir == null) {
            if (other.ftpBaseDir != null) {
                return false;
            }
        } else if (!ftpBaseDir.equals(other.ftpBaseDir)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (loginFailureDelay != other.loginFailureDelay) {
            return false;
        }
        if (maxAnonLogins != other.maxAnonLogins) {
            return false;
        }
        if (maxLoginFailures != other.maxLoginFailures) {
            return false;
        }
        if (maxLogins != other.maxLogins) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        if (ssl != other.ssl) {
            return false;
        }
        return true;
    }

}
