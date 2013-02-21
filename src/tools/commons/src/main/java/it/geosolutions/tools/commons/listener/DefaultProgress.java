/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.geosolutions.tools.commons.listener;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the interface {@link Progress}
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class DefaultProgress implements Progress<String> {

	private final Logger LOGGER;

	private final String name;

	private volatile float progress = 0;

	public void onNewTask(String task) {
		if (LOGGER.isInfoEnabled())
			LOGGER.info(new StringBuilder(name).append(" [task=").append(task)
					.append("]").toString());
	}

	public DefaultProgress(String name) {
		super();
		LOGGER = LoggerFactory.getLogger(name);
		this.name = name;
	}

	public DefaultProgress() {
		super();
		this.name = this.getClass().getSimpleName();
		LOGGER = LoggerFactory.getLogger(name);

	}

	/**
	 * {@link Progress#onStart()}
	 */
	public void onStart() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(new StringBuilder(name).append(" [ start ]").toString());
		}
	}

	/**
	 * {@link Progress#onUpdateProgress(float)}
	 */
	public void onUpdateProgress(float percent) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(new StringBuilder(name).append(" [old_progress=")
					.append(progress).append(", new_progress=").append(percent)
					.append("]").toString());
		}
	}

	/**
	 * {@link Progress#onCompleted()}
	 */
	public void onCompleted() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(new StringBuilder(name).append(" [completed=")
					.append("true").append("]").toString());
		}
	}

	/**
	 * {@link Progress#onDispose()}
	 */
	public void onDispose() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(new StringBuilder(name).append("[ dispose ]")
					.toString());
		}
	}

	/**
	 * {@link Progress#onCancel()}
	 */
	public void onCancel() {
		if (LOGGER.isInfoEnabled())
			LOGGER.info(new StringBuilder(name).append(" [ canceled ]")
					.toString());
	}

	/**
	 * {@link Progress#onExceptionOccurred(Throwable)}
	 */
	public void onExceptionOccurred(Throwable exception) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(new StringBuilder(name).append(" EXCEPTION [message=")
					.append(exception.getLocalizedMessage()).append("]")
					.toString(), exception);
		}
	}

	/**
	 * {@link Progress#onWarningOccurred(String, String, String)}
	 */
	public void onWarningOccurred(String source, String location, String warning) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(new StringBuilder(name).append("  [warning=")
					.append(new Warning(source, location, warning).toString())
					.append("]").toString());
		}
	}

}
