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

import it.geosolutions.geobatch.flow.file.FileBasedFlowManager;
import it.geosolutions.geobatch.users.model.GBUserRole;

import java.util.List;

/**
 * @author Francesco Izzi
 * @author Alessio Fabiani
 * 
 */
public class FtpUserDataBean {

    private Long userId;

    private String userName;

    private String password;

    private String repeatPassword;

    private GBUserRole role;

    private boolean writePermission;

    private String uploadRate;

    private String downloadRate;

    private int maxLoginPerIp;

    private int maxLoginNumber;

    private int idleTime;

    private List<GBUserRole> availableRoles;

    private List<FileBasedFlowManager> availableFlowManagers;

    private List<String> allowedFlowManagers;

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * @return the userId
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * @return the userId
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @return the userPassword
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param userPassword
     *            the userPassword to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param repeatPassword
     *            the repeatPassword to set
     */
    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    /**
     * @return the repeatPassword
     */
    public String getRepeatPassword() {
        return repeatPassword;
    }

    /**
     * @param role
     *            the role to set
     */
    public void setRole(GBUserRole role) {
        this.role = role;
    }

    /**
     * @return the role
     */
    public GBUserRole getRole() {
        return role;
    }

    /**
     * @return the writePermission
     */
    public boolean getWritePermission() {
        return writePermission;
    }

    /**
     * @param writePermission
     *            the writePermission to set
     */
    public void setWritePermission(boolean writePermission) {
        this.writePermission = writePermission;
    }

    /**
     * @return the uploadRate
     */
    public String getUploadRate() {
        return uploadRate;
    }

    /**
     * @param uploadRate
     *            the uploadRate to set
     */
    public void setUploadRate(String uploadRate) {
        this.uploadRate = uploadRate;
    }

    /**
     * @return the downloadRate
     */
    public String getDownloadRate() {
        return downloadRate;
    }

    /**
     * @param downloadRate
     *            the downloadRate to set
     */
    public void setDownloadRate(String downloadRate) {
        this.downloadRate = downloadRate;
    }

    /**
     * @param maxLoginPerIp
     *            the maxLoginPerIp to set
     */
    public void setMaxLoginPerIp(int maxLoginPerIp) {
        this.maxLoginPerIp = maxLoginPerIp;
    }

    /**
     * @return the maxLoginPerIp
     */
    public int getMaxLoginPerIp() {
        return maxLoginPerIp;
    }

    /**
     * @param maxLoginNumber
     *            the maxLoginNumber to set
     */
    public void setMaxLoginNumber(int maxLoginNumber) {
        this.maxLoginNumber = maxLoginNumber;
    }

    /**
     * @return the maxLoginNumber
     */
    public int getMaxLoginNumber() {
        return maxLoginNumber;
    }

