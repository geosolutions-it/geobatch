function  rhoa=air_dens(Ta,RH,Pa)
% AIR_DENS: computes computes the density of moist air.
% rhoa=AIR_DENS(Ta,RH,Pa) computes the density of moist air.
% Air pressure is optional.
%
% INPUT:   Ta  - air temperature Ta  [C]
%          RH  - relative humidity  [%]
%          Pa  - air pressure (optional) [mb] 
%
% OUTPUT:  rhoa - air density  [kg/m^3]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 4/7/99: version 1.2 (contributed by AA)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% load constants
as_consts;

if nargin == 2
  Pa = P_default; 
end

o61  = 1/eps_air-1;                % 0.61 (moisture correction for temp.)
Q    = (0.01.*RH).*qsat(Ta,Pa);    % specific humidity of air [kg/kg]
T    = Ta+CtoK;                    % convert to K
Tv   = T.*(1 + o61*Q);             % air virtual temperature
rhoa = (100*Pa)./(gas_const_R*Tv); % air density [kg/m^3]

