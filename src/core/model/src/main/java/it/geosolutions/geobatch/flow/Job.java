/*
 */

package it.geosolutions.geobatch.flow;

/**
 * Anything that can be run, paused and resumed.
 *
 * @author ETj <etj at geo-solutions.it>
 */
public interface Job {

    /**
     * Pause the execution of a Job.<BR>
     * Implementations may also completely ignore the <TT>pause()</TT> call --
     * for instance the job may not be split in execution steps, loop iterations and so on;
     * in this case the <TT>pause()</TT> invocation may simply return <TT>false</TT>, indicating to the caller
     * that the job was not paused.
     *
     * @return true if the Job has been successfully paused.
     */

    public boolean pause();

    /**
     * Pause the execution of a Job and optionally all of its subjobs.
     * <BR>A <TT>Job</TT> may control other subjobs running asynchronally.
     * <BR>When <TT>sub</TT> is true, also the subjobs will be paused.
     * 
     * <P><TT>pause(false)</TT> is equivalent to <TT>pause()</TT>
     *
     * @param sub when <TT>true</TT>, also subtasks will be paused.
     *
     * @return <TT>true</TT> if the Job has been successfully paused, disregarding any return values from its subjobs.
     */
    public boolean pause(boolean sub);

    /**
     * Resume a previously paused job.
     * <BR>If the instance does not support pausing or it was not paused, nothing
     * will happen.
     */
    public void resume();

    public boolean isPaused();

}
