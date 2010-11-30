function tau=stresstc(sp,z,Ta,rhoa)
% STRESSTC: computes the neutral wind stress following Smith (1988).
% tau = STRESSTC(sp,z,Ta,rhoa) computes the neutral wind stress given the 
% wind speed and air temperature at height z following Smith (1988),
% J. Geophys. Res., 93, 311-326. Air temperature and density are optional
% inputs. 
%
%   INPUT:  sp    - wind speed   [m/s]
%           z     - measurement height  [m]
%           Ta    - air temperature (optional) [C]
%           rhoa  - air density (optional)  [kg/m^3]
%
%   OUTPUT: tau   - wind stress  [N/m^2]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/26/98: version 1.1 (revised by RP)
% 4/2/99: versin 1.2 (air density option added by AA)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% load constants
as_consts;

if nargin == 2,
  Ta   = Ta_default;
  rhoa = rho_air;
elseif nargin == 3
  rhoa = rho_air; 
end

[cd,u10] = cdntc(sp,z,Ta);
tau = rhoa*(cd.*u10.^2);

