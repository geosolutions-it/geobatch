function visc = sw_visc(S,T,P)

% SW_VISC   kinematic viscosity 
%===========================================================================
% SW_VISC  $Revision: 0.0 $  $Date: 1998/01/19 $
%          Copyright (C) Ayal Anis 1998. 
%
% USAGE:  visc = sw_visc(S,T,P) 
%
% DESCRIPTION:
%    Calculates kinematic viscosity of sea-water. 
%    based on Dan Kelley's fit to Knauss's TABLE II-8
%
% INPUT:  (all must have same dimensions)
%   S  = salinity    [psu      (PSS-78) ]
%   T  = temperature [degree C (IPTS-68)]
%   P  = pressure    [db]
%       (P may have dims 1x1, mx1, 1xn or mxn for S(mxn) )
%
% OUTPUT:
%   visc = kinematic viscosity of sea-water [m^2/s] 
%
%   visc(40.,40.,1000.)=8.200167608E-7
%
% DISCLAIMER:
%   This software is provided "as is" without warranty of any kind.  
%=========================================================================

% CALLER:  general purpose
% CALLEE:  sw_dens.m

%-------------
% CHECK INPUTS
%-------------
if nargin ~= 3
   error('sw_visc.m: Must pass 3 parameters ')
end 

% CHECK S,T dimensions and verify consistent
[ms,ns] = size(S);
[mt,nt] = size(T);

  % CHECK THAT S & T HAVE SAME SHAPE
if (ms~=mt) | (ns~=nt)
   error('check_stp: S & T must have same dimensions')
end %if

% LET sw_dens.m DO DIMENSION CHECKING FOR P

% IF ALL ROW VECTORS ARE PASSED THEN LET US PRESERVE SHAPE ON RETURN.
Transpose = 0;
if ms == 1  % row vector
   T       =  T(:);
   S       =  S(:);   

   Transpose = 1;
end %if

%------
% BEGIN
%------

visc = 1e-4*(17.91-0.5381*T+0.00694*T.^2+0.02305*S)./sw_dens(S,T,P);

if Transpose
   visc = visc';
end %if

return      
%=========================================================================

