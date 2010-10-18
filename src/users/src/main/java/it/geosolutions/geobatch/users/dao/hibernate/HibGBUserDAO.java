/*
 * $Header: it.geosolutions.geobatch.ftp.server.dao.hibernate.HibGBUserDAO,v. 0.1 13/ott/2009 10.02.48 created by giuseppe $
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
package it.geosolutions.geobatch.users.dao.hibernate;

import it.geosolutions.geobatch.users.dao.DAOException;
import it.geosolutions.geobatch.users.dao.GBUserDAO;
import it.geosolutions.geobatch.users.model.GBUser;
import it.geosolutions.geobatch.users.model.GBUserRole;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 */
public class HibGBUserDAO extends DAOAbstractSpring<GBUser, Long> implements
		GBUserDAO, InitializingBean {

	private final static Logger LOGGER = Logger.getLogger(HibGBUserDAO.class
			.getName());

	public HibGBUserDAO() {
		super(GBUser.class);
	}

	/**
	 * Creates the default admin if no admin user is found.
	 * 
	 * @throws Exception
	 */
	protected void initDao() throws Exception {
		super.initDao();

		if (!existsAdmin()) {
			LOGGER.info("Admin user does not exist. Creating default one.");
			GBUser user = new GBUser();
			user.setName("admin");
			user.setPassword("admin");
			user.setEnabled(true);
			user.setRole(GBUserRole.ROLE_ADMIN);
			save(user);
		}
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	public GBUser findByUserId(Long userId) throws DAOException {
		return (GBUser) getHibernateTemplate().get(GBUser.class, userId);
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	public GBUser findByUserName(String userName) throws DAOException {
		List<GBUser> users = super.findByCriteria(Restrictions.eq("name",
				userName));
		if (users.size() > 0)
			return users.get(0);
		return null;
	}

	public boolean existsAdmin() throws DAOException {
		List<GBUser> users = super.findByCriteria(Restrictions.eq("role",
				GBUserRole.ROLE_ADMIN));
		return !users.isEmpty();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(final Long id) throws DAOException {
		try {
			getHibernateTemplate().execute(new HibernateCallback() {

				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Query query = session
							.createQuery("delete from User user where user.id = :id");
					query.setParameter("id", id);
					query.executeUpdate();
					return null;
				}
			});
		} catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(GBUser ftpUser) throws DAOException {
		super.makeTransient(ftpUser);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public GBUser save(GBUser ftpUser) throws DAOException {
		return super.makePersistent(ftpUser);
	}

}
