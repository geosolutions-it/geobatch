/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  https://github.com/nfms4redd/nfms-geobatch
 *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geobatch.imagemosaic;

import static org.junit.Assert.assertTrue;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemEventType;
import it.geosolutions.geobatch.flow.event.IProgressListener;
import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;
import it.geosolutions.geobatch.flow.event.action.ActionException;

import java.io.File;
import java.io.IOException;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

/**
 * This tests suite tests if the exception handling in ImageMosaicAction is done correctly.
 * TODO add more tests.
 * 
 * @author DamianoG
 * 
 */
public class ExceptionHandlingTest {

    /**
     * Call the ImageMosaicAction providing a not existent file event,
     * an ActionException must be returned because failIgnored equals false (Default value)
     * @throws Exception
     */
    @Test
    public void testDefaultExceptionHandling() throws Exception {

        try {
            File imcFile = new File("afileThatNotExist");
            Queue<EventObject> inputQ = new LinkedList<EventObject>();
            inputQ.add(new FileSystemEvent(imcFile, FileSystemEventType.FILE_ADDED));
            ImageMosaicAction action = createDefaultMosaicAction();
            Queue<EventObject> outputQ = action.execute(inputQ);
        } catch (ActionException e) {
            assertTrue(e.getMessage().startsWith("The input file does not exists at url:"));
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Call the ImageMosaicAction providing a not existent file event,
     * no exception must be returned because fail Ignored equals true so the method ActionExceptionHandler.handleError(...)
     * only log a WARNING message, then in the body of the Action a continue statement skip the current event.
     * 
     * @throws Exception
     */
    @Test
    public void testFailIgnoreExceptionHandling() throws Exception {

        try {
            File imcFile = new File("afileThatNotExist");
            Queue<EventObject> inputQ = new LinkedList<EventObject>();
            inputQ.add(new FileSystemEvent(imcFile, FileSystemEventType.FILE_ADDED));
            ImageMosaicAction action = createFailIgnoredMosaicAction();
            Queue<EventObject> outputQ = action.execute(inputQ);
        } catch (Exception e) {
            throw e;
        }
    }

    private ImageMosaicAction createDefaultMosaicAction() throws IOException {
        // config
        ImageMosaicConfiguration conf = new ImageMosaicConfiguration("aTestConf", "aTestConf",
                "aTestConf");
        ImageMosaicAction action = new ImageMosaicAction(conf);
        IProgressListener ipl = new ProgressListenerForwarder(action);
        action.addListener(ipl);
        return action;
    }

    private ImageMosaicAction createFailIgnoredMosaicAction() throws IOException {
        // config
        ImageMosaicConfiguration conf = new ImageMosaicConfiguration("aTestConf", "aTestConf",
                "aTestConf");
        conf.setFailIgnored(true);
        ImageMosaicAction action = new ImageMosaicAction(conf);
        IProgressListener ipl = new ProgressListenerForwarder(action);
        action.addListener(ipl);
        return action;
    }

}
