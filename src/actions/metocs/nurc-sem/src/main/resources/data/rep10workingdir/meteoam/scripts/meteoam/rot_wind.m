%
% Function for wind rotation when reading LAMI gribs
%
% function [pus,pvs]=rot_wind(alm,aph,tpus,tpvs);
%
% pus  =  u-wind component on geographycal coordinate (not rotated)
% pvs  =  v-wind component on geographycal coordinate (not rotated)
% alm  =  geographycal longitude (not rotated)
% aph  =  geographycal latitude  (not rotated)
% tpus =  u-wind component in LAMI rotated system
% tpvs =  v-wind component in LAMI rotated system
%
function [pus,pvs]=rot_wind(alm,aph,tpus,tpvs);
%
% LAMI  north pole of rotation 
tlm0=10.;
tph0=57.5;
%
dtr=pi/180.;
%
stph0=sin(tph0*dtr);
ctph0=cos(tph0*dtr);
relm=(alm-tlm0);
%
srlm=sin(relm*dtr);
crlm=cos(relm*dtr);
%
sph=sin(aph*dtr);
cph=cos(aph*dtr);
%
cc=cph.*crlm;
tph=asin(ctph0.*sph-stph0.*cc);
%
rctph=1./cos(tph);
cray=stph0.*srlm.*rctph;
dray=(ctph0.*cph+stph0.*sph.*crlm).*rctph;
dc=dray.^2+cray.^2;
pus=(dray.*tpus+cray.*tpvs)./dc;
pvs=(dray.*tpvs-cray.*tpus)./dc;
return
