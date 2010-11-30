function [almd,aphd]=rtll(tlm0d,tph0d,tlmd,tphd);
%-------------------------------------------------------------------------
% RTLL transforms rotated coordinates (tlmd,tphd) into 
% ordinary geographic coordinates (almd,aphd). i/o decimal degrees (DD)
% tlm0d, tph0d: lon e lat of the center of rotation ("North Pole") in DD.
% 
dtr=pi/180.;

ctph0=cos(tph0d*dtr);
stph0=sin(tph0d*dtr);

stph=sin(tphd*dtr);
ctph=cos(tphd*dtr);
ctlm=cos(tlmd*dtr);
stlm=sin(tlmd*dtr);

aph=asin(stph0.*ctph.*ctlm+ctph0.*stph);
cph=cos(aph);

almd=tlm0d+asin(stlm.*ctph./cph)/dtr; 
aphd=aph/dtr;
