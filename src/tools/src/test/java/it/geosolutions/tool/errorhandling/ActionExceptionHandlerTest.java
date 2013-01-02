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
package it.geosolutions.tool.errorhandling;

import it.geosolutions.geobatch.configuration.event.action.ActionConfiguration;
import it.geosolutions.geobatch.flow.event.action.ActionException;
import it.geosolutions.geobatch.flow.event.action.BaseAction;

import java.util.EventObject;
import java.util.Queue;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author DamianoG
 *
 */
public class ActionExceptionHandlerTest {

    @Test
    public void testExceptionHandler(){
        Exception res = null;
        try {
            ActionExceptionHandler.handleError(new TestConfiguration(),new TestAction(),"an error message.");
        } catch (ActionException e) {
            res = e;
            assertEquals("an error message.", res.getMessage());
        }
        assertTrue(res instanceof ActionException);
    }
    
    @Test
    public void testExceptionHandlerOwnerAndMsgNull(){
        Exception res = null;
        try {
            ActionExceptionHandler.handleError(new TestConfiguration(),null,null);
        } catch (ActionException e) {
            res = e;
            assertEquals("An error occurred. No details are avaiable.", res.getMessage());
        }
        assertTrue(res instanceof ActionException);
    }
    
    /**
     * This test check when a null configuration is provided. must throw an IllegalArgumentException
     */
    @Test
    public void testExceptionHandlerConfigNull(){
        Exception res = null;
        try {
            ActionExceptionHandler.handleError(null,new TestAction(),"a message");
        } catch (ActionException e) {
            res = e;
        }
    }
    
    @Test
    public void testExceptionHandlerWithFailIgnored(){
        Exception res = null;
        TestConfiguration testConf = new TestConfiguration();
        testConf.setFailIgnored(true);
        try {
            ActionExceptionHandler.handleError(testConf,new TestAction(),"an error message.");
        } catch (ActionException e) {
            fail();
        }
        assertTrue(res == null);
    }
    
    private class TestConfiguration extends ActionConfiguration{

        public TestConfiguration() {
            super("testID", "testName", "A test Configuration");
        }
        
    }
    
    private class TestAction extends BaseAction<EventObject> {

        /**
         * @param actionConfiguration
         */
        public TestAction() {
            super("testID", "testName", "A test Action");
            // TODO Auto-generated constructor stub
        }

        @Override
        public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
