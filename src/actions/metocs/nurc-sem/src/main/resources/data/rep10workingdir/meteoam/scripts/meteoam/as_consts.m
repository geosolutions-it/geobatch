% CONSTANTS returns values of a number of constants
%
%

% ------- Physical constants

g = 9.8;   % (m/s^2) Acceleration due to gravity
sigmaSB = 5.6697e-8;  % W/m^2/K^4  Stefan-Boltzmann constant
eps_air=0.62197;  % Molecular weight ratio (water/air)
gas_const_R=287.04;  % J/kg/K Gas constant for dry air

CtoK = 273.16;  % Conversion factor for deg C to K

% ------- Meteorological constants

kappa=0.4;    % von Karman's constant

Charnock_alpha=0.011;  % Charnock constant (for determining roughness length
                       % at sea given friction velocity), used in Smith
              % formulas for drag coefficient and also in Fairall and Edson.
              %
              % use alpha=0.011 for the open-ocean
              % and alpha=0.018 for fetch-limited (coastal) regions. 

R_roughness=0.11;  % limiting Roughness Reynolds # for 
                   % aerodynamically smooth flow         
               

% ------ Defaults suitable for boundary-layer studies

cp=1004.7;    % J/kg/K Heat capacity of air
rho_air = 1.22;  % kg/m^2  air density (when required as a constant)
Ta_default = 10; % deg C   default air temperature
P_default = 1020; % mbars  default air pressure
psych_default = 'screen';  % Default psychmometer type (see relhumid.m)


   % the following are useful in hfbulktc.m (and are the defaults
   % used in Fairall et al (1996) ).
CVB_depth=600;     % (m) Depth of convective boundary layer in atmosphere
min_gustiness=0.5; % (m/s) Minimum "gustiness" (i.e. unresolved fluctuations)
                   % should keep this strictly >0 otherwise bad stuff might
                   % happen (divide by zero errors)
Beta=1.25;         % scaling constant for gustiness

% ------ Short-wave flux calculations

Solar_const=1368.0;   % The solar constant (1368.0 W/m^2) represents a 
                      % mean of satellite measurements made over the 
                      % last sunspot cycle (1979-1995) taken from 
                      % Coffey et al (1995), Earth System Monitor, 6, 6-10.
                      

% ------ Long-wave flux calculations

emiss_lw = 0.985;     % Long-wave emissivity of ocean from Dickey et al, 
                      % J. Atmos. Oceanic Tech., 11, 1057-1076, 1994.

bulkf_default = 'berliand';  % Default bulk formula when downward long-wave
                             % measurements are not made
                             
                             
