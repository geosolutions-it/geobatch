/*
 */

package it.geosolutions.geobatch.sas.event;

import java.io.File;

/**
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class SASTrackEvent extends SASEvent {

	/**
	 * Generated Serial UID
	 */
	private static final long serialVersionUID = 7269583773345698296L;

	public SASTrackEvent(File source) {
        super(source);
    }

}
