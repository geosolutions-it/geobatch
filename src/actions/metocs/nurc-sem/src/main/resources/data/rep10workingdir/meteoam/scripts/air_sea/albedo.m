function alb=albedo(trans,sunalt)
% ALBEDO: computes sea surface albedo following Payne (1972).
% alb=ALBEDO(trans,sunalt) computes the sea surface albedo from the
% atmospheric transmittance and sun altitude by linear interpolation 
% using Table 1 in Payne (1972), J. Atm. Sci., 29, 959-970. Assumes 
% trans and sunalt both matrices of same size. Table 1 is called 
% albedot1.mat.
%
% INPUT:   trans - atmospheric transmittance 
%          sunalt - sun altitude [deg] 
% 
% OUTPUT:  alb - albedo 

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/10/96: version 1.0
% 7/24/98: version 1.1 (rev. to handle out-of-range input values by RP)
% 8/5/99:  version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% load table 1 
load albedot1

% create axes
x=[0:2:90];
y=[0:.05:1.0]';

alb=ones(size(trans))+NaN;
k=sunalt>0 & finite(trans) & trans<=1.01;

% interpolate
alb(k)=interp2(x,y,albedot1,sunalt(k),trans(k));