    /**
     * @param idleTime
     *            the idleTime to set
     */
    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }

    /**
     * @return the idleTime
     */
    public int getIdleTime() {
        return idleTime;
    }

    /**
     * @param availableRoles
     *            the availableRoles to set
     */
    public void setAvailableRoles(List<GBUserRole> availableRoles) {
        this.availableRoles = availableRoles;
    }

    /**
     * @return the availableRoles
     */
    public List<GBUserRole> getAvailableRoles() {
        return availableRoles;
    }

    /**
     * @param availableFlowManagers
     *            the availableFlowManagers to set
     */
    public void setAvailableFlowManagers(List<FileBasedFlowManager> availableFlowManagers) {
        this.availableFlowManagers = availableFlowManagers;
    }

    /**
     * @return the availableFlowManagers
     */
    public List<FileBasedFlowManager> getAvailableFlowManagers() {
        return availableFlowManagers;
    }

    /**
     * @param list
     *            the allowedFlowManagers to set
     */
    public void setAllowedFlowManagers(List<String> allowedFlowManagers) {
        this.allowedFlowManagers = allowedFlowManagers;
    }

    /**
     * @return the allowedFlowManagers
     */
    public List<String> getAllowedFlowManagers() {
        return allowedFlowManagers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final int maxLen = 10;
        StringBuilder builder = new StringBuilder();
        builder.append("FtpUserDataBean [");
        if (allowedFlowManagers != null) {
            builder.append("allowedFlowManagers=");
            builder.append(allowedFlowManagers.subList(0, Math.min(allowedFlowManagers.size(),
                    maxLen)));
            builder.append(", ");
        }
        if (availableFlowManagers != null) {
            builder.append("availableFlowManagers=");
            builder.append(availableFlowManagers.subList(0, Math.min(availableFlowManagers.size(),
                    maxLen)));
            builder.append(", ");
        }
        if (downloadRate != null) {
            builder.append("downloadRate=");
            builder.append(downloadRate);
            builder.append(", ");
        }
        builder.append("idleTime=");
        builder.append(idleTime);
        builder.append(", maxLoginNumber=");
        builder.append(maxLoginNumber);
        builder.append(", maxLoginPerIp=");
        builder.append(maxLoginPerIp);
        builder.append(", ");
        if (password != null) {
            builder.append("password=");
            builder.append(password);
            builder.append(", ");
        }
        if (repeatPassword != null) {
            builder.append("repeatPassword=");
            builder.append(repeatPassword);
            builder.append(", ");
        }
        if (uploadRate != null) {
            builder.append("uploadRate=");
            builder.append(uploadRate);
            builder.append(", ");
        }
        if (userId != null) {
            builder.append("userId=");
            builder.append(userId);
            builder.append(", ");
        }
        if (userName != null) {
            builder.append("userName=");
            builder.append(userName);
            builder.append(", ");
        }
        builder.append("writePermission=");
        builder.append(writePermission);
        builder.append("]");
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
        result = prime * result
                + ((allowedFlowManagers == null) ? 0 : allowedFlowManagers.hashCode());
        result = prime * result
                + ((availableFlowManagers == null) ? 0 : availableFlowManagers.hashCode());
        result = prime * result + ((downloadRate == null) ? 0 : downloadRate.hashCode());
        result = prime * result + idleTime;
        result = prime * result + maxLoginNumber;
        result = prime * result + maxLoginPerIp;
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((repeatPassword == null) ? 0 : repeatPassword.hashCode());
        result = prime * result + ((uploadRate == null) ? 0 : uploadRate.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        result = prime * result + (writePermission ? 1231 : 1237);
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
        if (!(obj instanceof FtpUserDataBean)) {
            return false;
        }
        FtpUserDataBean other = (FtpUserDataBean) obj;
        if (allowedFlowManagers == null) {
            if (other.allowedFlowManagers != null) {
                return false;
            }
        } else if (!allowedFlowManagers.equals(other.allowedFlowManagers)) {
            return false;
        }
        if (availableFlowManagers == null) {
            if (other.availableFlowManagers != null) {
                return false;
            }
        } else if (!availableFlowManagers.equals(other.availableFlowManagers)) {
            return false;
        }
        if (downloadRate == null) {
            if (other.downloadRate != null) {
                return false;
            }
        } else if (!downloadRate.equals(other.downloadRate)) {
            return false;
        }
        if (idleTime != other.idleTime) {
            return false;
        }
        if (maxLoginNumber != other.maxLoginNumber) {
            return false;
        }
        if (maxLoginPerIp != other.maxLoginPerIp) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (repeatPassword == null) {
            if (other.repeatPassword != null) {
                return false;
            }
        } else if (!repeatPassword.equals(other.repeatPassword)) {
            return false;
        }
        if (uploadRate == null) {
            if (other.uploadRate != null) {
                return false;
            }
        } else if (!uploadRate.equals(other.uploadRate)) {
            return false;
        }
        if (userId == null) {
            if (other.userId != null) {
                return false;
            }
        } else if (!userId.equals(other.userId)) {
            return false;
        }
        if (userName == null) {
            if (other.userName != null) {
                return false;
            }
        } else if (!userName.equals(other.userName)) {
            return false;
        }
        if (writePermission != other.writePermission) {
            return false;
        }
        return true;
    }

}
