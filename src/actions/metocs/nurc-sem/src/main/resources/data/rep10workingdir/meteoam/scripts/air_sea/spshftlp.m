function [sp2,ustar]=spshfttc(sp1,z1,z2)
% SPSHFTTC: adjusts wind speed from z1 to z2 following Large&Pond (1981).
% sp2 = SPSHFTLP(sp1,z1,z2) shifts the wind speed sp1 measured at z1 to
% z2 using the neutral drag coefficient given the wind speed and air
% temperature at height z following Large and Pond (1981), J. Phys. Oceanog.,
% 11, 324-336. 
%
%   INPUT:  sp1 - measured wind speed  [m/s]
%           z1 - measurement height [m]
%           z2 - desired height [m]
%
%   OUTPUT: sp2 - predicted wind speed  [m/s]
%           ustar - friction velocity  [m/s]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/27/98: version 1.1 (revised to use CDNLP efficiently by RP)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% get constants
as_consts;

% find cd and ustar
[cd10,sp10]=cdnlp(sp1,z1);

ustar=sqrt(cd10).*sp10;

sp2=sp10+ustar.*log(z2./10)/kappa;

