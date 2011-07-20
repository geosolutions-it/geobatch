/*
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

import it.geosolutions.geobatch.users.model.UserFlowAccess;

import java.util.List;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public interface UserFlowAccessDAO extends GenericDAO<UserFlowAccess, String> {

    public void add(Long userId, String flowId) throws DAOException;

    public void remove(Long userId, String flowName) throws DAOException;

    public void remove(Long userId) throws DAOException;

    public void remove(String flowId) throws DAOException;

    /**
     * @returns all the flownames associated with the user <TT>userId</TT>
     */
    public List<String> findFlows(Long userId) throws DAOException;

    /**
     * @returns all the userIds associated with the flow <TT>flowId</TT>
     */
    public List<Long> findUsersId(String flowId) throws DAOException;
}
