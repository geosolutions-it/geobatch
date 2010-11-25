function L=vapor(t)
% VAPOR calculates heat of evaporation for pure water 
% L=VAPOR(t)computes the heat of evaporation for pure water. This can
% be used to compute the fresh water flux from latent heat flux.
%
% INPUT:   t - water temperature  [C]
%
% OUTPUT:  L - heat of evaporation  [J/kg]

% Range of validity: 0 <= t <= 100 deg C.  Check value: at t=100 deg C, 
% L = 2.2566 x 10^6 J/kg.  Reference: Landolt-Bornstein, Numerical Data 
% and Functional Relationships in Science and Technology. New Series, 
% Sundermann, J. (editor), vol. 3, subvol. a, Springer-Verlag, p. 256.
% No formulas are known to be available for the change of the heat of
% evaporation as function of salinity. 

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 1/22/99: version 1.0 (contributed by RO)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

a0=2.50039e6;
a1=-2.3683e3;
a2=4.31e-1;
a3=-1.131e-2;
L=a0+a1*t+a2*t.*t+a3*t.*t.*t;

