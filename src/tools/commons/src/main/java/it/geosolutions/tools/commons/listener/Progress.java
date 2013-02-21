/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.geosolutions.tools.commons.listener;

import java.io.Serializable;

/**
 * Monitor the progress of some lengthly operation, and allows cancelation.
 * This interface makes no assumption about the output device. Additionnaly, this
 * interface provides support for non-fatal warning and exception reports.
 * <p>
 * All implementations should be multi-thread safe, even the ones that provide
 * feedback to a user interface thread.
 *
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 * 
 * @since 1.0.1
 *
 * 
 */
public interface Progress<T extends Serializable > {

    /**
     * Sets the description of the current task being performed. This method is usually invoked
     * before any progress begins. However, it is legal to invoke this method at any time during
     * the operation, in which case the description display is updated without any change to the
     * percentage accomplished.
     *
     * @param task Description of the task being performed, or {@code null} if none.
     */
    void onNewTask(T task);

    /**
     * Notifies this listener that the operation begins.
     */
    void onStart();

    /**
     * Notifies this listener of progress in the lengthly operation. Progress are reported
     * as a value between 0 and 100 inclusive. Values out of bounds will be clamped.
     *
     * @param percent The progress as a value between 0 and 100 inclusive.
     * 
     */
    void onUpdateProgress(float percent);


    /**
     * Notifies this listener that the operation has finished. The progress indicator will
     * shows 100% or disappears, at implementor choice. If warning messages were pending,
     * they will be displayed now.
     */
    void onCompleted();

    /**
     * Releases any resources used by this listener. If the progress were reported in a window,
     * this window may be disposed.
     */
    void onDispose();

    /**
     * Indicates that task should be cancelled.
     */
    void onCancel();

    /**
     * Reports a warning. This warning may be {@linkplain java.util.logger.Logger logged}, printed
     * to the {@linkplain System#err standard error stream}, appears in a windows or be ignored,
     * at implementor choice.
     *
     * @param source
     *          Name of the warning source, or {@code null} if none. This is typically the
     *          filename in process of being parsed or the URL of the data being processed
     * @param location
     *          Text to write on the left side of the warning message, or {@code null} if none.
     *          This is typically the line number where the error occured in the {@code source}
     *          file or the feature ID of the feature that produced the message
     * @param warning
     *          The warning message.
     */
    void onWarningOccurred(String source, String location, String warning);

    /**
     * Reports an exception. This method may prints the stack trace to the {@linkplain System#err
     * standard error stream} or display it in a dialog box, at implementor choice.
     *
     * @param exception The exception to report.
     */
    void onExceptionOccurred(Throwable exception);
    
}