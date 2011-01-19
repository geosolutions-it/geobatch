package it.geosolutions.geobatch.action.scripting;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class Collector extends DirectoryWalker<File>{
    private FileFilter filter=null;
    
    public Collector(FileFilter filter){
        super(null,-1);
        this.filter=filter;
    }
    
    public List<File> collect(File root) throws IOException{
        List<File> res=new ArrayList<File>();
        super.walk(root, res);
        return res;
    }
    
    @Override
    protected boolean handleDirectory(File directory, int depth, Collection<File> results) throws IOException {
        if (this.filter!=null){
            if (this.filter.accept(directory)){
                results.add(directory);
            }
        }
        return true;  // process directory
    }
    
    @Override
    protected File[] filterDirectoryContents(File directory, int depth, File[] files)
            throws IOException {
//        return directory.listFiles(this.filter);
        if (this.filter!=null)
            return directory.listFiles((FilenameFilter)FileFilterUtils.or(FileFilterUtils.directoryFileFilter(),FileFilterUtils.asFileFilter(this.filter)));
        else
            return directory.listFiles();
    }

    @Override
    protected void handleFile(File file, int depth, Collection<File> results) throws IOException {
        if (this.filter!=null)
            if (this.filter.accept(file));
                results.add(file);
    }

    //test
    public static void main(String []arg0){
        Collector c=new Collector(//new WildcardFileFilter("*",IOCase.INSENSITIVE));
                FileFilterUtils.or(
                        new WildcardFileFilter("*_PCK.xml",IOCase.INSENSITIVE),
                        new WildcardFileFilter("*_PRO",IOCase.INSENSITIVE)));
        try {
            List<File> list=c.collect(new File("/home/carlo/work/data/EMSAWorkingDir/outa/"));
            System.out.println("Number of files: "+list.size());
            for (File f : list)
                System.out.println("FILE: "+f.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("ERROR: "+e.getLocalizedMessage());
        }
    }

}
