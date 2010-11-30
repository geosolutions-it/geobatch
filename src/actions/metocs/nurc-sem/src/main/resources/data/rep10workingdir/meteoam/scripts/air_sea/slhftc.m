function  [qsen,qlat,tau,theta,A]=slhftc(ua,va,uo,vo,zr,Ta,zt,rh,zq,Pa,Ts)
% SLHFTC: computes sensible and latent heat flux following TOGA/COARE.
% [qsen,qlat,tau,theta,A]=SLHFTC(ua,va,uo,vo,zr,Ta,zt,rh,zq,ap,Ts) computes 
% the sensible and latent heat fluxes into the ocean and the surface wind
% stress based on the Fairall et al (1996) COARE code. (see HFBULKTC for 
% description).  Assumes input series are either column or row vectors;
% zr, zt, and zq are fixed scalars, and rh and/or Pa may be scalars. 
% The output variables are column vectors and the column matrix A.  NOTE:
% user must decide if cool-skin and Webb corrections are to be included.
%
%  INPUT:   ua,va  -  east, north wind components [m/s]
%           uo,vo  -  east, north ocean surface currents [m/s]
%           zr  -  wind measurement height [m]
%           Ta  -  air temperature  [C]
%           zt  -  air temperature measurement height [m]
%           rh  -  relative humidity   [%]
%           zq  -  rh measurement height [m]
%           Ts  -  sea surface temperature  [C]
%           Pa  - air pressure  [mb] 
%
%  OUTPUT:  qsen  -  sensible heat flux  [W/m^2]
%           qlat  -  latent heat flux   [W/m^2]
%           tau  -  wind stress magnitude  [Pa]
%           theta  -  direction of wind stress [deg CCW from east]
%           A - 12 column matrix of auxilary diagnostic outputs
%                (see HFBULKTC for details)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 8/28/98: version 1.1 (contributed by RP)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% compute relative velocity magnitude and direction
du=ua-uo;
dv=va-vo;
spd=sqrt(du.^2+dv.^2);
theta=(180./pi).*atan2(dv,du);

% change column vector to row vector if necessary
[m n] = size(spd);
if m > n
  spd = spd';
end
% keep theta a column vector
[m n] = size(theta);
if n > m
  theta = theta';
end

% compute fluxes
A = hfbulktc(spd,zr,Ta,zt,rh,zq,Pa,Ts); qsen=A(:,1); % no cool_skin, Webb correction
qlat=A(:,2);
tau=A(:,4);


