/*
 * $Header: it.geosolutions.geobatch.ftp.server.dao.hibernate.HibFtpPropsDAO,v. 0.1 13/ott/2009 10.02.48 created by giuseppe $
 * $Revision: 0.1 $
 * $Date: 13/ott/2009 10.02.48 $
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
package it.geosolutions.geobatch.ftpserver.dao.hibernate;

import it.geosolutions.geobatch.ftpserver.dao.FtpPropsDAO;
import it.geosolutions.geobatch.ftpserver.model.FtpProps;

import it.geosolutions.geobatch.users.dao.DAOException;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author giuseppe
 * 
 */
public class HibFtpPropsDAO
		extends DAOAbstractSpring<FtpProps, Long>
		implements FtpPropsDAO {

	public HibFtpPropsDAO() {
		super(FtpProps.class);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(final Long id) throws DAOException {
        FtpProps props = super.findById(id, true);
        super.makeTransient(props);
//
//		try {super.
//			getHibernateTemplate().execute(new HibernateCallback() {
//
//				public Object doInHibernate(Session session)
//						throws HibernateException, SQLException {
//					Query query = session
//							.createQuery("delete from FtpProps props where props.id = :id");
//					query.setParameter("id", id);
//					query.executeUpdate();
//					return null;
//				}
//			});
//		} catch (HibernateException e) {
//			throw new DAOException(e);
//		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(FtpProps props) throws DAOException {
		super.makeTransient(props);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public FtpProps save(FtpProps props) throws DAOException {
		return super.makePersistent(props);
	}

}
