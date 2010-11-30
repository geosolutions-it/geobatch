function sradav = clskswr(yd,lat)
% CLSKSWR: computes clear sky insolation following Seckel&Beaudry (1973).
% sradav = CLSKSWR(yd,lat) computes average clear sky solar insolation
% based on the Seckel and Beaudry (1973) formula presented in Reed (1977),
% J. Phys. Ocean., 7, 482-485. Assumes the year is not a leap year.
%
% INPUT:   yd  -  yearday (e.g., Jan 10th is 10) 
%          lat -  latitude [deg]
%  
% OUTPUT:  sradav - clear sky mean daily insolation [W/m^2]

% NOTE: The output appears to be very similar to what you would get by
%       averaging soradna.m output over a day and then assuming an 
%       atmospheric transmission of 0.7 (with differences of order 10% 
%       for latitudes below 40N, and increasing to 30% in winter at 60N).
%       In absolute terms the agreement is to within +/-25 W/m^2.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/28/98: version 1.1 (vectorized by RP)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

radlat = pi*lat/180;

if length(lat)==1,
 lat=lat+zeros(size(yd));
end;

% check if yd is negative
ind=find(yd<0);
yd(ind)=yd(ind)+365;
% truncate to integer yearday for use with formula
yd=fix(yd);

phi = (yd-21)*360/365;
phi = pi*phi/180;

sradav=zeros(size(yd))+zeros(size(lat))+NaN;

ii= lat>=-20 & lat<40;
if any(ii(:)),
  a0 = -15.82 + 326.87*cos(radlat(ii));
  a1 = 9.63 + 192.44*cos(radlat(ii)+pi/2);
  b1 = -3.27 + 108.70*sin(radlat(ii));
  a2 = -0.64 + 7.80*sin(2*(radlat(ii)-pi/4));
  b2 = -0.50 + 14.42*cos(2*(radlat(ii)-5*pi/180));
  sradav(ii) = a0 + a1.*cos(phi(ii)) + b1.*sin(phi(ii)) + a2.*cos(2*phi(ii)) + b2.*sin(2*phi(ii));
end

ii= lat>=40 & lat<=60;;
if any(ii(:)),
  l2=lat(ii).^2;
  a0 = 342.61 - 1.97*lat(ii) - 0.018*l2;
  a1 = 52.08 - 5.86*lat(ii) + 0.043*l2;
  b1 = -4.80 + 2.46*lat(ii) -0.017*l2;
  a2 = 1.08 - 0.47*lat(ii) + 0.011*l2;
  b2 = -38.79 + 2.43*lat(ii) - 0.034*l2;
  sradav(ii) = a0 + a1.*cos(phi(ii)) + b1.*sin(phi(ii)) + a2.*cos(2*phi(ii)) + b2.*sin(2*phi(ii));
end

if any(lat>60 | lat<-20)
  warning('Formula only works for latitudes 20S-60N, see help text for further help')
end


