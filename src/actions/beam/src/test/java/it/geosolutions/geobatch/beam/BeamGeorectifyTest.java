package it.geosolutions.geobatch.beam;

import it.geosolutions.geobatch.beam.msgwarp.MSGConfiguration;
import it.geosolutions.geobatch.catalog.Identifiable;
import it.geosolutions.geobatch.flow.event.IProgressListener;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.esa.beam.util.UtilConstants;
import org.junit.Test;

public class BeamGeorectifyTest {
    
    static class MyProgressListener implements IProgressListener {

        String task;

        float progress;

        @Override
        public void completed() {
            System.out.println("Completed");
            
        }

        @Override
        public void failed(Throwable arg0) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public Identifiable getOwner() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public float getProgress() {
            return progress;
        }

        @Override
        public String getTask() {
            return task;
        }

        @Override
        public void paused() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void progressing() {
            System.out.println("Progressing: TASK: " + task + " progress: " + progress);
        }

        @Override
        public void resumed() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setProgress(float arg0) {
            progress = arg0;
//            System.out.println("Progress: " + arg0);
        }

        @Override
        public void setTask(String arg0) {
            task = arg0;
//            System.out.println("Setting Task: " + arg0);
        }

        @Override
        public void started() {
            System.out.println("Started");
        }

        @Override
        public void terminated() {
            System.out.println("Terminated");
            
        }
        
    }

    static class ConfigSamplePair {
        public ConfigSamplePair(String folder, String file, BeamGeorectifierConfiguration config) {
            this.folder = folder;
            this.file = file;
            this.config = config;
        }

        String file;

        String folder;

        BeamGeorectifierConfiguration config;
    }
    
    @Test
    public void emptyTest() {
        // Does nothing
    }

    public static void main(String[] args) throws IOException {
        BeamGeorectifierConfiguration configuration = new BeamGeorectifierConfiguration();
        configuration.setFilterInclude(false);
        configuration.setGeophysics(true);
        configuration.setJAICapacity(512*1024*1024);
        final File outputFolder = new File("C:\\work\\EUMETSAT\\test_data\\output");
        configuration.setOutputFolder(outputFolder.getAbsolutePath());
        configuration.setOutputFormat("NETCDF");
        
        final String sourceFolder = "C:\\work\\EUMETSAT\\test_data"; //eumetsat/";

        final ConfigSamplePair[] cases = new ConfigSamplePair[] {
//                new ConfigSamplePair(
//                        sourceFolder,"W_XX-EUMETSAT-Darmstadt,VIS+IR+IMAGERY,MSG2+SEVIRI_C_EUMG_20120421120011.nc", // Meteosat MSG2
//                        configuration.clone()),
//                new ConfigSamplePair(
//                        sourceFolder,"W_XX-EUMETSAT-Darmstadt,VIS+IR+IMAGERY,MET7+MVIRI_C_EUMS_20120904163000.nc", // Meteosat MSG2
//                        configuration.clone()),
//                new ConfigSamplePair(
//                       sourceFolder,"W_XX-EUMETSAT-Darmstadt,HYPERSPECT+SOUNDING,METOPA+IASI_C_EUMP_20121120113254_31593_eps_o_l1c.nc", // METOPA IASI
//                                                                                                                                        // L1C
//                        configuration.clone()),
                new ConfigSamplePair(
                        sourceFolder,"W_XX-EUMETSAT-Darmstadt,VIS+IR+IMAGERY,MSG3+SEVIRI_C_EUMG_20130421120010.nc", // Meteosat MSG3
                                                                                                                                         // L1C
                         configuration.clone()),
//                 new ConfigSamplePair(
//                         sourceFolder,"SS30_SR5_TS10.0_PPSD10_W_XX-EUMETSAT-Darmstadt,VIS+IR+IMAGERY,MSG3+SEVIRI_C_EUMG_20130421120010.nc", // Meteosat MSG3
//                                                                                                                                          // L1C
//                          configuration.clone()),
//                new ConfigSamplePair(
//                        sourceFolder,"W_XX-EUMETSAT-Darmstadt,IASI,METOPA+IASI_C_EUMP_20121120113254_31593_eps_o_l2.nc", // METOPA IASI L2
//                        configuration.clone()),
//                new ConfigSamplePair(
////                        sourceFolder,"W_XX-EUMETSAT-Darmstadt,SURFACE+SATELLITE,METOPA+ASCAT_C_EUMP_20120412144801_28441_eps_o_125_l1.nc", // METOPA ASCAT L1
//                        sourceFolder,"W_XX-EUMETSAT-Darmstadt,SURFACE+SATELLITE,METOPA+ASCAT_C_EUMP_20110620020000_24214_eps_o_125_l1.nc", // METOPA ASCAT L1
//                        configuration.clone()),
//                new ConfigSamplePair(
//                        sourceFolder,"W_XX-EUMETSAT-Darmstadt,SURFACE+SATELLITE,METOPA+ASCAT_C_EUMP_20110620020000_24214_eps_o_125_ssm_l2.nc", // METOPA
//                                                                                                                                              // ASCAT L2
//                        configuration.clone()),

        };

        final String prefix = "SS" + UtilConstants.SUBSAMPLING + "_SR" + UtilConstants.SEARCH_RADIUS 
                + "_TS" + UtilConstants.TILE_SIZE + "_PPSD" + UtilConstants.POINTS_PER_SIDE_DIVIDER + "_";
        for (ConfigSamplePair singlecase : cases) {
            if (singlecase.file.contains("IASI,METOPA")) {
                singlecase.config.setDimensions("nlt, nlq, new, nlo, surf_temp, cloud_formations");
            } else if (singlecase.file.contains("HYPERSPECT")) {
                    singlecase.config.setDimensions("spectral");
                    singlecase.config.setLargeFile(true);
                    String parameters = "width=360,height=180";
                    singlecase.config.setParams(parameters);
            } else if (singlecase.file.contains("ASCAT_C_EUMP") && singlecase.file.contains("l1.nc")) {
                singlecase.config.setDimensions("numSigma");
                // Forcing coordinates creation for numSigma dimension to use stacking dimensions
                singlecase.config.setForceCoordinates(true);
            }  else if (singlecase.file.contains("MSG3")) {
//                singlecase.config.setLargeFile(true);
                String parameters = "width=3712,height=3712";
                singlecase.config.setGeophysics(false);
                singlecase.config.setParams(parameters);
                singlecase.config.setUseBeam(false);
                // Creation of an MSG configuration Object
                MSGConfiguration config = new MSGConfiguration();
                config.setEarthCentreDistance(42164);
                config.setLrx(82);
                config.setLry(-82);
                config.setResol_angle_X(17.83);
                config.setResol_angle_Y(17.83);
                config.setUlx(-82);
                config.setUly(82);
                // Setting of the MSG configuration object
                singlecase.config.setMsgConf(config);
            }
            BeamGeorectifier georectifier = new BeamGeorectifier(singlecase.config);
            georectifier.addListener(new MyProgressListener());
            // configuration.setGeophysics(false);
            try {
                georectifier.georectify(new File(sourceFolder, singlecase.file), null);
            } catch (NullPointerException npe) {
                
            }
            FileUtils.moveFile(new File(outputFolder, singlecase.file), new File(outputFolder, prefix + singlecase.file));
            System.out.println("DONE");
        }
    }
}
