/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.geo-solutions.it/
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geobatch.flow.event.action;

/**
 * Generic Exception thrown by {@link Action#execute(java.util.Queue) 
 *
 * @author ETj <etj at geo-solutions.it>
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 */
public class ActionException extends Exception {

	/**
	 * The Action that threw the Exception.</br>
	 */
	private Class type;

	public <A extends Action> ActionException(Class<A> type, String message) {
		super(message);
		setType(type);
	}
	
	public ActionException(Action action, String message) {
		super(message);
		setType(action);
	}
	
	private <T> void setType(Object type){
		if (type==null)
			this.type=Action.class;
		else {
			if (type instanceof Class)
				this.type = (Class)type;
			else
				this.type = type.getClass();
		}
	}

	public ActionException(Class<Action> type, String message, Throwable cause) {
		super(message, cause);
		setType(type);
	}
	
	public ActionException(Action action, String message, Throwable cause) {
		super(message, cause);
		setType(action);
	}

	/**
	 * @return The Class of the Action that threw the Exception.
	 */
	public Class<Action> getType() {
		return type;
	}
}
