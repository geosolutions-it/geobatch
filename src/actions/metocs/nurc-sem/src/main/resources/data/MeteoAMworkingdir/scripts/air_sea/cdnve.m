function [cd,u10]=cdnve(sp,z)
% CDNVE: computes neutral drag coefficient following Vera (1983).
% [cd,u10]=CDNVE(sp,z) computes the neutral drag coefficient and wind 
% speed at 10m given the wind speed at height z.  Uses the expression 
% for friction velocity derived by E. Vera (1983) and published as 
% eqn. 8 in Large, Morzel, and Crawford (1995), J. Phys. Oceanog., 25,
% 2959-2971. Range of fit to data is 1 to 25 m/s.
%
% INPUT:   sp - wind speed  [m/s]
%          z - measurement height  [m]
%
% OUTPUT:  cd - neutral drag coefficient at 10m
%          u10 - wind speed at 10m  [m/s]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/26/98: version 1.1 (modified by RP)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% constants in fit for drag coefficient
A=2.717e-3;
B=0.142e-3;
C=0.0764e-3;

as_consts;           % other constants
a=log(z./10)/kappa;  % log-layer correction factor
tol=.001;            % tolerance for iteration (m/s)

u10o=zeros(size(sp))+.1;  % don't start iteration at 0 to prevent blowups.
cd=(A./u10o + B + C*u10o);

u10=sp./(1+a.*sqrt(cd));

ii=abs(u10-u10o)>tol;
while any(ii(:)),
  u10o=u10;
  cd=(A./u10o + B + C*u10o);
  u10=sp./(1+a.*sqrt(cd));   % next iteration
  ii=abs(u10-u10o)>tol;      % keep going until iteration converges
end;


