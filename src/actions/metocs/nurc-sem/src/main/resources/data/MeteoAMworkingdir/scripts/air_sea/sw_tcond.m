function tcond = sw_tcond(S,T,P)

% SW_TCOND   thermal conductivity 
%===========================================================================
% SW_TCOND  $Revision: 0.0 $  $Date: 1998/01/19 $
%           Copyright (C) Ayal Anis 1998. 
%
% USAGE:  tcond = sw_tcond(S,T,P) 
%
% DESCRIPTION:
%    Calculates thermal conductivity of sea-water. 
%    Two options (Note: one option has to be remarked): 
%    1) based on Caldwell's DSR 21:131-137 (1974)  EQN. 9
%    2) based on Catelli et al.'s DSR 21:311-3179(1974)  EQN. 5
%
% INPUT:  (all must have same dimensions)
%   S  = salinity    [psu      (PSS-78) ]
%   T  = temperature [degree C (IPTS-68)]
%   P  = pressure    [db]
%       (P may have dims 1x1, mx1, 1xn or mxn for S(mxn) )
%
% OUTPUT:
%   tcond = thermal conductivity of sea-water [W/m/K] 
%
%   tcond(35,20,0)=0.5972
%
% DISCLAIMER:
%   This software is provided "as is" without warranty of any kind.  
%=========================================================================

% CALLER:  general purpose
% CALLEE:  none

%----------------------
% CHECK INPUT ARGUMENTS
%----------------------
if nargin ~= 3
   error('sw_tcond.m: Must pass 3 parameters ')
end 

% CHECK S,T,P dimensions and verify consistent
[ms,ns] = size(S);
[mt,nt] = size(T);
[mp,np] = size(P);

  
% CHECK THAT S & T HAVE SAME SHAPE
if (ms~=mt) | (ns~=nt)
   error('check_stp: S & T must have same dimensions')
end %if

% CHECK OPTIONAL SHAPES FOR P
if     mp==1  & np==1      % P is a scalar.  Fill to size of S
   P = P(1)*ones(ms,ns);
elseif np==ns & mp==1      % P is row vector with same cols as S
   P = P( ones(1,ms), : ); %   Copy down each column.
elseif mp==ms & np==1      % P is column vector
   P = P( :, ones(1,ns) ); %   Copy across each row
elseif mp==ms & np==ns     % P is a matrix size(S)
   % shape ok 
else
   error('check_stp: P has wrong dimensions')
end %if
[mp,np] = size(P);
 

% IF ALL ROW VECTORS ARE PASSED THEN LET US PRESERVE SHAPE ON RETURN.
Transpose  = 0;
if mp == 1  % row vector
   P       =  P(:);
   T       =  T(:);
   S       =  S(:);   

   Transpose = 1;
end

%------
% BEGIN
%------

% 1) Caldwell's option # 2 - simplified formula, accurate to 0.5% (eqn. 9)
tcond1 = 0.001365*(1+0.003*T-1.025e-5*T.^2+0.0653*(1e-4*P)-0.00029*S); % [cal/cm/C/sec]
tcond = tcond1*418.4; % [cal/cm/C/sec] ->[W/m/K] 

% 2) Castelli's option
%tcond2 = 100*(5.5286e-3+3.4025e-8*P+1.8364e-5*T-3.3058e-9*T.^3); % [W/m/K] 
%tcond = tcond2 % [W/m/K] 

if Transpose
   tcond  = tcond';
end

return      
%=========================================================================

