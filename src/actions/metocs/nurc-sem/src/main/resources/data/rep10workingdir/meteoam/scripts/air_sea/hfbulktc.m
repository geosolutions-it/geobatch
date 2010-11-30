function A=hfbulktc(ur,zr,Ta,zt,rh,zq,Pa,Ts,sal,dlw,dsw,nsw)
% HFBULKTC: computes sensible and latent heat fluxes and other variables.
% A=HFBULKTC(ur,zr,Ta,zt,rh,zq,Pa,Ts,sal,dlw,dsw,nsw) computes the following:
%
%         Hs      = sensible heat flux INTO ocean [W/m^2]
%         Hl      = latent heat flux INTO ocean [W/m^2]
%         Hl_webb = Webb correction to latent heat flux INTO ocean [W/m^2]
%         stress  = wind stress [N/m^2]
%         U_star  = velocity friction scale [m/s]
%         T_star  = temperature scale [deg C]
%         Q_star  = humidity scale [kg/kg]
%         L       = Monin-Obukhov length [m]
%         zetu    = zr/L
%         CD      = drag coefficient
%         CT      = temperature transfer coefficient (Stanton number)
%         CQ      = moisture transfer coefficient (Dalton number)
%         RI      = bulk Richardson number
%         Dter    = cool-skin temperature difference (optional output) [C]; 
%                   positive if surface is cooler than bulk (presently no 
%                   warm skin permitted by model)
%                    
% Based on the following buoy input data:
%
%           ur     = wind speed [m/s] measured at height zr [m] 
%           Ta     = air temperature [C] measured at height zt [m]
%           rh     = relative humidity [%] measured at height zq [m]
%           Pa     = air pressure [mb]
%           Ts     = sea surface temperature [C]
%           sal    = salinity [psu (PSS-78)]
%                    (optional - only needed for cool-skin)
%           dlw    = downwelling (INTO water) longwave radiation [W/m^2]
%                    (optional - only needed for cool-skin)
%           dsw    = measured insolation [W/m^2]
%                    (optional - only needed for cool-skin)
%           nsw    = net shortwave radiation INTO the water [W/m^2]
%                    (optional - only needed for cool-skin)
%
% where ur, Ta, rh, Pa, Ts, zr, zt, and zq (and optional sal, dlw,
% dsw, and nsw) may be either row or column vectors; and rh, Pa, 
% zr, zt, and zq (and optional sal) may also be fixed scalars.
%
% Output variables are given as column vectors in A:
%
% 1) without cool-skin correction:
%
%   A=[Hs Hl Hl_webb stress U_star T_star Q_star L zetu CD CT CQ RI]
%
% 2) with cool-skin correction: 
%
%   A=[Hs Hl Hl_webb stress U_star T_star Q_star L zetu CD CT CQ RI Dter];

% Code follows Edson and Fairall TOGA COARE code (version 2.0), modified 
% to include Rogers' weighting factor for unstable conditions.  Code does
% include gustiness, and assumes that the marine boundary layer height is
% known and constant over time for simiplicity. zr/L is limited to 
% be <=3.0 to ensure that the code converges to nonzero stress and heat 
% flux values for strongly stable conditions.  The bulk Richardson number
% is computed between the sea surface and zr as a diagnostic about whether
% turbulent boundary layer theory is applicable.  Code does not include 
% warm layer effects to modify Ts.  See Fairall et al (1996), J. Geophys. 
% Res., 101, 3747-3764, for description of full TOGA COARE code and 
% comparison with data. 

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 8/19/98: version 1.1 (rewritten by RP to remove inconsistencies in 
%          virtual and real temperatures, improve loop structure, 
%          correct gustiness component of stress computation) 
% 4/9/99: version 1.2 (rewritten by AA to clarify some variable names
%         and include cool-skin effect and Webb correction to latent 
%         heat flux added to output matrix)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

M=length(ur);

% change to column vectors
ur = ur(:);
Ta = Ta(:);
rh = rh(:);
Ts = Ts(:);
Pa = Pa(:);
zr = zr(:);
zt = zt(:);
zq = zq(:);

% create vectors for rh, Pa, zr, zt, and zq, if scalars are input
if length(rh)==1 & M>1
  rh=rh*ones(M,1);
end
if length(Pa)==1 & M>1
  Pa=Pa*ones(M,1);
end
if length(zr)==1 & M>1
  zr=zr*ones(M,1);
end
if length(zt)==1 & M>1
  zt=zt*ones(M,1);
end
if length(zq)==1 & M>1
  zq=zq*ones(M,1);
end

if nargin > 8
  % optional cool-skin stuff
  sal = sal(:);
  dlw = dlw(:);
  dsw = dsw(:);
  nsw = nsw(:);
  % create vector for sal if scalar is input
  if length(sal)==1 & M>1
    sal=sal*ones(M,1);
  end
