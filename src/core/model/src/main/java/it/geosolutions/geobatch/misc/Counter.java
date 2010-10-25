/*
 */

package it.geosolutions.geobatch.misc;

/**
 * 
 * @author ETj <etj at geo-solutions.it>
 */
public class Counter {
    private long next = 0;

    public Counter() {
    }

    public Counter(long start) {
        next = start;
    }

    public long getNext() {
        synchronized (this) {
            return next++;
        }
    }
}
