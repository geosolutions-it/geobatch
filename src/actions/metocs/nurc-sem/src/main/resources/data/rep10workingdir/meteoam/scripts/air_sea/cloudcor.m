function Fc=cloudcor(C,optns,lat)
% CLOUDCOR: computes cloud correction factor for bulk long-wave flux.
% Fc=CLOUDCOR(C,optns,lat) computes the cloud correction factor F(C)
% as a function of the cloud fraction C for bulk long-wave flux formulae.
% In general, these are functions of the form
%             1 - a_n*C^n
% Since the coefficients and powers depend a lot on the dominant cloud
% type which may vary from region to region and season to season, it is
% not clear which parametrization is best (see Fung et al (1984), 
% Rev. of Geophys. and Space Phys., 22, 177-193). 
%
% The particular parametrization used here depends on the second input
% variable, for which no default is given to emphasize the fact that you
% really need to understand what you are doing here!
%
% optns = [a1 a2] = use a correction factor of [1-a1*C-a2*C^2].
%
% There are several "built-in" formulae (from Fung et al) that all have
% a latitude-dependence of some kind.
%
% optns = 'clarke',lat = Clarke (1974) corrections for abs(lat)<50.
%       = 'bunker',lat = Bunker (1976) corrections for N Atlantic.
%
% INPUT:   C - cloud fraction
%          optns - see above for details
%          lat - latitude [deg] - required for "built-in" formulae only
%
% OUTPUT:  Fc - correction factor used as input to BLWHF

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/12/98: version 1.1 (contributed by RP)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

if isstr(optns),

  switch optns(1),
    case 'c',
      a1=0;
      if     abs(lat)>55, a2=NaN;
      elseif abs(lat)>45, a2=0.73;
      elseif abs(lat)>35, a2=0.69;
      elseif abs(lat)>25, a2=0.64;
      elseif abs(lat)>15, a2=0.60;
      elseif abs(lat)> 7, a2=0.56;
      elseif abs(lat)> 2, a2=0.53;
      else                a2=0.51;
      end;
        
    case 'b',
      a2=0;
      if     abs(lat)>75, a1=0.84;
      elseif abs(lat)>65, a1=0.80;
      elseif abs(lat)>55, a1=0.76;
      elseif abs(lat)>45, a1=0.72;
      elseif abs(lat)>35, a1=0.68;
      elseif abs(lat)>25, a1=0.63;
      elseif abs(lat)>15, a1=0.59;
      elseif abs(lat)>7,  a1=0.52;
      else                a1=0.50;
      end;
      
    otherwise
      error('Unrecognized option');
  end;
else
 a1=optns(1);a2=optns(2);
end;

Fc = 1 - a1*C - a2.*C.^2;
