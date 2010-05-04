/*
 */

package it.geosolutions.geobatch.sas.event;

import it.geosolutions.opensdi.sas.model.Type;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class SASMosaicEvent extends SASEvent {

    /**
	 * Generated Serial UID
	 */
	private static final long serialVersionUID = 1324450904568395490L;

	private List<String> legNames;
	private Type type;
	
	public SASMosaicEvent(File source) {
        super(source);
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
