package it.geosolutions.filesystemmonitor.neutral.OLD;

import it.geosolutions.filesystemmonitor.monitor.FileSystemMonitorEvent;
import it.geosolutions.filesystemmonitor.monitor.impl.GBEventConsumer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.monitor.FileAlterationObserver;

public class Tree extends DirectoryWalker<FileAlterationObserver> {
    
    private final List<FileAlterationObserver> observers = new CopyOnWriteArrayList<FileAlterationObserver>();
    
    private GBEventConsumer consumer=null;
    
    private File root=null;

    public Tree(final String wildcard, final File base, final GBEventConsumer consumer) {
        // here we could not be sure that wildcard is !null so
        //we use the 2nd filter overriding filterDirectory
        super(new WildcardFileFilter(wildcard),-1);
        if (base!=null)
            if (base.exists())
                root=base;
        
System.out.println("TEST");
//      else
//      //TODO LOG NO FILTER

//      else
//      //TODO LOG NO FILTER
        if (consumer!=null)
            this.consumer=consumer;
//      else
//      //TODO LOG NO FILTER
        
    }
    
    public void addEvent(FileSystemMonitorEvent ev){
        consumer.add(ev);
    }
    
    @Override
    protected void handleFile(File file, int depth, Collection<FileAlterationObserver> results) throws IOException {

        FileAlterationObserver fao=new FileAlterationObserver(file);
        fao.addListener(new GBFileAlterationListener(this,fao));
        // TODO better check
        if (!results.contains(fao)){
            try {
                fao.initialize();
            } catch (Exception e) {
//                LOGGER.log(Level.FINER, e.getMessage(), e);
e.printStackTrace();
            }
            results.add(fao);
        } else {
            try {
                fao.destroy();
            } catch (Exception e) {
//                LOGGER.log(Level.FINER, e.getMessage(), e);
e.printStackTrace();
            }
        }
    }
    
//    @Override
//    protected File[] filterDirectoryContents(File directory, int depth, File[] files) throws IOException {
//        return directory.listFiles(filter);
//    }
    
    @Override
    protected boolean handleDirectory(File directory, int depth,Collection<FileAlterationObserver> results) throws IOException {
        // TODO Auto-generated method stub
        handleFile(directory, depth, results);
        return true;
    }

    /**
     * used by listeners to handle file deletion and collection
     * updating
     * @param f
     * @throws Exception
     */
    protected void delete(FileAlterationObserver f) throws Exception{
        if (observers.remove(f))
            f.destroy();
    }
    
    public void update() throws IOException{
        walk(root, observers);
    }
    
    public void check(){
        for (FileAlterationObserver observer : observers) {
            observer.checkAndNotify();
        }
    }
    
    public void clear(){
        int size=observers.size();
        for (int i=0; i<size; i++){
            FileAlterationObserver o=null;
            try {
                o=observers.remove(i);
                if (o!=null)
                    o.destroy();
            } catch (Exception e) {
// TODO                LOGGER.log(Level.FINER, e.getMessage(), e);
e.printStackTrace();
            }
        }
    }
    

//    /**
//     * Add a file system observer to this monitor.
//     *
//     * @param observer The file system observer to add
//     */
//    public void addObserver(final FileAlterationObserver observer) {
//        if (observer != null) {
//            observers.add(observer);
//        }
//    }
//
//    /**
//     * Remove a file system observer from this monitor.
//     *
//     * @param observer The file system observer to remove
//     */
//    public void removeObserver(final FileAlterationObserver observer) {
//        if (observer != null) {
//            while (observers.remove(observer)) {
//            }
//        }
//    }
//
//    /**
//     * Returns the set of {@link FileAlterationObserver} registered with
//     * this monitor. 
//     *
//     * @return The set of {@link FileAlterationObserver}
//     */
//    public Iterable<FileAlterationObserver> getObservers() {
//        return observers;
//    }
}
