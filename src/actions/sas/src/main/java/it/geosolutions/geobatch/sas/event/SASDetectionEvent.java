/*
 */

package it.geosolutions.geobatch.sas.event;

import java.io.File;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class SASDetectionEvent extends SASEvent {

    /**
	 * Generated Serial UID
	 */
	private static final long serialVersionUID = 1546493272038183232L;

	public SASDetectionEvent(File source) {
        super(source);
    }

}
