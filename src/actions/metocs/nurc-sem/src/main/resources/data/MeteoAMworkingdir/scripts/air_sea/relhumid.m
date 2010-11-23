function rh=relhumid(Td,Tw,P,p_typ)
%  RELHUMID: finds relative humidity from wet/dry thermometer readings.
%  rh=relhumid(Td,Tw,Pa,type) computes the relative humidity from
%  wt and dry-bulb temperature measurements using the psychrometric eqn.
%  and constants from Sargent (1980), Meteorol. Mag. 109, 238-246. The
%  latter two inputs are optional.
%
%  INPUTS : Td - dry bulb thermometer  [C]
%           Tw - wet thermometer  [C]
%           Pa - air pressure (optional)  [mb]
%           type - 'assman' for Assman-type forced ventilation
%                  'screen' for standard screen (natural ventilation)
%
%  OUTPUT:  rh - relative humidity  [%]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 8/28/98: version 1.1 (contributed by RP)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

as_consts;

if nargin==2,
 P=P_default;
 p_typ=psych_default;
elseif nargin==3,
 if isstr(P),
   p_typ=P;
   P=P_default;
 else
   p_typ=psych_default;
 end;
end;

% psychrometric coefficient

switch p_typ,
  case 'screen',
    A=0.000799;   % natural screens
  case 'assman',
    A = 0.000667; % Assmann-type with forced ventilation
  otherwise
    error(['unknown psychrometer type: ' p_typ]);
end;

% compute saturation vapour pressure for both temps.
ed=satvap(Td,P);
ewp=satvap(Tw,P);

% The psychrometric eqn!
e = ewp - A*P.*(Td-Tw);  % ambient vapour pressure

rh= e./ed * 100;

