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

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Thread-safe implementation of list of Progress Listener can be used to propagate events to all
 * the registered list of progress listeners
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @param <T>
 *            the serializable type for tasks
 * 
 * @see DefaultProgress
 */
public final class ProgressList<T extends Serializable> implements Progress<T> {

	private final List<Progress<T>> listeners = new LinkedList<Progress<T>>();

	/**
	 * 
	 * @param listener
	 *            the listener to add
	 * @return as specified {@link Collection#add(Object)}
	 */
	public synchronized boolean addListener(Progress<T> listener) {
		return listeners.add(listener);
	}

	public synchronized void onNewTask(T task) {
		for (Progress<T> p : listeners) {
			p.onNewTask(task);
		}
	}

	public synchronized void onStart() {
		for (Progress<T> p : listeners) {
			p.onStart();
		}
	}

	public synchronized void onUpdateProgress(float percent) {
		for (Progress<T> p : listeners) {
			p.onUpdateProgress(percent);
		}
	}

	public synchronized void onCompleted() {
		for (Progress<T> p : listeners) {
			p.onCompleted();
		}
	}

	public synchronized void onDispose() {
		for (Progress<T> p : listeners) {
			p.onDispose();
		}
	}

	// public boolean isCanceled() {
	// boolean res=false;
	// for (Progress<T> p: listeners){
	// res=res||p.isCanceled();
	// }
	// return res;
	// }

	public synchronized void onCancel() {
		for (Progress<T> p : listeners) {
			p.onCancel();
		}
	}

	public synchronized void onWarningOccurred(String source, String location,
			String warning) {
		for (Progress<T> p : listeners) {
			p.onWarningOccurred(source, location, warning);
		}

	}

	public synchronized void onExceptionOccurred(Throwable exception) {
		for (Progress<T> p : listeners) {
			p.onExceptionOccurred(exception);
		}
	}

}