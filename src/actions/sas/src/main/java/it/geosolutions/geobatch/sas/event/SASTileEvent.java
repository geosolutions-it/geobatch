/*
 */

package it.geosolutions.geobatch.sas.event;

import it.geosolutions.opensdi.sas.model.Type;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class SASTileEvent extends SASEvent {

    /**
	 * Generated Serial UID
	 */
	private static final long serialVersionUID = -1897987452573996213L;
	
	private Date date;
	private List<String> legNames;
	private Type type;

    public SASTileEvent(File source) {
        super(source);
    }

    /**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return the legNames
	 */
	public List<String> getLegNames() {
		if (legNames == null)
			legNames = new ArrayList<String>();
		
		return legNames;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}
}
