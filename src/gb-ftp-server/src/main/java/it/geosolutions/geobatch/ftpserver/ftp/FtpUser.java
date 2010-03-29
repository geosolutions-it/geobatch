/*
 */

package it.geosolutions.geobatch.ftpserver.ftp;

import it.geosolutions.geobatch.ftpserver.model.*;
import it.geosolutions.geobatch.users.model.GBUser;
import it.geosolutions.geobatch.users.model.GBUserRole;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
/**
 * Represents a GBuser merged with its related FTP props
 *
 * @author etj
 */
public class FtpUser implements User {
    private final static Logger LOGGER = Logger.getLogger(FtpUser.class.getName());

	private GBUser delegateUser;
	private FtpProps delegateFtpProps;

	/**
	 * This is a required data used in class User, holding the full path of the homedir
	 */
	private String homeDirectory;

    private List<Authority> authorities = new ArrayList<Authority>();

	public static FtpUser createInstance()  {
		return new FtpUser(new GBUser(), new FtpProps());
	}

	public FtpUser(GBUser delegateUser, FtpProps delegateFtpProps) {
		this.delegateUser = delegateUser;
		this.delegateFtpProps = delegateFtpProps;
	}
	
    //==========================================================================
    // GBUser props

	public Long getId() {
		return delegateUser.getId();
	}

	public void setId(Long id) {
		delegateUser.setId(id);
	}

	public String getName() {
		return delegateUser.getName();
	}

	public void setName(String userId) {
		delegateUser.setName(userId);
	}

	public String getPassword() {
		return delegateUser.getPassword();
	}

	public void setPassword(String userPassword) {
		delegateUser.setPassword(userPassword);
	}

	public String getRelativeHomeDir() {
		return delegateUser.getRelativeHomeDir();
	}

	public void setRelativeHomeDir(String homeDirectory) {
		delegateUser.setRelativeHomeDir(homeDirectory);
	}

	public boolean getEnabled() {
		return delegateUser.getEnabled();
	}

	public void setEnabled(boolean enableFlag) {
		delegateUser.setEnabled(enableFlag);
	}

    public void setRole(GBUserRole role) {
        delegateUser.setRole(role);
    }

    public GBUserRole getRole() {
        return delegateUser.getRole();
    }

    //==========================================================================
    // FTP props

	public void setWritePermission(boolean writePermission) {
		delegateFtpProps.setWritePermission(writePermission);
	}

	public void setUploadRate(int uploadRate) {
		delegateFtpProps.setUploadRate(uploadRate);
	}

	public void setMaxLoginPerIp(int maxLoginPerIp) {
		delegateFtpProps.setMaxLoginPerIp(maxLoginPerIp);
	}

	public void setMaxLoginNumber(int maxLoginNumber) {
		delegateFtpProps.setMaxLoginNumber(maxLoginNumber);
	}

	public void setMaxIdleTime(int idleTime) {
		delegateFtpProps.setMaxIdleTime(idleTime);
	}

	public void setDownloadRate(int downloadRate) {
		delegateFtpProps.setDownloadRate(downloadRate);
	}

	public boolean isWritePermission() {
		return delegateFtpProps.isWritePermission();
	}

	public int getUploadRate() {
		return delegateFtpProps.getUploadRate();
	}

	public int getMaxLoginPerIp() {
		return delegateFtpProps.getMaxLoginPerIp();
	}

	public int getMaxLoginNumber() {
		return delegateFtpProps.getMaxLoginNumber();
	}

	public int getMaxIdleTime() {
		return delegateFtpProps.getMaxIdleTime();
	}

	public int getDownloadRate() {
		return delegateFtpProps.getDownloadRate();
	}


    public List<Authority> getAuthorities() {
        if (authorities != null) {
            return Collections.unmodifiableList(authorities);
        } else {
            // try to lazily create auths
            List<Authority> a = createAuthorities();
            if( ! a.isEmpty()) {
                authorities = a;
                return Collections.unmodifiableList(authorities);
            } else
                return null;
        }
    }

    public void setAuthorities(List<Authority> authorities) {
        if (authorities != null) {
            this.authorities = Collections.unmodifiableList(authorities);
        } else {
            this.authorities = null;
        }
    }
	
    /**
     * {@inheritDoc}
     */
    public AuthorizationRequest authorize(AuthorizationRequest request) {
        // check for no authorities at all
        if(authorities == null || authorities.isEmpty()) {
            authorities = createAuthorities();
        }

        boolean someoneCouldAuthorize = false;
        for (Authority authority : authorities) {
            if (authority.canAuthorize(request)) {
                someoneCouldAuthorize = true;

                request = authority.authorize(request);

                // authorization failed, return null
                if (request == null) {
                    return null;
                }
            }

        }

        if (someoneCouldAuthorize) {
            return request;
        } else {
            return null;
        }
    }


    /**
     * Creates Authorities based on ftp properties.
     * 
     */
    protected List<Authority> createAuthorities() {

        List<Authority> auth = new ArrayList<Authority>();

        if (isWritePermission()) {
            auth.add(new WritePermission());
        }
        auth.add(new ConcurrentLoginPermission(getMaxLoginNumber(), getMaxLoginPerIp()));
        auth.add(new TransferRatePermission(getDownloadRate(), getUploadRate()));

        return auth;
    }

    /**
     * {@inheritDoc}
     */
    public List<Authority> getAuthorities(Class<? extends Authority> clazz) {
        List<Authority> selected = new ArrayList<Authority>();

        for (Authority authority : authorities) {
            if (authority.getClass().equals(clazz)) {
                selected.add(authority);
            }
        }

        return selected;
    }

	public FtpProps getSourceFtpProps() {
		return delegateFtpProps;
	}

	public GBUser getSourceUser() {
		return delegateUser;
	}

	public void setSourceFtpProps(FtpProps delegateFtpProps) {
		this.delegateFtpProps = delegateFtpProps;
	}

	public void setSourceUser(GBUser delegateUser) {
		this.delegateUser = delegateUser;
	}

	public String getHomeDirectory() {
		return homeDirectory;
	}

	public void setHomeDirectory(String homeDirectory) {
		this.homeDirectory = homeDirectory;
	}

	@Override
	public String toString() {
		return "[ ID : " + getId()
				+ " - NAME : " + getName()
//				+ " - PWD : " + getPassword()
				+ " - HOME_DIR : " + getRelativeHomeDir()
				+ " - ENABLE_FLAG : " + getEnabled()
				+ " - ROLE : " + getRole()
                + "]["
				+ " - WRITE_PERMISSION " + isWritePermission()
				+ " - IDLE_TIME " + getMaxIdleTime()
				+ " - UPLOAD_RATE " + getUploadRate()
				+ " - DOWNLOAD_RATE " + getDownloadRate()
				+ " - MAX_LOGIN_NUMBER " + getMaxLoginNumber()
				+ " - MAX_LOGIN_PER_IP " + getMaxLoginPerIp()
                + "]";
	}

}


