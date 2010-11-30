% T_HFBULKTC: a program to test hfbulktc.m using COARE test data
% T_HFBULKTC is a program to test hfbulktc.m using COARE test data
% (file test2_5b.dat).  NOTE: Make sure that you use the COARE 
% parameters located in the files as_consts.m and cool_skin.m when 
% conducting the test.
%
% VARIBLES:
%
%   ur     = wind speed [m/s] measured at height zr [m] 
%   Ta     = air temperature [C] measured at height zt [m]
%   rh     = relative humidity [%] measured at height zq [m]
%   Pa     = air pressure [mb]
%   Ts     = sea surface temperature [C]
%   sal    = salinity [psu (PSS-78)]
%   dlw    = downwelling (INTO water) longwave radiation [W/m^2]
%   dsw    = measured insolation [W/m^2]
%   nsw    = net shortwave radiation INTO the water [W/m^2]
%   rain   = rain rate  [mm/hr]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 4/7/99: version 1.2 (contributed by AA)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% change directory to yours
load c:\flux_fsu\test\test2_5b.dat

% parse data
ur  = test2_5b(:,2);
zr  = 15;
Ta  = test2_5b(:,4);
zt  = 15;
Pa  = 1008*ones(size(ur));
q   = test2_5b(:,5);
rh  = q./qsat(Ta,Pa)/10;
zq  = 15;
Ts  = test2_5b(:,14);
sal = 30*ones(size(ur));
dsw = test2_5b(:,9);
nsw = 0.945*dsw;
dlw = test2_5b(:,10);
rain= test2_5b(:,11);

i=[1:length(ur)];
% i=[1:10];
% NO  cool-skin; compare to COARE output in file no_skin.out
A1 = hfbulktc(ur(i),zr,Ta(i),zt,rh(i),zq,Pa(i),Ts(i));

% YES cool-skin; compare to COARE output file yes_skin.out
A2 = hfbulktc(ur(i),zr,Ta(i),zt,rh(i),zq,Pa(i),Ts(i), ...
              sal(i),dlw(i),dsw(i),nsw(i));

