/*
 * $Header: it.geosolutions.geobatch.ftp.server.model.FtpUser,v. 0.1 13/ott/2009 09.16.47 created by giuseppe $
 * $Revision: 0.1 $
 * $Date: 13/ott/2009 09.16.47 $
 *
 * ====================================================================
 *
 * Copyright (C) 2007-2008 GeoSolutions S.A.S.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. 
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.geobatch.ftp.server.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.ftpserver.usermanager.impl.BaseUser;

/**
 * @author giuseppe
 * 
 */
@Entity(name = "FtpUser")
@Table(name = "FTP_USER")
public class FtpUser extends BaseUser implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1959960226594655854L;

	@Id
	@GeneratedValue
	@Column(name = "ID")
	private Long id;

	@Column(name = "USER_ID", nullable = false, unique = true, length = 64)
	private String userId;

	@Column(name = "USER_PASSWORD", nullable = false, length = 64)
	private String userPassword;

	@Column(name = "HOME_DIRECTORY", length = 128)
	private String homeDirectory;

	@Column(name = "ENABLE_FLAG", columnDefinition = "boolean default true")
	private boolean enableFlag;

	@Column(name = "WRITE_PERMISSION", columnDefinition = "boolean default false")
	private boolean writePermission;

	@Column(name = "IDLE_TIME", columnDefinition = "int default 0")
	private int idleTime;

	@Column(name = "UPLOAD_RATE", columnDefinition = "int default 0")
	private int uploadRate;

	@Column(name = "DOWNLOAD_RATE", columnDefinition = "int default 0")
	private int downloadRate;

	@Column(name = "MAX_LOGIN_NUMBER", columnDefinition = "int default 0")
	private int maxLoginNumber;

	@Column(name = "MAX_LOGIN_PER_IP", columnDefinition = "int default 0")
	private int maxLoginPerIp;

	public FtpUser() {

	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the userPassword
	 */
	public String getUserPassword() {
		return userPassword;
	}

	/**
	 * @param userPassword
	 *            the userPassword to set
	 */
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	/**
	 * @return the homeDirectory
	 */
	public String getHomeDirectory() {
		return homeDirectory;
	}

	/**
	 * @param homeDirectory
	 *            the homeDirectory to set
	 */
	public void setHomeDirectory(String homeDirectory) {
		this.homeDirectory = homeDirectory;
	}

	/**
	 * @return the enableFlag
	 */
	public boolean isEnableFlag() {
		return enableFlag;
	}

	/**
	 * @param enableFlag
	 *            the enableFlag to set
	 */
	public void setEnableFlag(boolean enableFlag) {
		this.enableFlag = enableFlag;
	}

	/**
	 * @return the writePermission
	 */
	public boolean isWritePermission() {
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
	 * @return the idleTime
	 */
	public int getIdleTime() {
		return idleTime;
	}

	/**
	 * @param idleTime
	 *            the idleTime to set
	 */
	public void setIdleTime(int idleTime) {
		this.idleTime = idleTime;
	}

	/**
	 * @return the uploadRate
	 */
	public int getUploadRate() {
		return uploadRate;
	}

	/**
	 * @param uploadRate
	 *            the uploadRate to set
	 */
	public void setUploadRate(int uploadRate) {
		this.uploadRate = uploadRate;
	}

	/**
	 * @return the downloadRate
	 */
	public int getDownloadRate() {
		return downloadRate;
	}

	/**
	 * @param downloadRate
	 *            the downloadRate to set
	 */
	public void setDownloadRate(int downloadRate) {
		this.downloadRate = downloadRate;
	}

	/**
	 * @return the maxLoginNumber
	 */
	public int getMaxLoginNumber() {
		return maxLoginNumber;
	}

	/**
	 * @param maxLoginNumber
	 *            the maxLoginNumber to set
	 */
	public void setMaxLoginNumber(int maxLoginNumber) {
		this.maxLoginNumber = maxLoginNumber;
	}

	/**
	 * @return the maxLoginPerIp
	 */
	public int getMaxLoginPerIp() {
		return maxLoginPerIp;
	}

	/**
	 * @param maxLoginPerIp
	 *            the maxLoginPerIp to set
	 */
	public void setMaxLoginPerIp(int maxLoginPerIp) {
		this.maxLoginPerIp = maxLoginPerIp;
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FtpUser other = (FtpUser) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[ ID : " + getId() + " - USER_ID : " + getUserId()
				+ " - USER_PASSWORD : " + getUserPassword()
				+ "  - HOME_DIRECTORY : " + getHomeDirectory()
				+ " - ENABLE_FLAG : " + isEnableFlag() + " - WRITE_PERMISSION "
				+ isWritePermission() + " - IDLE_TIME " + getIdleTime()
				+ " - UPLOAD_RATE " + getUploadRate() + " - DOWNLOAD_RATE "
				+ getDownloadRate() + " - MAX_LOGIN_NUMBER "
				+ getMaxLoginNumber() + " - MAX_LOGIN_PER_IP "
				+ getMaxLoginPerIp() + "]";
	}

	@Transient
	public String getName() {
		// TODO Auto-generated method stub
		return getUserId();
	}

	@Transient
	public String getPassword() {
		// TODO Auto-generated method stub
		return getUserPassword();
	}
}
