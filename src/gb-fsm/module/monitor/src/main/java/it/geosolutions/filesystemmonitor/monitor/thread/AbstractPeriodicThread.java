package it.geosolutions.filesystemmonitor.monitor.thread;

import java.util.logging.Level;

/**
 * This is the timer thread which is executed every n milliseconds according to the setting of the file monitor. It investigates the file in question and notify listeners if changed.
 * @author   SImone Giannecchini
 */
public abstract class AbstractPeriodicThread extends AbstractPausableThread {

	/**
	 * Interval at which we will check the file system.
	 * @uml.property  name="pollingInterval"
	 */
	protected long pollingInterval = 10;

	public AbstractPeriodicThread(String name, final long pollingInterval) {
		super(name);
		this.pollingInterval = pollingInterval;

	}

	/**
	 * @return
	 * @uml.property  name="pollingInterval"
	 */
	public long getPollingInterval() {
		return pollingInterval;
	}

	public void run() {
		isRunning = true;

		while (true) {
			if (isTerminationRequested()) {
				break;
			}

			try {
				if (!execute())
					break;
			} catch (Exception e1) {
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
				break;
			}

			try {
				while (isPauseRequested()) {
					pauseRequested = false;
					isPaused = true;
					synchronized (this) {
						this.wait();
					}

					if (!pauseRequested)
						break;

				}
				synchronized (this) {
					this.wait(pollingInterval);
				}
			} catch (InterruptedException e) {
				if (LOGGER.isLoggable(Level.SEVERE))
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				break;
			}

		}
		isRunning = false;
	}

}