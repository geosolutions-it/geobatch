/*
 */

package it.geosolutions.geobatch.misc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles pausing and resuming in a multithreaded environment.
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class PauseHandler {

    
    final Lock lock = new ReentrantLock();

    
    final Condition pause = lock.newCondition();

    
    private boolean isPaused;

    // private AtomicBoolean isPaused = new AtomicBoolean(false);
    public PauseHandler(boolean paused) {
        this.isPaused = paused;
    }

    /**
     * 
     * @return false if was already paused.
     */
    public void pause() {
        isPaused = true;
    }

    public void resume() {
        lock.lock();
        try {
            isPaused = false;
            pause.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * @return
     * @uml.property  name="isPaused"
     */
    public boolean isPaused() {
        return isPaused;

    }

    /**
     * Blocking call: if paused, will block until a {@link resume()} is invoked. <BR>
     * Will not block if not paused.
     * 
     * @return true if the call blocked.
     */
    public boolean waitUntilResumed() {
        boolean wasBlocked = false;
        while (isPaused) {
            wasBlocked = true;
            lock.lock();
            try {
                pause.await();
            } catch (InterruptedException _) {
            } finally {
                lock.unlock();
            }
        }

        return wasBlocked;
    }
}