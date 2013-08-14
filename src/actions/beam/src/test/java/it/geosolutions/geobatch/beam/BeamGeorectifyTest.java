package it.geosolutions.geobatch.beam;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.esa.beam.util.UtilConstants;
import org.junit.Test;

public class BeamGeorectifyTest {

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
        final File outputFolder = new File("C:\\data\\outAscatL\\");
        configuration.setOutputFolder(outputFolder.getAbsolutePath());
        configuration.setOutputFormat("NETCDF");
        
        final String sourceFolder = "c:/data/dlr/eumetsat"; //eumetsat/"; 

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
//                new ConfigSamplePair(
//                        sourceFolder,"W_XX-EUMETSAT-Darmstadt,IASI,METOPA+IASI_C_EUMP_20121120062959_31590_eps_o_l2.nc", // METOPA IASI L2
//                        configuration.clone()),
                new ConfigSamplePair(
//                        sourceFolder,"W_XX-EUMETSAT-Darmstadt,SURFACE+SATELLITE,METOPA+ASCAT_C_EUMP_20120412144801_28441_eps_o_125_l1.nc", // METOPA ASCAT L1
                        sourceFolder,"W_XX-EUMETSAT-Darmstadt,SURFACE+SATELLITE,METOPA+ASCAT_C_EUMP_20110620020000_24214_eps_o_125_l1.nc", // METOPA ASCAT L1
                        configuration.clone()),
//                new ConfigSamplePair(
//                        sourceFolder,"W_XX-EUMETSAT-Darmstadt,SURFACE+SATELLITE,METOPA+ASCAT_C_EUMP_20110620020000_24214_eps_o_125_ssm_l2.nc", // METOPA
//                                                                                                                                              // ASCAT L2
//                        configuration.clone()),

        };

        final String prefix = "SS" + UtilConstants.SUBSAMPLING + "_SR" + UtilConstants.SEARCH_RADIUS 
                + "_TS" + UtilConstants.TILE_SIZE + "_PPSD" + UtilConstants.POINTS_PER_SIDE_DIVIDER + "_";
        for (ConfigSamplePair singlecase : cases) {
            if (singlecase.file.endsWith("C_EUMP_20121120062959_31590_eps_o_l2.nc")) {
                singlecase.config.setDimensions("nlt, nlq, new, nlo, surf_temp, cloud_formations");
            } else if (singlecase.file.contains("ASCAT_C_EUMP") && singlecase.file.contains("l1.nc")) {
                singlecase.config.setDimensions("numSigma");
                // Forcing coordinates creation for numSigma dimension to use stacking dimensions
                singlecase.config.setForceCoordinates(true);
            }
            BeamGeorectifier georectifier = new BeamGeorectifier(singlecase.config);

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
