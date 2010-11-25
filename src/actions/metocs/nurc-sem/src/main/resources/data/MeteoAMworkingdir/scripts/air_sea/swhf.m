function [qsw,alb]=swhf(yd,yr,long,lat,dsw)
% SWHF: computes net shortwave heat flux into the ocean and albedo.
% [qsw,alb]=SWHF(yd,yr,long,lat,dsw) computes the net shortwave heat 
% flux into the ocean and albedo, using Payne (1972), J. Atm. Sci., 29,
% 959-970, to estimate the instantaneous albedo given the atmospheric 
% transmittance (the ratio of measured insolation to the no-atmosphere 
% insolation).
%
% INPUT:  yd   - decimal yearday (e.g., 0000Z Jan 1 is 0.0)
%         yr   - year (e.g., 1995)
%         long - longitude  [deg]
%         lat  - latitude  [deg]
%         dsw  - (measured) insolation  [W/m^2]
%
% OUTPUT: qsw - net shortwave heat flux [W/m^2]
%         alb - albedo 

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 7/29/99: version 1.1
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% compute sun altitude and no atm solar radiation 
[sunalt, sorad]=soradna1(yd,yr,long,lat);

% compute atm transmittance  (note: trans=Inf when sorad=0)
trans=inf.*ones(size(sorad));
j=find(sorad>0);
trans(j)=dsw(j)./sorad(j);

% compute albedo  (note: alb=NaN when trans>1 or sunalt<0)
alb=albedo(trans, sunalt);

% compute net shortwave heat flux
%      (note: qsw set equal to 0 when alb=NaN)
qsw=(1-alb).*dsw;
ind=find(isnan(qsw));
qsw(ind)=zeros(size(ind));



