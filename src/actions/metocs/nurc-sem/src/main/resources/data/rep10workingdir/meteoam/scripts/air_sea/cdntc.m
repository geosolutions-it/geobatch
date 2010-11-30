function [cd,u10]=cdntc(sp,z,Ta)
% CTDTC: computes the neutral drag coefficient following Smith (1988).
% [cd,u10]=CDNTC(sp,z,Ta) computes the neutral drag coefficient and 
% wind speed at 10m given the wind speed and air temperature at height z 
% following Smith (1988), J. Geophys. Res., 93, 311-326. 
%
% INPUT:   sp - wind speed  [m/s]
%          z - measurement height [m]
%          Ta - air temperature (optional)  [C] 
%
% OUTPUT:  cd - neutral drag coefficient at 10m
%          u10 - wind speed at 10m  [m/s]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/26/98: version 1.1 (vectorized by RP)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% get constants
as_consts; 

if nargin==2,
  Ta=Ta_default;
end;

% iteration endpoint
tol=.00001;

visc=viscair(Ta);

% remove any sp==0 to prevent division by zero
i=find(sp==0);
sp(i)=.1.*ones(length(i),1);

% initial guess
ustaro=zeros(size(sp));
ustarn=.036.*sp;

% iterate to find z0 and ustar
ii=abs(ustarn-ustaro)>tol;
while any(ii(:)),
  ustaro=ustarn;
  z0=Charnock_alpha.*ustaro.^2./g + R_roughness*visc./ustaro;
  ustarn=sp.*(kappa./log(z./z0));
  ii=abs(ustarn-ustaro)>tol;
end

sqrcd=kappa./log((10)./z0);
cd=sqrcd.^2;

u10=ustarn./sqrcd;
