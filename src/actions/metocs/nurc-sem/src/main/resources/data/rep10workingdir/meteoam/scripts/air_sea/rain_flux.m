function [tau_rain,heat_rain] = rain_flux(Ta,Pa,rh,rain,Ts,sal,u,zu)
% RAIN_FLUX: computes heat and momentum flux due to rain.
% RAIN_FLUX computes heat flux and momentum flux due to rain. This 
% code follows the Fortran program bulk_v25b.f. For more details, 
% see Fairall et al. (1996), JGR, 101, 3751-3752.
%
% INPUT:  Ta        -  air temperature          [C]
%         Pa        -  air pressure             [mb]
%         rh        -  relative humidity        [%]
%         rain      -  rain rate                [mm/hr]
%         Ts        -  sea surface temperature  [C]
%         sal       -  salinity                 [psu (PSS-78)]
%         u         -  wind speed               [m/s]
%         zu        -  wind measurement height  [m]
%
% OUTPUT: tau_rain  -  momentum flux of rainfall            [N/m^2]
%         heat_rain -  heat flux of rainfall (OUT of ocean) [W/m^2]
%
% USAGE: [tau_rain,heat_rain] = RAIN_FLUX(Ta,Pa,rh,rain,Ts,sal,u,zu) 
  
% NOTE: All input variables should be vectors (either row or column), zu
%       may also be a fixed scalar. Output variables are column vectors.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 4/3/99: version 1.2 (contributed by AA)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% -> column vectors
Ta = Ta(:); Pa = Pa(:); rh = rh(:); rain = rain(:); Ts = Ts(:);
sal = sal(:); u = u(:); zu = zu(:);

% get constants
as_consts;
cpa = cp;


o61  = 1/eps_air - 1;              % ~0.61 (moisture correction for temp.)
Qa   = 0.01*rh.*qsat(Ta,Pa);       % specific humidity of air [kg/kg]
T    = Ta + CtoK;                  % C -> K
Tv   = T.*(1 + o61*Qa);            % air virtual temperature
rhoa = (100*Pa)./(gas_const_R*Tv); % air density
Le   = (2.501-0.00237*Ts)*1e6;     % latent heat of vaporization at Ts
Qs   = Qsat_coeff*qsat(Ts,Pa);     % saturation specific humidity


% compute heat flux of rainfall OUT of ocean
dwat   = 2.11e-5*(T./CtoK).^1.94;    % water vapour diffusivity
dtmp   = (1+3.309e-3*Ta-1.44e-6*Ta.^2)*0.02411./(rhoa.*cpa); % heat diffusivity
dqs_dt = Qa.*Le./(gas_const_R*T.^2); % Clausius-Clapeyron
alfac  = 1./(1+0.622*(dqs_dt.*Le.*dwat)./(cpa.*dtmp)); % wet bulb factor
cpw    = sw_cp(sal,Ts,0);            % heat capacity of sea water 

heat_rain = rain.*alfac.*cpw.*((Ts-Ta)+(Qs-Qa).*Le./cpa)/3600;

% compute momentum flux of rainfall
[cd10,u10] = cdntc(u,zu,Ta);% use Smith's formula to compute wind speed at 10m
tau_rain   = rain.*u10/3600;

