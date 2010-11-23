function lwest = blwhf(Ts,Ta,rh,Fc,bulk_f)
% BLWHF: estimates net long-wave heat flux using bulk formulas
% lwest = BLWHF(Ts,Ta,rh,Fc,bulk_f) estimates the net longwave 
% heat flux into the ocean using one of a number of bulk formulas 
% evaluated by Fung et al (1984), Rev. Geophys. Space Physics, 
% 22,177-193. 
%
% INPUT:   Ts - sea surface temperature [C]
%          Ta - air temperature  [C]
%          rh - relative humidity  [%]
%          Fc - cloudiness correction factor F(C) (=1 for clear sky) 
%                  (see cloudcor.m for more information)
%          bulk_f - bulk formulas to be used 
%                  (see Fung et al, 1984) for details). 
%                   Options are:
%                     'brunt'
%                     'berliand'  - probably the best one (default)
%                     'clark'
%                     'hastenrath'
%                     'efimova'
%                     'bunker'
%                     'anderson'
%                     'swinbank' - does not use rh
%
% OUTPUT:  lwest - net downward longwave flux [W/m^2]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 8/31/98: version 1.1 (contributed by RP)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% load constants and defaults
as_consts; 

if nargin<5,
 bulk_f=bulkf_default;
end;

% compute vapour pressure from relative humidity (using formulas 
% from Gill, 1982)
ew=satvap(Ta);
rw=eps_air*ew./(P_default-ew);
r=(rh/100).*rw;
e_a=r*P_default./(eps_air+r);

% convert to K
ta=Ta+CtoK;
ts=Ts+CtoK;

% signs for flux INTO ocean
switch bulk_f,
  case {'brunt',1}
    lwest =  -emiss_lw*sigmaSB.*ts.^4.*(0.39 - 0.05*sqrt(e_a)).*Fc;

  case {'berliand',2}
    lwest =  -emiss_lw*sigmaSB.*ta.^4.*(0.39 - 0.05*sqrt(e_a)).*Fc ...
             - 4*emiss_lw*sigmaSB.*ta.^3.*(Ts - Ta);

  case {'clark',3}
    lwest =  -emiss_lw*sigmaSB.*ts.^4.*(0.39 - 0.05*sqrt(e_a)).*Fc ...
             - 4*emiss_lw*sigmaSB.*ts.^3.*(Ts - Ta);

  case {'hastenrath',4}
    q_a = e_a*eps_air./P_default;
    lwest =  -emiss_lw*sigmaSB.*ts.^4.*(0.39 - 0.056*sqrt(q_a)).*Fc ...
             - 4*emiss_lw*sigmaSB.*ts.^3.*(Ts - Ta);

  case {'efimova',5}
    lwest =  -emiss_lw*sigmaSB.*ta.^4.*(0.254 - 0.00495*(e_a)).*Fc ;
    
  case {'bunker',6}
    lwest =  -0.022*emiss_lw*sigmaSB.*ta.^4.*(11.7 - 0.23*(e_a)).*Fc ...
             - 4*emiss_lw*sigmaSB.*ta.^3.*(Ts - Ta);

  case {'anderson',5}
    lwest =  -emiss_lw*sigmaSB.*(ts.^4 - ta.^4.*(0.74+0.0049*(e_a))).*Fc ;
 
  case {'swinbank',5}
    lwest =  -emiss_lw*sigmaSB.*(ts.^4 - 9.36e-6*ta.^6).*Fc ;

   otherwise
    error(['Unrecognized bulk formula specified: ' bulk_f]);
end;

