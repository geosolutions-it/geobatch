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

import java.util.List;

/**
 * @author Francesco Izzi
 * 
 */
public class FtpUserDataBean {

	private Long   userId;
	
	private String userName;

	private String password;
	
	private String repeatPassword;

	private boolean writePermission;

	private String uploadRate;

	private String downloadRate;
	
	private List<FileBasedFlowManager> availableFlowManagers;
	private List<String> allowedFlowManagers;

	/**
	 * @param userId the userId to set
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
	 * @param repeatPassword the repeatPassword to set
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
	 * @param availableFlowManagers the availableFlowManagers to set
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
	 * @param list the allowedFlowManagers to set
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "FtpUserDataBean [downloadRate=" + downloadRate + ", password="
				+ password + ", uploadRate=" + uploadRate + ", userId="
				+ userName + ", writePermission=" + writePermission + "]";
	}

}
