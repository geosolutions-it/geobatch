function dq=delq(Ts,Ta,rh)
% DELQ: computes air-sea specific humidity difference.
% dq=DELQ(Ts,Ta,rh) computes the specific humidity (kg/kg) difference
% between the air (as determined by relative humidty rh and air
% temperature Ta measurements) and the sea surface (where q is 
% assumed to be at 98% satuation at the sea surface temperature Ts).
% DELQ uses QSAT based on Tetens' formula for saturation vapor
% pressure from Buck (1981), J. App. Meteor., 1527-1532.  The
% dependence of QSAT on pressure is small (<0.5%) and has been
% removed using a mean pressure of 1020 mb.  
%
%    INPUT:   Ts - sea surface temperature  [C]
%             Ta - air temperature  [C]
%             rh - relative humidity  [%]
%
%    OUTPUT:  dq - air-sea specific humidity difference  [kg/kg]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 4/10/98: version 1.1
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

dq=0.01.*rh.*qsat(Ta) - 0.98.*qsat(Ts);


