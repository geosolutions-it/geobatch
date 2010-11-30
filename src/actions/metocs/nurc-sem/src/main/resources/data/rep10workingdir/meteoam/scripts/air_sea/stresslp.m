function tau=stresslp(sp,z,rhoa)
% STRESSLP: computes neutral wind stress following Large and Pond (1981).
% tau = STRESSLP(sp,z,rhoa) computes the neutral wind stress given the wind
% speed at height z following Large and Pond (1981), J. Phys. Oceanog.,
% 11, 324-336. Air density is an optional input, otherwise assumed to
% be constant (1.22 kg/m^3). 
%
%   INPUT:   sp    - wind speed             [m/s]
%            z     - measurement height     [m]
%            rhoa  - air_density (optional) [kg/m^3]
%
%   OUTPUT:  tau   - wind stress            [N/m^2]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/26/98: version 1.1 (revised by RP)
% 4/2/99: version 1.2 (optional air density added by AA)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% get constants
as_consts;

if nargin == 2
  rhoa = rho_air; 
end

[cd,u10]=cdnlp(sp,z);
tau=rhoa.*(cd.*u10.^2);

