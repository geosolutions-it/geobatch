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
package it.geosolutions.geobatch.tools.file;

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
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * A collector which wraps the DirectoryWalker to define a Collector
 * which is able to collect (recursively) a set of file starting from
 * a base dir applying a FileFilter 
 * 
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class Collector extends DirectoryWalker<File>{
    private FileFilter filter=null;
    
    public Collector(FileFilter filter){
        super(null,-1);
        this.filter=filter;
    }
    
    /**
     * @param filter the filter to apply, null means visit all files
     * @param deep controls how deep the hierarchy is navigated to 
     * (less than 0 means unlimited)
     */
    public Collector(FileFilter filter, int deep){
        super(null,deep);
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

    /**
     * TODO test 
     * @deprecated a unit test should be implemented
     */
    public static void main(String []arg0){
        Collector c=new Collector(//new WildcardFileFilter("*",IOCase.INSENSITIVE));
                FileFilterUtils.or(
                        new WildcardFileFilter("*_PCK.xml",IOCase.INSENSITIVE),
                        new WildcardFileFilter("*_PRO",IOCase.INSENSITIVE)));
        try {
            List<File> list=c.collect(new File("/home/carlo/work/data/EMSAWorkingDir/out/20110118T084207016UTC"));
            System.out.println("Number of files: "+list.size());
            for (File f : list)
                System.out.println("FILE: "+f.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("ERROR: "+e.getLocalizedMessage());
        }
    }

}
