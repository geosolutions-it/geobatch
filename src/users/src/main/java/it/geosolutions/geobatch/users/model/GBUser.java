/*
 * $Header: it.geosolutions.geobatch.ftp.server.model.GBUser,v. 0.1 13/ott/2009 09.16.47 created by giuseppe $
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
package it.geosolutions.geobatch.users.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.Table;

/**
 * 
 */
@Entity(name = "User")
@Table(name = "GBUSER")
public class GBUser implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = -1959960226594655854L;
	/**
	 * @return the id
	 */
	@Id
	@Column(name = "ID")
	@GeneratedValue
	private Long id;

	@Column(name = "USER_NAME", nullable = false, unique = true, length = 64, updatable = false)
	private String name;

	@Column(name = "USER_PASSWORD", nullable = false, length = 64)
	private String password;

	@Column(name = "HOME_DIRECTORY", length = 128)
	private String relativeHomeDir;

	@Column(name = "USER_ENABLED", columnDefinition = "boolean default true")
	private boolean enabled = true;

	@Column(name = "USER_ROLE")
	@Enumerated(EnumType.STRING)
	private GBUserRole role = GBUserRole.ROLE_USER;

	public GBUser() {
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
	public String getName() {
		return name;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setName(String userName) {
		this.name = userName;
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
	public void setPassword(String userPassword) {
		this.password = userPassword;
	}

	/**
	 * @return the homeDirectory
	 */
	public String getRelativeHomeDir() {
		return relativeHomeDir;
	}

	/**
	 * @param homeDirectory
	 *            the homeDirectory to set
	 */
	public void setRelativeHomeDir(String homeDirectory) {
		this.relativeHomeDir = homeDirectory;
	}

	/**
	 * @return the enableFlag
	 */
	public boolean getEnabled() {
		return enabled;
	}

	/**
	 * @param enableFlag
	 *            the enableFlag to set
	 */
	public void setEnabled(boolean enableFlag) {
		this.enabled = enableFlag;
	}

	public GBUserRole getRole() {
		return role;
	}

	public void setRole(GBUserRole role) {
		this.role = role;
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		GBUser other = (GBUser) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[ ID : " + getId() + " - USER_NAME : " + getName()
				+ " - USER_PASSWORD : " + getPassword()
				+ "  - HOME_DIRECTORY : " + getRelativeHomeDir()
				+ " - ENABLE_FLAG : " + getEnabled() + "]";
	}

	@PostLoad
	protected void postLoad() {
		if (getRelativeHomeDir() == null) {
			relativeHomeDir = getName();
		}
	}
}
