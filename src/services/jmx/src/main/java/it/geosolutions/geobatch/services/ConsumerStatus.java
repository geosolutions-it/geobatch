package it.geosolutions.geobatch.services;

import it.geosolutions.geobatch.flow.event.consumer.EventConsumerStatus;

/**
 * This class maps Evenent consumer statuses to JMX consumer status
 * @see {@link EventConsumerStatus}
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public enum ConsumerStatus {
    /**
     * Idle EventConsumerStatus CODE
     */
    IDLE,

    /**
     * Waiting EventConsumerStatus CODE
     */
    WAITING,

    /**
     * Processing EventConsumerStatus CODE
     */
    EXECUTING,

    /**
     * Finished OK EventConsumerStatus CODE
     */
    COMPLETED,

    /**
     * Finished OK EventConsumerStatus CODE
     */
    FAILED,
    /*
     * added by Carlo on 02 02 2011
     * these status are inherited from the EventConsumer
     * interface methods
     * isPaused()
     * isCanceled()
     */
    /**
     * isCanceled()
     */
    CANCELED,
    /**
     * isPaused()
     */
    PAUSED,
    
    /**
     * case: consumer UUID not found
     */
    UNRECOGNIZED;

    static ConsumerStatus getStatus(EventConsumerStatus status) {
        switch (status) {
        case IDLE:
            return ConsumerStatus.IDLE;
        case WAITING:
            return ConsumerStatus.WAITING;
        case PAUSED:
            return ConsumerStatus.PAUSED;
        case EXECUTING:
            return ConsumerStatus.EXECUTING;
        case COMPLETED:
            return ConsumerStatus.COMPLETED;
        case CANCELED:
            return ConsumerStatus.CANCELED;
        case FAILED:
            return ConsumerStatus.FAILED;
        }
        return ConsumerStatus.UNRECOGNIZED; 
    }

}
