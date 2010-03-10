/*
 * $Header: it.geosolutions.geobatch.ais.dao.hibernate.DAOAISAnomaliesHibernate,v. 0.1 06/ott/2009 12.38.28 created by Francesco $
 * $Revision: 0.1 $
 * $Date: 06/ott/2009 12.38.28 $
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
package it.geosolutions.geobatch.ais.dao.hibernate;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.geosolutions.geobatch.ais.dao.DAOException;
import it.geosolutions.geobatch.ais.dao.AISAnomaliesDAO;
import it.geosolutions.geobatch.ais.model.AISAnomalies;

/**
 * @author Francesco
 * 
 */
public class DAOAISAnomaliesHibernate extends DAOAbstractSpring<AISAnomalies,Long>
		implements AISAnomaliesDAO {

	public DAOAISAnomaliesHibernate() {
		super(AISAnomalies.class);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public AISAnomalies save(AISAnomalies aisAnomalies) throws DAOException {
		return super.makePersistent(aisAnomalies);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(final String type) throws DAOException {
		try {
			getHibernateTemplate().execute(new HibernateCallback() {

				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Query query = session
							.createQuery("delete from AISAnomalies ais where ais.type = ?");
					query.setParameter(0, type);
					query.executeUpdate();
					return null;
				}
			});
		} catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

}