end

% initialize various constants
as_consts;

tol=.001;    % tolerance on Re changes to make sure soln has converged.

onethird=1./3;
o61=1/eps_air-1;   % 0.61 (moisture correction for temperature)

visc=viscair(Ta);                 % viscosity
Qsats=Qsat_coeff*qsat(Ts,Pa);     % saturation specific humidity; the Qsat_coeff
                                  % value is set in routine as_consts.m
Q=(0.01.*rh).*qsat(Ta,Pa);        % specific humidity of air [kg/kg]
T =Ta+CtoK;                       % convert to K
Tv=T.*(1 + o61*Q);                % air virtual temperature
rho=(100*Pa)./(gas_const_R*Tv);   % air density
Dt=(Ta+0.0098.*zt)-Ts;            % adiabatic temperature difference
Dq=Q-Qsats;                       % humidity difference

% compute initial neutral scaling coefficients
S=sqrt(ur.^2 + min_gustiness.^2);
cdnhf=sqrt(cdntc(S,zr,Ta)); % Smith's neutral cd as first guess

z0t=7.5*10^(-5);
ctnhf=kappa./log(zt./z0t);

z0q=z0t;
cqnhf=kappa./log(zq./z0q);

U_star = cdnhf.*S;      % (includes gustiness)
T_star = ctnhf.*Dt;     % 
Q_star = cqnhf.*Dq;     %

Dter   = 0;
Dqer   = 0;
if nargin > 8
% initial cool-skin thickness guess  
  delta = 0.001*ones(size(Ts));
end

Reu=0;Ret=0;Req=0;
% begin iteration loop to compute best U_star, T_star, and Q_star
for iter1=1:80;

    ReuO=Reu; RetO=Ret; ReqO=Req; % Save old values
    
    % Compute Monin-Obukov length (NB - definition given as eqn (7)
    % of Fairall et al (1996) probably wrong, following, e.g.
    % Godfrey and Bellars (1991), JGR, 96, 22043-22048 and original code)
    bs=g*(T_star.*(1 + o61*Q) + o61*T.*Q_star)./Tv; 
    L=(U_star.^2)./(kappa*bs);
    % set upper limit on zr/L = 3.0 to force convergence under 
    % very stable conditions. Assume that zr, zt and zq comparable.
    index_limit   = (L<zr/3 & L>0);
    L(index_limit)=zr(index_limit)/3;
    
    zetu=zr./L;  % nondimensionalized heights
    zett=zt./L;
    zetq=zq./L;

    % surface roughness
    z0=(Charnock_alpha/g).*U_star.^2 + R_roughness.*visc./U_star;

    % compute U_star correction for non-neutral conditions
    cdnhf=kappa./(log(zr./z0)-psiutc(zetu));
    U_star=cdnhf.*S;
  
    Reu=z0.*U_star./visc;   % roughness Reynolds #
    [Ret,Req]=LKB(Reu);  % compute other roughness Reynolds #s

    % compute t and q roughness scales from roughness R#s
    z0t=visc.*Ret./U_star;
    z0q=visc.*Req./U_star;

    % compute new transfer coefficients at measurement heights
    cthf=kappa./(log(zt./z0t)-psittc(zett));
    cqhf=kappa./(log(zq./z0q)-psittc(zetq));

    % compute new values of T_star, Q_star
    T_star=cthf.*(Dt + Dter);
    Q_star=cqhf.*(Dq + Dqer);

    % estimate new gustiness
    Ws=U_star.*(-CVB_depth./(kappa*L)).^onethird;
    wg=min_gustiness*ones(M,1);
    j=find(zetu<0);                 % convection in unstable conditions only
    wg(j)=max(min_gustiness,beta_conv.*Ws(j)); % set minimum gustiness
    S=sqrt(ur.^2 + wg.^2);

    if nargin > 8
    % compute cool-skin parameters
      [delta,Dter,Dqer] = cool_skin(sal,Ts-Dter,rho,cp,Pa, ...
                                    U_star,T_star,Q_star, ...
                                    dlw,dsw,nsw,delta,g,gas_const_R, ...
                                    CtoK,Qsat_coeff);
    end

end % end of iteration loop

