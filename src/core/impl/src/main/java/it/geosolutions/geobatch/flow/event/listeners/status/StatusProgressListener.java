/*
 */

package it.geosolutions.geobatch.flow.event.listeners.status;

import it.geosolutions.geobatch.flow.event.ProgressListener;
import java.util.List;

/**
 * Remember the state of the event firer.
 * 
 * <P>
 * You can retrieve the info using
 * <LI>{@link #is}</LI>
 * <LI>{@link #is}</LI>
 * <LI>{@link #is}</LI>
 * <LI>{@link #is}</LI>
 * <LI>{@link #is}</LI>
 * <LI>{@link #is}</LI>
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class StatusProgressListener extends
		ProgressListener<StatusProgressListenerConfiguration> {

	private Object source;

	protected boolean started = false;
	protected boolean paused = false;
	protected boolean failed = false;
	protected boolean completed = false;
	protected boolean terminated = false;

	protected Throwable failException = null;

	public StatusProgressListener(
			StatusProgressListenerConfiguration configuration) {
		super(configuration);
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public void started() {
		started = true;
	}

	public boolean isStarted() {
		return started;
	}

	/**
	 * This event should trigger some refresh on interactive displays, but we
	 * don't need it
	 */
	public void progressing() {
	}

	public void paused() {
		paused = true;
	}

	public boolean isPaused() {
		return paused;
	}

	public void resumed() {
		paused = false;
	}

	public void completed() {
		completed = true;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void failed(Throwable exception) {
		failed = true;
		failException = exception;
	}

	public boolean isFailed() {
		return failed;
	}

	public Throwable getFailException() {
		return failException;
	}

	public void terminated() {
		terminated = true;
	}

	public boolean isTerminated() {
		return terminated;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append("Status of ").append(
				source).append('[');

		sb.append("Last task: '").append(getTask()).append("' ");
		sb.append(" at ").append(getProgress()).append("% ");
		if (started)
			sb.append("started ");
		if (paused)
			sb.append("paused ");
		if (completed)
			sb.append("completed");
		if (failed)
			sb.append("failed (").append(failException.getMessage())
					.append(")");
		if (terminated)
			sb.append("terminated");
		sb.append(']');

		return sb.toString();
	}
}
