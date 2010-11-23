function [cd,u10]=cdnlp(sp,z)
% CDNLP: computes neutral drag coefficient following Large&Pond (1981).
% [cd,u10]=CDNLP(sp,z) computes the neutral drag coefficient and wind 
% speed at 10m given the wind speed at height z following Large and 
% Pond (1981), J. Phys. Oceanog., 11, 324-336. 
%
% INPUT:   sp - wind speed  [m/s]
%          z - measurement height [m]
%
% OUTPUT:  cd - neutral drag coefficient at 10m
%          u10 - wind speed at 10m  [m/s]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/26/98: version 1.1 (vectorized by RP)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

as_consts;           % define physical constants
a=log(z./10)/kappa;  % log-layer correction factor
tol=.001;            % tolerance for iteration [m/s]

u10o=zeros(size(sp));
cd=1.15e-3*ones(size(sp));
u10=sp./(1+a.*sqrt(cd));

ii=abs(u10-u10o)>tol;
while any(ii(:)),
  u10o=u10;
  cd=(4.9e-4+6.5e-5*u10o);    % compute cd(u10)
  cd(u10o<10.15385)=1.15e-3;
  u10=sp./(1+a.*sqrt(cd));   % next iteration
  ii=abs(u10-u10o)>tol;      % keep going until iteration converges
end;
