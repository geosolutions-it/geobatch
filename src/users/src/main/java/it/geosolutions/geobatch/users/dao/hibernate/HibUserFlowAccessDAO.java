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
package it.geosolutions.geobatch.users.dao.hibernate;

import it.geosolutions.geobatch.users.dao.DAOException;
import it.geosolutions.geobatch.users.dao.UserFlowAccessDAO;
import it.geosolutions.geobatch.users.model.UserFlowAccess;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 */
@Transactional
public class HibUserFlowAccessDAO extends
		DAOAbstractSpring<UserFlowAccess, String> implements UserFlowAccessDAO {

	private final static Logger LOGGER = Logger
			.getLogger(HibUserFlowAccessDAO.class.getName());

	public HibUserFlowAccessDAO() {
		super(UserFlowAccess.class);
	}

	/**
     */
	protected void initDao() throws Exception {
		super.initDao();
	}

	public void add(Long userId, String flowId) throws DAOException {
		UserFlowAccess flowAccess = new UserFlowAccess(userId, flowId);
		super.makePersistent(flowAccess);
	}

	public void remove(Long userId, String flowName) throws DAOException {
		UserFlowAccess flowAccess = new UserFlowAccess(userId, flowName);
		super.makeTransient(flowAccess);
	}

	public void remove(final Long userId) throws DAOException {
		try {
			getHibernateTemplate().execute(new HibernateCallback() {

				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Query query = session
							.createQuery("delete from UserFlowAccess a where a.userId = :userId");
					query.setParameter("userId", userId);
					query.executeUpdate();
					return null;
				}
			});
		} catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

	public void remove(final String flowId) throws DAOException {
		try {
			getHibernateTemplate().execute(new HibernateCallback() {

				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Query query = session
							.createQuery("delete from UserFlowAccess a where a.flowId = :flowId");
					query.setParameter("flowId", flowId);
					query.executeUpdate();
					return null;
				}
			});
		} catch (HibernateException e) {
			throw new DAOException(e);
		}
	}

	public List<String> findFlows(Long userId) throws DAOException {
		List<UserFlowAccess> acc = super.findByCriteria(Restrictions.eq(
				"userId", userId));
		List<String> ret = new ArrayList<String>(acc.size());
		for (UserFlowAccess userFlowAccess : acc) {
			ret.add(userFlowAccess.getFlowId());
		}
		return ret;
	}

	public List<Long> findUsersId(String flowId) throws DAOException {
		List<UserFlowAccess> acc = super.findByCriteria(Restrictions.eq("flow",
				flowId));
		List<Long> ret = new ArrayList<Long>(acc.size());
		for (UserFlowAccess userFlowAccess : acc) {
			ret.add(userFlowAccess.getUserId());
		}
		return ret;
	}

}
