/*
 * $Header: it.geosolutions.geobatch.ftp.server.GeoBatchUserManager,v. 0.1 14/ott/2009 10.33.19 created by giuseppe $
 * $Revision: 0.1 $
 * $Date: 14/ott/2009 10.33.19 $
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
package it.geosolutions.geobatch.ftp.server;

import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.ftp.server.dao.DAOException;
import it.geosolutions.geobatch.ftp.server.dao.FtpUserDAO;
import it.geosolutions.geobatch.ftp.server.model.FtpUser;
import it.geosolutions.geobatch.global.CatalogHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.impl.DefaultFtpServer;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

/**
 * @author giuseppe
 * 
 */
public class GeoBatchUserManager implements UserManager {

	private Logger logger = Logger.getLogger(GeoBatchUserManager.class
			.getName());

	private File ftpRootDir;

	private FtpUserDAO ftpUserDAO;

	private DefaultFtpServer ftpServer;

	public GeoBatchUserManager() {
		String baseDir = ((FileBaseCatalog) CatalogHolder.getCatalog())
				.getBaseDirectory();
		ftpRootDir = new File(baseDir + File.separator + "FTP" + File.separator);
	}

	/**
	 * @param ftpUserDAO
	 *            the ftpUser to set
	 */
	public void setFtpUserDAO(FtpUserDAO ftpUserDAO) {
		this.ftpUserDAO = ftpUserDAO;
	}

	/**
	 * @param ftpServer
	 *            the ftpServer to set
	 */
	public void setFtpServer(DefaultFtpServer ftpServer) {
		this.ftpServer = ftpServer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.ftpserver.ftplet.UserManager#authenticate(org.apache.ftpserver
	 * .ftplet.Authentication)
	 */
	public User authenticate(Authentication authentication)
			throws AuthenticationFailedException {

		if ((authentication instanceof AnonymousAuthentication)
				&& (!ftpServer.getConnectionConfig().isAnonymousLoginEnabled()))
			throw new AuthenticationFailedException(
					"Anonymous authentication is not allowed.");

		if (authentication instanceof UsernamePasswordAuthentication) {
			// check username and pwd
			final UsernamePasswordAuthentication upAuth = (UsernamePasswordAuthentication) authentication;
			final String userName = upAuth.getUsername();

			FtpUser user;
			try {
				user = ftpUserDAO.findByUserName(userName);
			} catch (DAOException e) {
				throw new AuthenticationFailedException(e);
			}

			if (user.getPassword().equals(
					((UsernamePasswordAuthentication) authentication)
							.getPassword())) {

				return transcodeUser(user);
			}

		}

		throw new AuthenticationFailedException("Unable to authenticate user "
				+ authentication.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.ftpserver.ftplet.UserManager#delete(java.lang.String)
	 */
	public void delete(String userId) throws FtpException {
		try {
			ftpUserDAO.delete(userId);
			this.deleteFtpUserDir(userId);
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			logger.info("ERROR :" + e.getMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info("ERROR :" + e.getMessage());
		}
	}

	private void deleteFtpUserDir(String userId) throws Exception {
		File homeDirectory = new File(ftpRootDir, userId);
		if (homeDirectory.exists()) {
			if (!deleteDir(homeDirectory))
				throw new Exception("Error to delete "
						+ homeDirectory.getAbsolutePath());
		}
	}

	private boolean deleteDir(File dir) {
		// First delete all files and subdirectories
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so delete it
		return dir.delete();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.ftpserver.ftplet.UserManager#doesExist(java.lang.String)
	 */
	public boolean doesExist(String arg0) throws FtpException {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.ftpserver.ftplet.UserManager#getAdminName()
	 */
	public String getAdminName() throws FtpException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.ftpserver.ftplet.UserManager#getAllUserNames()
	 */
	public String[] getAllUserNames() throws FtpException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.ftpserver.ftplet.UserManager#getUserByName(java.lang.String)
	 */
	public User getUserByName(String arg0) throws FtpException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.ftpserver.ftplet.UserManager#isAdmin(java.lang.String)
	 */
	public boolean isAdmin(String arg0) throws FtpException {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.ftpserver.ftplet.UserManager#save(org.apache.ftpserver.ftplet
	 * .User)
	 */
	public void save(User arg0) throws FtpException {
		try {
			if (!checkUser(((FtpUser) arg0).getUserId()))
				ftpUserDAO.save((FtpUser) arg0);
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			logger.log(Level.INFO, "Error :" + e.getMessage());
		}

	}

	private FtpUser transcodeUser(FtpUser user) {
		File homeDirectory = new File(ftpRootDir, user.getUserId());

		if (!homeDirectory.exists()) {
			if (!homeDirectory.mkdir())
				throw new IllegalStateException(
						"Unalbe to create ftp home dir dir at "
								+ homeDirectory.getAbsolutePath()
								+ " for user " + user.getUserId());
		}

		user.setHomeDirectory(homeDirectory.getAbsolutePath());
		final List<Authority> auths = new ArrayList<Authority>();

		// for the moment they are all enabled with write permission
		if (user.isWritePermission()) {
			final Authority authW = new WritePermission();
			auths.add(authW);
		}

		// concurrent logins
		auths.add(new ConcurrentLoginPermission(user.getMaxLoginNumber(), user
				.getMaxLoginPerIp()));

		// up and download rates
		auths.add(new TransferRatePermission(user.getDownloadRate(), user
				.getUploadRate()));

		user.setAuthorities(auths);
		return user;
	}

	public boolean checkUser(String userId) {
		try {
			FtpUser ftpUserToSearch = ftpUserDAO.findByUserName(userId);
			if (ftpUserToSearch != null)
				return true;
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			logger.info("ERROR : " + e.getMessage());
		}
		return false;
	}

	public List<FtpUser> getAllUsers() throws DAOException {
		return ftpUserDAO.findAll();
	}
}
