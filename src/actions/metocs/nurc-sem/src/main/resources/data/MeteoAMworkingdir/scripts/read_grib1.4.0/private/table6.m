%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% TABLE 6, GDS (Section 2) Octet 6                 %
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function Dat_Rep_Type=table6(ival)
switch ival
   case 0,Dat_Rep_Type='Equidis. Cyl. Lat/Lon';
   case 1,Dat_Rep_Type='Mercator';
   case 2,Dat_Rep_Type='Gnomic';
   case 3,Dat_Rep_Type='Lambert Conf.';
   case 4,Dat_Rep_Type='Gaussian Lat/Lon';
   case 5,Dat_Rep_Type='Polar Stereogrphic';
   case 13,Dat_Rep_Type='Oblique Lambert Conf.';
   case 50,Dat_Rep_Type='Spher. Harm. Coeff';
   case 90,Dat_Rep_Type='Space View';
   otherwise, Dat_Rep_Type='Reserved';
end
