package it.geosolutions.geobatch.ais.dao;

import java.io.Serializable;

public class DAOException extends Exception implements Serializable {

	private static final long serialVersionUID = -6648711041950295611L;

	private String message;

	public DAOException() {
	}

	public DAOException(String message) {
		this.message = message;
	}

	public DAOException(Exception e) {
		super(e);
	}

	public DAOException(Throwable e) {
		super(e);
	}

	public String getMessage() {
		return message;
	}

}
