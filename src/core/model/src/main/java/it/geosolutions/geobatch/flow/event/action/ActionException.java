/*
 */

package it.geosolutions.geobatch.flow.event.action;

/**
 * Generic Exception thrown by {@link Action#execute(java.util.Queue) 
 *
 * @author ETj <etj at geo-solutions.it>
 */
public class ActionException extends Exception {

    /**
     * The Action that threw the Exception.
     * <P>Maybe is not a good idea to store the Action here, because it may
     * hold heavy data. 
     */
    private Action action;

    public ActionException(Action action, String message) {
        super(message);
        this.action = action;
    }

    public ActionException(Action action, String message, Throwable cause) {
        super(message, cause);
        this.action = action;
    }

    /**
     * @return The Action that threw the Exception.
     */
    public Action getAction() {
        return action;
    }
}
