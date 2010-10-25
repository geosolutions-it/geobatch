/*
 * $Header: it.geosolutions.geobatch.ftp.server.dao.GBUserDAO,v. 0.1 13/ott/2009 09.57.52 created by giuseppe $
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
package it.geosolutions.geobatch.users.dao;

import it.geosolutions.geobatch.users.model.GBUser;

/**
 * @author giuseppe
 * 
 */
public interface GBUserDAO extends GenericDAO<GBUser, Long> {

    public GBUser findByUserId(Long userId) throws DAOException;

    public GBUser findByUserName(String userName) throws DAOException;

    public GBUser save(GBUser user) throws DAOException;

    public void delete(Long userId) throws DAOException;

    public void delete(GBUser user) throws DAOException;

}
