/*
 * $Header: it.geosolutions.geobatch.ais.dao.hibernate.DAOAbstractSpring,v. 0.1 06/ott/2009 12.33.33 created by Francesco $
 * $Revision: 0.1 $
 * $Date: 06/ott/2009 12.33.33 $
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

import it.geosolutions.geobatch.ais.dao.DAOException;
import it.geosolutions.geobatch.ais.dao.GenericDAO;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.criterion.Criterion;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * @author Francesco Izzi
 * 
 */
public abstract class DAOAbstractSpring<T, ID extends Serializable> extends
		HibernateDaoSupport implements GenericDAO<T, ID> {

	private static Logger logger = Logger.getLogger(DAOAbstractSpring.class
			.getName());

	private Class<T> persistentClass;

	public DAOAbstractSpring(Class<T> persistentClass) {
		logger.finest("Persistent Class : " + persistentClass);
		this.persistentClass = persistentClass;
	}

	protected Class<T> getPersistentClass() {
		logger.info("Persistent class: " + persistentClass.getName());
		return persistentClass;
	}

	public List<T> findAll() throws DAOException {
		return findByCriteria();
	}

	public List<T> findAll(int offset, int limite) throws DAOException {
		return findByCriteria(offset, limite);
	}

	@SuppressWarnings("unchecked")
	public List<T> findByCriteria(Criterion... criterion) throws DAOException {
		try {
			Criteria crit = getSession().createCriteria(getPersistentClass());
			for (Criterion c : criterion) {
				crit.add(c);
			}
			return crit.list();
		} catch (HibernateException ex) {
			logger.fine(ex.getMessage());
			throw new DAOException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public List<T> findByCriteria(int offset, int limite,
			Criterion... criterion) throws DAOException {
		try {
			Criteria crit = getSession().createCriteria(getPersistentClass());
			for (Criterion c : criterion) {
				crit.add(c);
			}
			crit.setFirstResult(offset);
			crit.setMaxResults(limite);
			return crit.list();
		} catch (HibernateException ex) {
			logger.fine(ex.getMessage());
			throw new DAOException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	public T findById(ID id, boolean lock) throws DAOException {
		T entity;
		try {
			if (lock) {
				entity = (T) getSession().load(getPersistentClass(), id,
						LockMode.UPGRADE);
			} else {
				entity = (T) getSession().load(getPersistentClass(), id);
			}
		} catch (HibernateException ex) {
			logger.fine(ex.getMessage());
			throw new DAOException(ex);
		}
		return entity;

	}

	public void lock(T entity) throws DAOException {
		try {
			getSession().lock(entity, LockMode.UPGRADE);
		} catch (HibernateException ex) {
			logger.fine(ex.getMessage());
			throw new DAOException(ex);
		}
	}

	public T makePersistent(T entity) throws DAOException {
		try {
			getSession().saveOrUpdate(entity);
		} catch (HibernateException ex) {
			logger.fine(ex.getMessage());
			throw new DAOException(ex);
		}
		return entity;
	}

	public void makeTransient(T entity) throws DAOException {
		try {
			getSession().delete(entity);
		} catch (HibernateException ex) {
			logger.fine(ex.getMessage());
			throw new DAOException(ex);
		}
	}

}
