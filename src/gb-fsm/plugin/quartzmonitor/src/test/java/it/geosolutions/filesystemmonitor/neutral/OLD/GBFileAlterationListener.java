package it.geosolutions.filesystemmonitor.neutral.OLD;
/**
 * 
 */

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorNotifications;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;

/**
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class GBFileAlterationListener implements FileAlterationListener {
    
    private FileAlterationObserver observate=null;
    private Tree root=null;
    
    public GBFileAlterationListener(Tree t, FileAlterationObserver o){
        if (o!=null && t!=null){
            observate=o;
            root=t;
        }
        else
            throw new NullPointerException("Unable to add listener to an empty observer");
//            LOG THE NULL EVENT
    }

    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onDirectoryChange(java.io.File)
     */
    public void onDirectoryChange(File file) {
        System.out.println("onDirectoryChange for file: "+file);
        root.addEvent(new FileSystemMonitorEvent(file,
                FileSystemMonitorNotifications.DIR_MODIFIED));

    }

    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onDirectoryCreate(java.io.File)
     */
    public void onDirectoryCreate(File file) {
        System.out.println("onDirectoryCreate for file: "+file);
        root.addEvent(new FileSystemMonitorEvent(file,
                FileSystemMonitorNotifications.DIR_CREATED));
    }

    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onDirectoryDelete(java.io.File)
     */
    public void onDirectoryDelete(File file) {
        System.out.println("onDirectoryDelete for file: "+file);
        root.addEvent(new FileSystemMonitorEvent(file,
                FileSystemMonitorNotifications.DIR_REMOVED));
        try {
            root.delete(observate);
        } catch (Exception e) {
//TODO LOG
//            LOGGER.log(Level.FINER, e.getMessage(), e);
e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onFileChange(java.io.File)
     */
    public void onFileChange(File file) {
        System.out.println("onFileChange for file: "+file);
        root.addEvent(new FileSystemMonitorEvent(file,
                FileSystemMonitorNotifications.FILE_MODIFIED));
    }

    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onFileCreate(java.io.File)
     */
    public void onFileCreate(File file) {
        System.out.println("onFileCreate for file: "+file);
        root.addEvent(new FileSystemMonitorEvent(file,
                FileSystemMonitorNotifications.FILE_ADDED));
    }

    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onFileDelete(java.io.File)
     */
    public void onFileDelete(File file) {
        System.out.println("onFileDelete for file: "+file);
        root.addEvent(new FileSystemMonitorEvent(file,
                FileSystemMonitorNotifications.FILE_REMOVED));
        try {
            root.delete(observate);
        } catch (Exception e) {
//TODO LOG
//            LOGGER.log(Level.FINER, e.getMessage(), e);
e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onStart(org.apache.commons.io.monitor.FileAlterationObserver)
     */
    public void onStart(FileAlterationObserver file) {
        System.out.println("onStart for file:"+file.getDirectory());
    }

    /* (non-Javadoc)
     * @see org.apache.commons.io.monitor.FileAlterationListener#onStop(org.apache.commons.io.monitor.FileAlterationObserver)
     */
    public void onStop(FileAlterationObserver file) {
        System.out.println("onStop for file:"+file.getDirectory());
    }

}