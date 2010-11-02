/**
 * 
 */
package it.geosolutions.geobatch.action.egeos.emsa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;

/**
 * @author Administrator
 * 
 */
public class EMSAIOUtils {

    protected final static Logger LOGGER = Logger.getLogger(EMSAIOUtils.class.toString());

    private EMSAIOUtils() {

    }

    /**
     * 
     * @param destBaseDir
     * @return
     * @throws IOException
     */
    public static File unZip(final File inputFile, final File destBaseDir) throws IOException {
        final File tmpDestDir = createTodayDirectory(destBaseDir, FilenameUtils
                .getBaseName(inputFile.getName()));

        ZipFile zipFile = new ZipFile(inputFile);

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            InputStream stream = zipFile.getInputStream(entry);

            if (entry.isDirectory()) {
                // Assume directories are stored parents first then
                // children.
                (new File(tmpDestDir, entry.getName())).mkdir();
                continue;
            }

            File newFile = new File(tmpDestDir, entry.getName());
            FileOutputStream fos = new FileOutputStream(newFile);
            try {
                byte[] buf = new byte[1024];
                int len;

                while ((len = stream.read(buf)) >= 0)
                    saveCompressedStream(buf, fos, len);

            } catch (IOException e) {
                zipFile.close();
                IOException ioe = new IOException("Not valid COAMPS archive file type.");
                ioe.initCause(e);
                throw ioe;
            } finally {
                fos.flush();
                fos.close();

                stream.close();
            }
        }
        zipFile.close();

        return tmpDestDir;
    }

    /**
     * 
     * @param destBaseDir
     * @return
     * @throws IOException
     */
    public static File unTar(final File inputFile, final File destBaseDir) throws IOException {
        final File tmpDestDir = createTodayDirectory(destBaseDir, FilenameUtils
                .getBaseName(inputFile.getName()));

        final TarInputStream stream = new TarInputStream(new FileInputStream(inputFile));

        if (stream == null) {
            throw new IOException("Not valid archive file type.");
        }

        TarEntry entry;
        while ((entry = stream.getNextEntry()) != null) {
            final String entryName = entry.getName();

            if (entry.isDirectory()) {
                // Assume directories are stored parents first then
                // children.
                (new File(tmpDestDir, entry.getName())).mkdir();
                continue;
            }

            byte[] buf = new byte[(int) entry.getSize()];
            stream.read(buf);

            File newFile = new File(tmpDestDir.getAbsolutePath(), entryName);
            FileOutputStream fos = new FileOutputStream(newFile);
            try {
                saveCompressedStream(buf, fos, buf.length);
            } catch (IOException e) {
                stream.close();
                IOException ioe = new IOException("Not valid archive file type.");
                ioe.initCause(e);
                throw ioe;
            } finally {
                fos.flush();
                fos.close();
            }
        }
        stream.close();

        return tmpDestDir;
    }

    /**
     * 
     * @param destDir
     * @return
     * @throws IOException
     */
    public static File unTarGz(final File inputFile, final File destDir) throws IOException {
        File unzippedDir = unZip(inputFile, destDir);

        if (unzippedDir != null && unzippedDir.exists() && unzippedDir.isDirectory()) {

            File[] tarFiles = unzippedDir.listFiles(new FilenameFilter() {

                public boolean accept(File dir, String name) {
                    if (FilenameUtils.getExtension(name).equalsIgnoreCase("tar"))
                        return true;
                    return false;
                }
            });

            if (tarFiles != null && tarFiles.length > 0) {
                if (tarFiles.length == 1) {
                    return unTar(tarFiles[0], destDir);
                } else {
                    for (File tarFile : tarFiles)
                        unTar(tarFile, destDir);
                }

                return destDir;
            }
        }

        throw new IOException(
                "::decompressTarGz : could not find any valid tar file to decompress.");
    }

    /**
     * Calling tar -xzf from command line
     * 
     * @return
     */
    public static File unTarGzTaskExec(final File inputFile, final File destDir) throws IOException {
        final File tmpDestDir = createTodayDirectory(destDir, FilenameUtils.getBaseName(inputFile
                .getName()));

        final Project project = new Project();
        project.init();

        final ExecTask execTask = new ExecTask();
        execTask.setProject(project);

        // Setting execution directory
        execTask.setDir(tmpDestDir);

        // Setting executable
        execTask.setExecutable("tar");

        // Setting Error logging
        execTask.setLogError(true);
        // execTask.setError(error);
        execTask.setFailonerror(true);

        // Setting the timeout
        execTask.setTimeout(1200000);

        // Setting command line argument
        final String argument = " -xzf " + inputFile.getAbsolutePath();
        execTask.createArg().setLine(argument);

        // Executing
        execTask.execute();

        return tmpDestDir;
    }

    /**
     * @param len
     * @param stream
     * @param fos
     * @return
     * @throws IOException
     */
    public static void saveCompressedStream(final byte[] buffer, final OutputStream out,
            final int len) throws IOException {
        try {
            out.write(buffer, 0, len);

        } catch (Exception e) {
            out.flush();
            out.close();
            IOException ioe = new IOException("Not valid archive file type.");
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Create a subDirectory having the actual date as name, within a specified destination
     * directory.
     * 
     * @param destDir
     *            the destination directory where to build the "today" directory.
     * @param inputFileName
     * @return the created directory.
     */
    public final static File createTodayDirectory(File destDir, String inputFileName) {
        return createTodayDirectory(destDir, inputFileName, false);
    }

    /**
     * Create a subDirectory having the actual date as name, within a specified destination
     * directory.
     * 
     * @param destDir
     *            the destination directory where to build the "today" directory.
     * @param inputFileName
     * @return the created directory.
     */
    public final static File createTodayDirectory(File destDir, String inputFileName,
            final boolean withTime) {
        final SimpleDateFormat SDF = withTime ? new SimpleDateFormat("yyyy_MM_dd_hhmmsss")
                : new SimpleDateFormat("yyyy_MM_dd");
        final String newPath = (new StringBuffer(destDir.getAbsolutePath().trim()).append(
                File.separatorChar).append(SDF.format(new Date())).append("_")
                .append(inputFileName)).toString();
        File dir = new File(newPath);
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }
}
