/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
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
package it.geosolutions.filesystemmonitor.neutral.monitorpolling;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;
import it.geosolutions.filesystemmonitor.monitor.impl.GBEventConsumer;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

/**
 * Listener for FS Modifications
 * 
 * @see GBFileSystemMonitorJob
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class GBFileAlterationListener implements FileAlterationListener{

    GBEventConsumer consumer=null;
    
    // TODO CAMEL
//    @EndpointInject(uri="direct:foo")
//    ProducerTemplate producer;

    public GBFileAlterationListener(GBEventConsumer ec) throws Exception {
        if (ec!=null && !ec.isStopped())
            consumer=ec;
        else {
//TODO log
            throw new Exception("The passed event consumer is null or stopped!");
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onDirectoryChange(java.io.File)
     */
    public void onDirectoryChange(File file) {
//        CAMEL
//        consumer.add(new FileSystemMonitorEvent(file, notification))
//        System.out.println("onDirectoryChange for file: "+file);
        consumer.add(new FileSystemMonitorEvent(file, FileSystemMonitorNotifications.DIR_MODIFIED));
    }

    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onDirectoryCreate(java.io.File)
     */
    public void onDirectoryCreate(File file) {
//        System.out.println("onDirectoryCreate for file: "+arg0);
        consumer.add(new FileSystemMonitorEvent(file, FileSystemMonitorNotifications.DIR_CREATED));
    }

    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onDirectoryDelete(java.io.File)
     */
    public void onDirectoryDelete(File file) {
//        System.out.println("onDirectoryDelete for file: "+arg0);
        consumer.add(new FileSystemMonitorEvent(file, FileSystemMonitorNotifications.DIR_REMOVED));
    }

    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onFileChange(java.io.File)
     */
    public void onFileChange(File file) {
        consumer.add(new FileSystemMonitorEvent(file, FileSystemMonitorNotifications.FILE_MODIFIED));
//        System.out.println("onFileChange for file: "+file);
//        try{
//            producer.sendBody(arg0);
//        }
//        catch (Throwable t){
//t.printStackTrace();
//        }
    }

    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onFileCreate(java.io.File)
     */
    public void onFileCreate(File file) {
//        System.out.println("onFileCreate for file: "+arg0);
        consumer.add(new FileSystemMonitorEvent(file, FileSystemMonitorNotifications.FILE_ADDED));
    }

    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onFileDelete(java.io.File)
     */
    public void onFileDelete(File file) {
//        System.out.println("onFileDelete for file: "+arg0);
        consumer.add(new FileSystemMonitorEvent(file, FileSystemMonitorNotifications.FILE_REMOVED));
    }
    
    public void onStart(FileAlterationObserver fao) {
        //TODO need logging info on this event? fao.getDirectory()
//        System.out.println("onStart");
    }

    public void onStop(FileAlterationObserver fao) {
        //TODO need logging info on this event? fao.getDirectory()
//        System.out.println("onStop");
    }
    
    // CAMEL
//    public void process(Exchange exchange) throws Exception {
//        // TODO Auto-generated method stub
//        
//    }

}
