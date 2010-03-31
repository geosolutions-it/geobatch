/*
 */

package it.geosolutions.geobatch.ftpserver.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author etj
 */
@Entity(name = "FtpServerConfig")
@Table(name = "FTP_SERVER_CONFIG")
public class FtpServerConfig implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5498994877614701945L;

	/** We have only one instance */
	@Id
	private int id = 100;

	@Column(nullable=false)
	private int maxLogins = 50;

	@Column(nullable=false)
	private boolean anonEnabled = false;

	@Column(nullable=false)
	private int maxAnonLogins = 1;

	@Column(nullable=false)
	private int maxLoginFailures = 3;

	@Column(nullable=false)
	private int loginFailureDelay = 10;


	@Column(nullable=false)
	private int port = 2121;

	@Column(nullable=false)
	private boolean ssl = false;

	@Column(nullable=false)
	private boolean autoStart = false;

	@Column(length=512)
	private String ftpBaseDir;

	public boolean isAnonEnabled() {
		return anonEnabled;
	}

	public void setAnonEnabled(boolean anonEnabled) {
		this.anonEnabled = anonEnabled;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLoginFailureDelay() {
		return loginFailureDelay;
	}

	public void setLoginFailureDelay(int loginFailureDelay) {
		this.loginFailureDelay = loginFailureDelay;
	}

	public int getMaxAnonLogins() {
		return maxAnonLogins;
	}

	public void setMaxAnonLogins(int maxAnonLogins) {
		this.maxAnonLogins = maxAnonLogins;
	}

	public int getMaxLoginFailures() {
		return maxLoginFailures;
	}

	public void setMaxLoginFailures(int maxLoginFailures) {
		this.maxLoginFailures = maxLoginFailures;
	}

	public int getMaxLogins() {
		return maxLogins;
	}

	public void setMaxLogins(int maxLogins) {
		this.maxLogins = maxLogins;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isSsl() {
		return ssl;
	}

	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	public boolean isAutoStart() {
		return autoStart;
	}

	public void setAutoStart(boolean autoStart) {
		this.autoStart = autoStart;
	}

    public String getFtpBaseDir() {
        return ftpBaseDir;
    }

    public void setFtpBaseDir(String ftpBaseDir) {
        this.ftpBaseDir = ftpBaseDir;
    }


	@Override
	public String toString() {
		return new StringBuilder(getClass().getSimpleName())
			.append('[')
			.append("port:").append(getPort())
			.append(" ssl:").append(isSsl())
			.append(" maxLogins:").append(getMaxLogins())
			.append(" anon:").append(isAnonEnabled())
			.append(" maxAnonLogins:").append(getMaxAnonLogins())
			.append(" maxLoginFailures:").append(getMaxLoginFailures())
			.append(" loginFailureDelay:").append(getLoginFailureDelay())
			.append(" auto:").append(isAutoStart())
			.append(" basedir:").append(getFtpBaseDir())
			.append(']')
			.toString();
	}

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FtpServerConfig other = (FtpServerConfig) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.maxLogins != other.maxLogins) {
            return false;
        }
        if (this.anonEnabled != other.anonEnabled) {
            return false;
        }
        if (this.maxAnonLogins != other.maxAnonLogins) {
            return false;
        }
        if (this.maxLoginFailures != other.maxLoginFailures) {
            return false;
        }
        if (this.loginFailureDelay != other.loginFailureDelay) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if (this.ssl != other.ssl) {
            return false;
        }
        if (this.autoStart != other.autoStart) {
            return false;
        }
        if ((this.ftpBaseDir == null) ? (other.ftpBaseDir != null) : !this.ftpBaseDir.equals(other.ftpBaseDir)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.id;
        hash = 97 * hash + this.maxLogins;
        hash = 97 * hash + (this.anonEnabled ? 1 : 0);
        hash = 97 * hash + this.maxAnonLogins;
        hash = 97 * hash + this.maxLoginFailures;
        hash = 97 * hash + this.loginFailureDelay;
        hash = 97 * hash + this.port;
        hash = 97 * hash + (this.ssl ? 1 : 0);
        hash = 97 * hash + (this.autoStart ? 1 : 0);
        hash = 97 * hash + (this.ftpBaseDir != null ? this.ftpBaseDir.hashCode() : 0);
        return hash;
    }

    @Override
    public FtpServerConfig clone()  {
        FtpServerConfig clone = new FtpServerConfig();
        clone.setAnonEnabled(anonEnabled);
        clone.setAutoStart(autoStart);
        clone.setId(id);
        clone.setLoginFailureDelay(loginFailureDelay);
        clone.setMaxAnonLogins(maxAnonLogins);
        clone.setMaxLoginFailures(maxLoginFailures);
        clone.setMaxLogins(maxLogins);
        clone.setPort(port);
        clone.setSsl(ssl);
        clone.setFtpBaseDir(ftpBaseDir);
        return clone;
    }
}