ii= abs(Reu-ReuO)>tol | abs(Ret-RetO)>tol | abs(Req-ReqO)>tol;
if any(ii),
 disp(['Algorithm did not converge for ' int2str(sum(ii)) ' values. Indices are:']);
 disp(find(ii)');
 warning('Not converged!');
end;


% compute latent heat
Le=(2.501-0.00237*(Ts-Dter))*10^6;

% compute fluxes into ocean
Hs=rho.*cp.*U_star.*T_star;
Hl=rho.*Le.*U_star.*Q_star;

% compute transfer coefficients at measurement heights
CD=(U_star./S).^2;
CT=U_star.*T_star./(S.*(Dt + Dter)); % Stanton number
CQ=U_star.*Q_star./(S.*(Dq + Dqer)); % Dalton number

% to compute mean stress, we don't want to include the effects
% of gustiness which average out (in a vector sense).
stress=rho.*CD.*S.*ur;

% compute bulk Richardson number (as a diagnostic) - the "T"
% is probably not quite right - assumes T \ approx. Ts (good enough though)
RI=g.*zr.*((Dt + Dter) + o61*T.*(Dq + Dqer))./(Tv.*S.^2);

% compute Webb correction to latent heat flux into ocean
W = 1.61.*U_star.*Q_star + (1 + 1.61.*Q).*U_star.*T_star./T; % eqn. 21
Hl_webb = rho.*Le.*W.*Q; % eqn. 22, Fairall et al. (1996), JGR, 101, p3751.

% output array
if nargin > 8
  % output additional cool-skin parameters 
  A=[Hs Hl Hl_webb stress U_star T_star Q_star L zetu CD CT CQ RI Dter];
else
  % otherwise
  A=[Hs Hl Hl_webb stress U_star T_star Q_star L zetu CD CT CQ RI];
end



function y=psiutc(zet)
% PSIUTC: computes velocity profile function following TOGA/COARE.
% y=PSIUTC(zet) computes the turbulent velocity profile function given 
% zet = (z/L), L the Monin-Obukoff length scale, following Edson and
% Fairall TOGA COARE code (version 2.0) as modified to include Rogers' 
% weighting factor to combine the Dyer and free convection forms for 
% unstable conditions. 
 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 8/28/98: version 1.1
% 8/5/99: version 1.2
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

c13=1.0./3.0;
sq3=sqrt(3.0);

% stable conditions
y=-4.7*zet;

% unstable conditions
j=find(zet<0);
zneg=zet(j);

% nearly stable (standard functions)
 x=(1-16.0.*zneg).^0.25;
 y1=2.0.*log((1+x)./2) + log((1+x.^2)./2) -2.*atan(x) + pi/2;

% free convective limit
 x=(1-12.87*zneg).^c13;
 y2=1.5*log((x.^2+x+1)./3) - sq3*atan((2.*x+1)/sq3) + pi/sq3;
	
% weighted sum of the two
 F=1.0./(1+zneg.^2);
 y(j)=F.*y1+(1-F).*y2;




function y=psittc(zet)
% PSITTC: computes potential temperature profile following TOGA/COARE.
% y=PSITTC(zet) computes the turbulent potential temperature profile 
% function given zet = (z/L), L the Monin-Obukoff length scale, following 
% Edson and Fairall TOGA COARE code (version 2.0), as modified to use
% Rogers' weighting factor to combine the Dyer and free convective 
% forms for unstable conditions. 

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 8/28/98: version 1.1
% 8/5/99: version 1.2
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

c13=1.0./3.0;
sq3=sqrt(3.0);

% stable conditions
y=-4.7.*zet;

% unstable conditions
j=find(zet<0);
zneg=zet(j);

% nearly stable (standard functions)
 x=(1-16.0.*zneg).^0.25;
 y1=2.0*log((1+x.^2)./2);

% free convective limit
 x=(1-12.87*zneg).^c13;
 y2=1.5.*log((x.^2+x+1)./3.0) - sq3.*atan((2.*x+1)./sq3) + pi./sq3;

% weighted sum of the two
 F=1.0./(1+zneg.^2);
 y(j)=F.*y1 + (1-F).*y2;





function [Ret,Req]=LKB(Reu);
% LKB: computes rougness Reynolds numbers for temperature and humidity
% [Ret,Req]=LKB(Reu) computes the roughness Reynolds for temperature
% and humidity following Liu, Katsaros and Businger (1979), J. Atmos.
% Sci., 36, 1722-1735.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 8/28/98: version 1.1
% 8/5/99: version 1.2
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

   Ret=.177*ones(size(Reu));
   Req=.292*ones(size(Reu));
j=find(Reu>.11 & Reu<=.825);
   Ret(j)=1.376.*Reu(j).^0.929;
   Req(j)=1.808.*Reu(j).^0.826;
j=find(Reu>.825 & Reu<=3);
   Ret(j)=1.026./Reu(j).^0.599;
   Req(j)=1.393./Reu(j).^0.528;
j=find(Reu>3 & Reu<=10);
   Ret(j)=1.625./Reu(j).^1.018;
   Req(j)=1.956./Reu(j).^0.870;
j=find(Reu>10 & Reu<=30);
   Ret(j)=4.661./Reu(j).^1.475;
   Req(j)=4.994./Reu(j).^1.297;
j=find(Reu>30);
   Ret(j)=34.904./Reu(j).^2.067;
   Req(j)=30.790./Reu(j).^1.845;
