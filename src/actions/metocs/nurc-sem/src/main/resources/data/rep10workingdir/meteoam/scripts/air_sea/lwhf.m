function qlw=lwhf(Ts,dlw,dsw)
% LWHF: computes net longwave heat flux following Dickey et al (1994).
% qlw=LWHF(Ts,dlw) computes the net longwave heat flux into the ocean.
% Following Dickey et al (1994), J. Atmos. Oceanic Tech., 11, 1057-1078,
% the incident longwave flux can be corrected for sensor heating due to
% insolation if you are using Epply or Kipp & Zonen CG1 pyrgeometers.
% In this case, use qlw=LWHF(Ts,dlw,dsw). Epply is the default 
% pyrgeometer; change code for the Kipp & Zonen instrument.
%
%   INPUT:  Ts  - sea surface temperature [C]
%           dlw - (measured) downward longwave flux [W/m^2]
%           dsw - (measured) insolation [W/m^2] (needed for Eppley 
%                 or Kipp & Zonen pyrgeometers)
%
%   OUTPUT: qlw - net longwave heat flux [W/m^2]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/19/98: version 1.1 (revised for non-Epply pyrgeometers by RP)
% 4/9/99: version 1.2 (included Kipp & Zonen CG1 pyrgeometers by AA)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% get constants
as_consts;

% convert degC to degK
ts=Ts+CtoK;

% correct dlw for sensor heating by insolation
if nargin==3,
  % this line is for Epply pyrgeometers
  dlwc=dlw-0.036.*dsw; 

  % this line is for Kipp & Zonen CG1 pyrgeometers  
  % (the offset is specified as 25 W/m^2 at 1000 W/m^2)
  % dlwc=dlw-0.025.*dsw;
else
  dlwc=dlw;
end;

% compute upward gray-body longwave flux
lwup=-emiss_lw.*sigmaSB.*(ts.^4);

% compute net flux
qlw=lwup + emiss_lw.*dlwc;

