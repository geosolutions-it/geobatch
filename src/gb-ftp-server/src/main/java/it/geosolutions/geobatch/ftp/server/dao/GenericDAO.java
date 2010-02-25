/*
 * $Header: it.geosolutions.geobatch.ftp.server.dao.GenericDAO,v. 0.1 13/ott/2009 09.53.26 created by giuseppe $
 * $Revision: 0.1 $
 * $Date: 13/ott/2009 09.53.26 $
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
package it.geosolutions.geobatch.ftp.server.dao;

import java.io.Serializable;
import java.util.List;

/**
 * @author giuseppe
 * 
 */
public interface GenericDAO<T, ID extends Serializable> {

	public T findById(ID id, boolean lock) throws DAOException;

	public List<T> findAll() throws DAOException;

	public List<T> findAll(int offset, int limit) throws DAOException;

	public T makePersistent(T entity) throws DAOException;

	public void makeTransient(T entity) throws DAOException;

	public void lock(T entity) throws DAOException;
}
