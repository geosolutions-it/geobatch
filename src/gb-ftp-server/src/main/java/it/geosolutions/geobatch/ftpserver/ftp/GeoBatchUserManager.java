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
package it.geosolutions.geobatch.ftpserver.ftp;

import it.geosolutions.geobatch.catalog.file.FileBaseCatalog;
import it.geosolutions.geobatch.ftpserver.model.FtpServerConfig;
import it.geosolutions.geobatch.global.CatalogHolder;
import it.geosolutions.geobatch.users.dao.DAOException;
import it.geosolutions.geobatch.users.dao.UserFlowAccessDAO;

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
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

/**
 * @author giuseppe
 * @author ETj
 * 
 */
public class GeoBatchUserManager implements UserManager {

	private Logger logger = Logger.getLogger(GeoBatchUserManager.class.getName());

//	private File ftpRootDir;

	private FtpUserDAO ftpUserDAO;

	private UserFlowAccessDAO userFlowAccess;
	
	private FtpServerConfig serverConfig;

	public GeoBatchUserManager() {
//		String baseDir = ((FileBaseCatalog) CatalogHolder.getCatalog())
//				.getBaseDirectory();
//		ftpRootDir = new File(baseDir, "FTP");
	}

	private File getFtpRootDir() {
        if(serverConfig.getFtpBaseDir() != null) {
            return new File(serverConfig.getFtpBaseDir());
        } else {
            return new File( ((FileBaseCatalog) CatalogHolder.getCatalog())
                    .getBaseDirectory(), "FTP");
        }
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
				&&  ! serverConfig.isAnonEnabled())
			throw new AuthenticationFailedException(
					"Anonymous authentication is not allowed.");

		if (authentication instanceof UsernamePasswordAuthentication) {
			// check username and pwd
			final UsernamePasswordAuthentication upAuth = (UsernamePasswordAuthentication) authentication;
			final String userName = upAuth.getUsername();

			FtpUser user = null;
			try {
				user = ftpUserDAO.findByUserName(userName);
			} catch (DAOException e) {
				throw new AuthenticationFailedException(e);
			}

			if (user != null &&
				user.getPassword().equals(((UsernamePasswordAuthentication) authentication).getPassword())) {
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
			ftpUserDAO.delete(new Long(userId));
			this.deleteFtpUserDir(userId);
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			logger.info("ERROR :" + e.getMessage());
		} catch (NumberFormatException e) {
			logger.info("ID needed, not user name:" + e.getMessage()); // just to remember it :P
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.info("ERROR :" + e.getMessage());
		}
	}

	private void deleteFtpUserDir(String userId) throws Exception {
		File homeDirectory = new File(getFtpRootDir(), userId);
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
        try {
            List<FtpUser> users = ftpUserDAO.findAll(); // TODO: this call is heavy, coz it joins by hand ftp props
            String[] ret = new String[users.size()];
            int cnt = 0;
            for (FtpUser ftpUser : users) {
                ret[cnt++] = ftpUser.getName();
            }
            return ret;
        } catch (DAOException ex) {
            throw new FtpException("Can't retrieve users.", ex);
        }
	}
	
	public List<FtpUser> getAllUsers() throws DAOException {
		return ftpUserDAO.findAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.ftpserver.ftplet.UserManager#getUserByName(java.lang.String)
	 */
	public User getUserByName(String name) throws FtpException {
		try {
			return ftpUserDAO.findByUserName(name);
		} catch(DAOException ex) {
			throw new FtpException(ex);
		}
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
	public void save(User user) throws FtpException {
		if(! ( user instanceof FtpUser)) {
			throw new FtpException("Bad user class to save: ["+user.getClass()+"]" + user);
		}
		try {
			FtpUser ftpUser = (FtpUser)user;
			ftpUserDAO.save(ftpUser); // atm this will call a saveOrUpdate() which is the right behaviour
			                          // when using JPA pls ensure the right behaviour will be enforced
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			logger.log(Level.WARNING, "Error saving user("+user+"):" + e.getMessage(), e);
		}
	}

	private FtpUser transcodeUser(FtpUser user) {
		File homeDirectory = new File(getFtpRootDir(), user.getRelativeHomeDir());

		// ETj: maybe this is not the right place to create an home dir 
		if (!homeDirectory.exists()) {
			if (!homeDirectory.mkdir()) {
                logger.warning("Unable to create ftp home dir at "
								+ homeDirectory.getAbsolutePath()
								+ " for user " + user.getName());

				throw new IllegalStateException(
						"Unable to create ftp home dir at "
								+ homeDirectory.getAbsolutePath()
								+ " for user " + user.getName());
            }
		}

		try {
			List<String> allowedFlows = userFlowAccess.findFlows(user.getId());
			
			for (String flowId : allowedFlows) {
				File flowDir = new File(homeDirectory, flowId);
				
				if (!flowDir.exists()) {
					if (!flowDir.mkdir()) {
		                logger.warning("Unable to create ftp flow dir at "
										+ flowDir.getAbsolutePath()
										+ " for user " + user.getName());

						throw new IllegalStateException(
								"Unable to create ftp flow dir at "
										+ flowDir.getAbsolutePath()
										+ " for user " + user.getName());
		            }
				}
			}
		} catch (DAOException e) {
			throw new IllegalStateException(
					"Unable to retrieve allowed flows"
							+ " for user " + user.getName());
		}
		
		user.setHomeDirectory(homeDirectory.getAbsolutePath());
		final List<Authority> auths = new ArrayList<Authority>();

		// for the moment they are all enabled with write permission (ETj:: ???)
		if (user.isWritePermission()) {
			final Authority authW = new WritePermission();
			auths.add(authW);
		}

		// concurrent logins
		auths.add(new ConcurrentLoginPermission(user.getMaxLoginNumber(),
												user.getMaxLoginPerIp()));

		// up and download rates
		auths.add(new TransferRatePermission(user.getDownloadRate(),
											 user.getUploadRate()));

        // TODO: add constraints on file size

		user.setAuthorities(auths);
        logger.info("TRANSCODED USER " + user);
		return user;
	}

	public boolean doesExist(Long id) {
		try {
			return ftpUserDAO.existsUser(id);
		} catch (DAOException e) {
			logger.log(Level.INFO, "ERROR : " + e.getMessage(), e);
		}
		return false;
	}

	public boolean doesExist(String name) throws FtpException {
		try {
			return ftpUserDAO.existsUser(name);
		} catch (DAOException e) {
			logger.log(Level.INFO, "ERROR : " + e.getMessage(), e);
		}
		return false;
	}

	/**
	 * @param ftpUserDAO
	 *            the ftpUser to set
	 */
	public void setFtpUserDAO(FtpUserDAO ftpUserDAO) {
		this.ftpUserDAO = ftpUserDAO;
	}

    /**
	 * @param userFlowAccess the userFlowAccess to set
	 */
	public void setUserFlowAccess(UserFlowAccessDAO userFlowAccess) {
		this.userFlowAccess = userFlowAccess;
	}

	public void setServerConfig(FtpServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

}
