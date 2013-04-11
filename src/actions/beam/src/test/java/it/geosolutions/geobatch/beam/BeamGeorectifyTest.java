package it.geosolutions.geobatch.beam;

import java.io.File;
import java.io.IOException;


public class BeamGeorectifyTest {

    public static void main(String[] args) throws IOException {
        BeamGeorectifierConfiguration configuration = new BeamGeorectifierConfiguration();
        configuration.setOutputFormat("NETCDF");
//        configuration.setFilterVariables("integrated_n2o,integrated_co,integrated_ch4,integrated_co2");
//        configuration.setAcceptedVariables("proc_flag1");
        
//        configuration.setFilterVariables("gs_1c_spect");
//        configuration.setFilterInclude(false);
        
//     final File netcdfFile = new File("c:/data/dlr/W_XX-EUMETSAT-Darmstadt,VIS+IR+IMAGERY,MSG2+SEVIRI_C_EUMG_20120830211510.nc");
//     final File netcdfFile = new
//     File("c:/data/dlr/W_XX-EUMETSAT-Darmstadt,HYPERSPECT+SOUNDING,METOPA+IASI_C_EUMP_20121120113254_31593_eps_o_l1c.nc");
//     final File netcdfFile = new
//     File("c:/data/dlr/converters/W_XX-EUMETSAT-Darmstadt,IASI,METOPA+IASI_C_EUMP_20121120062959_31590_eps_o_l2.nc");
     final File netcdfFile = new
             File("c:/data/dlr/W_XX-EUMETSAT-Darmstadt,SURFACE+SATELLITE,METOPA+ASCAT_C_EUMP_20110620020000_24214_eps_o_125_l1.nc");
//     final File netcdfFile = new
//             File("C:/data/dlr/eumetsat/W_XX-EUMETSAT-Darmstadt,SURFACE+SATELLITE,METOPA+ASCAT_C_EUMP_20110620020000_24214_eps_o_125_ssm_l2.nc");
//    final File netcdfFile = new File(
//            "c:/data/dlr/converters/W_XX-EUMETSAT-Darmstadt,SURFACE+SATELLITE,METOPA+ASCAT_C_EUMP_20110620020002_24214_eps_o_250_l1.nc");

         configuration.setGeophysics(false);
        BeamGeorectifier georectifier = new BeamGeorectifier(configuration);
        georectifier.georectify(netcdfFile);
    }
}
