/*
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
package it.geosolutions.geobatch.ftpserver.dao.hibernate;

import it.geosolutions.geobatch.ftpserver.dao.FtpServerConfigDAO;

import it.geosolutions.geobatch.ftpserver.model.FtpServerConfig;
import it.geosolutions.geobatch.users.dao.DAOException;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author etj
 */
public class HibFtpServerConfigDAO extends DAOAbstractSpring<FtpServerConfig, Long> implements
        FtpServerConfigDAO {

    private final static Logger LOGGER = LoggerFactory.getLogger(HibFtpServerConfigDAO.class.getName());

    public HibFtpServerConfigDAO() {
        super(FtpServerConfig.class);
        // TODO Auto-generated constructor stub
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void save(FtpServerConfig props) throws DAOException {
        super.makePersistent(props);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public FtpServerConfig load() throws DAOException {
        List<FtpServerConfig> list = super.findAll();
        switch (list.size()) {
        case 0:
            LOGGER.info("No FTP server config found. Storing a brand new one.");
            FtpServerConfig config = new FtpServerConfig();
            return super.makePersistent(config);

        case 1:
            return list.get(0);

        default:
            LOGGER.error("Too many FTP server configs found(" + list.size() + ").");
            return list.get(0);
        }

    }
}
