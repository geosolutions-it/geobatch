/*
 * $Header: it.geosolutions.geobatch.ftp.server.dao.FtpPropsDAO,v. 0.1 13/ott/2009 09.57.52 created by giuseppe $
 * $Revision: 0.1 $
 * $Date: 13/ott/2009 09.57.52 $
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

import it.geosolutions.geobatch.users.dao.DAOException;

import java.util.List;

/**
 * @author ETj
 */
public interface FtpUserDAO { // extends GenericDAO<FtpUser, Long> {

	public FtpUser findByUserName(String userName) throws DAOException;

	public FtpUser findByUserId(Long userId) throws DAOException;

	public List<FtpUser> findAll() throws DAOException;

	public FtpUser save(FtpUser props) throws DAOException;

	public void delete(Long id) throws DAOException;

	public boolean existsUser(Long id) throws DAOException;

	public boolean existsUser(String name) throws DAOException;

}
