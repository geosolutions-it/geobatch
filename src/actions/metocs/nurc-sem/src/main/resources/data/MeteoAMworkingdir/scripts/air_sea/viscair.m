function  vis=viscair(Ta)
% VISCAIR: computes viscosity of air 
% vis=VISCAIR(Ta) computes the kinematic viscosity of dry air as a 
% function of air temperature following Andreas (1989), CRREL Report 
% 89-11.
%
% INPUT:   Ta  -  air temperature  [C]
%
% OUTPUT:  vis  -  air viscosity  [m^2/s]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

vis = 1.326e-5*(1 + 6.542e-3*Ta + 8.301e-6*Ta.^2 - 4.84e-9*Ta.^3);

