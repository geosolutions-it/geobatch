  function q=qsat(Ta,Pa)
% QSAT: computes specific humidity at saturation. 
% q=QSAT(Ta) computes the specific humidity (kg/kg) at satuation at
% air temperature Ta (deg C). Dependence on air pressure, Pa, is small,
% but is included as an optional input.
%
%    INPUT:   Ta - air temperature  [C]
%             Pa - (optional) pressure [mb]
%
%    OUTPUT:  q  - saturation specific humidity  [kg/kg]

% Version 1.0 used Tetens' formula for saturation vapor pressure 
% from Buck (1981), J. App. Meteor., 1527-1532.  This version 
% follows the saturation specific humidity computation in the COARE
% Fortran code v2.5b.  This results in an increase of ~5% in 
% latent heat flux compared to the calculation with version 1.0.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 4/7/99: version 1.2 (revised as above by AA)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

if nargin==1,
  as_consts;
  Pa=P_default; % pressure in mb
end;

% original code
% a=(1.004.*6.112*0.6220)./Pa;
% q=a.*exp((17.502.*Ta)./(240.97+Ta))

% as in Fortran code v2.5b for COARE
ew = 6.1121*(1.0007+3.46e-6*Pa).*exp((17.502*Ta)./(240.97+Ta)); % in mb
q  = 0.62197*(ew./(Pa-0.378*ew));                         % mb -> kg/kg
