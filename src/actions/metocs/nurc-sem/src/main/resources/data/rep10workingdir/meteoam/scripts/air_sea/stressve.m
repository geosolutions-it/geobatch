function tau=stressve(sp,z,rhoa)
% STRESSVE: computes stress using Vera (1983) neutral drag law.
% tau = STRESSVE(sp,z,rhoa) computes the neutral wind stress given the wind
% speed at height z following Vera (1983) [see Large, Morzel, and Crawford
% (1995), J. Phys. Oceanog., 25, 2959-2971 (eqn. 8)]. Assumes z a fixed
% scalar. Air density is an optional input.
%
%   INPUT:  sp    - wind speed  [m/s]
%           z     - measurement height  [m]
%           rhoa  - air density (optional)  [kg/m^3]
%
%   OUTPUT: tau   - wind stress  [N/m^2]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/26/98: version 1.1 (revised by RP)
% 4/2/99: version 1.2 (air density option added by AA)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% load constants
as_consts;

if nargin == 2
  rhoa = rho_air; 
end

[cd,u10]=cdnve(sp,z);
tau=rhoa*(cd.*u10.^2);



