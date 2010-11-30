function [delta,Dter,Dqer] = cool_skin(sal,Tw,rhoa,cpa,Pa, ...
                                        U_star,T_star,Q_star, ...
                                        dlw,dsw,nsw,delta,g,Rgas, ...
                                        CtoK,Qsat_coeff)
% COOL_SKIN: compute the cool-skin parameters.
% COOL_SKIN computes the cool-skin parameters. This code follows 
% the fortran program bulk_v25b.f. For more details, see the cool-skin
% and warm layer paper by Fairall et al (1996), JGR, 101, 1295-1308. 
% All input variables should be vectors (either row or column), except 
% Rgas, CtoK, Qsat_coeff, and g, which can be scalars. Uses some
% functions from CSIRO SEAWATER TOOLBOX.
%
% INPUT:  sal       -  salinity [psu (PSS-78)]
%         Tw        -  water surface temperature [C]
%         rhoa      -  air density [kg/m^3]
%         cpa       -  specific heat capacity of air [J/kg/C] 
%         Pa        -  air pressure [mb]
%         U_star    -  friction velocity including gustiness [m/s]
%         T_star    -  temperature scale [C]
%         Q_star    -  humidity scale [kg/kg]
%         dlw       -  downwelling (INTO water) longwave radiation [W/m^2]
%         dsw       -  measured insolation [W/m^2]
%         nsw       -  net shortwave radiation INTO water [W/m^2]
%         delta     -  cool-skin layer thickness [m]
%         g         -  gravitational constant [m/s^2]
%         Rgas      -  gas constant for dry air [J/kg/K]
%         CtoK      -  conversion factor for deg C to K
%        Qsat_coeff - saturation specific humidity coefficient
%
% OUTPUT: delta     -  cool-skin layer thickness [m]
%         Dter      -  cool-skin temperature difference [C]; positive if 
%                      surface is cooler than bulk (presently no warm skin 
%                      permitted by model)
%         Dqer      -  cool-skin specific humidity difference [kg/kg]
%
% USAGE:  [delta,Dter,Dqer] = cool_skin(sal,Tw,rhoa,cpa,Pa, ...
%                                 U_star,T_star,Q_star, ...
%                                 dlw,dsw,nsw,delta,g,Rgas, ...
%                                 CtoK,Qsat_coeff)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 4/9/99: version 1.2 (contributed by AA)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% -> column vectors
sal = sal(:); Tw = Tw(:); rhoa = rhoa(:); cpa = cpa(:); Pa = Pa(:);
U_star = U_star(:); T_star = T_star(:); Q_star = Q_star(:); 
dlw = dlw(:); nsw = nsw(:); delta = delta(:); Rgas = Rgas(:);
CtoK = CtoK(:); Qsat_coeff = Qsat_coeff(:); g = g(:);


size_data = size(Tw);

alpha    = sw_alpha(sal,Tw,0);   % thermal expansion coeff [1/C]
beta_sal = sw_beta(sal,Tw,0);    % saline contraction coeff [1/psu]
cpw      = sw_cp(sal,Tw,0);      % specific heat capacity  [J/kg/C] 
rhow     = sw_dens0(sal,Tw);     % density at atmospheric press [kg/m^3]
viscw    = sw_visc(sal,Tw,0);    % kinematic viscosity of sea-water [m^2/s]
tcondw   = sw_tcond(sal,Tw,0);   % thermal conductivity of sea-water [W/m/K]

% the following are values used for COARE
% alpha    = 2.1e-5*(Tw+3.2).^0.79;% as used for COARE data
% beta_sal = 0.026./sal;           % as used for COARE data
% cpw      = 4000*ones(size(Tw));  % as used for COARE data
% rhow     = 1022*ones(size(Tw));  % as used for COARE data
% viscw    = 1e-6*ones(size(Tw));  % as used for COARE data
% tcondw   = 0.6*ones(size(Tw));   % as used for COARE data

% latent heat of water
Le       = (2.501-0.00237*Tw)*10^6;

% saturation specific humidity; 
Qs       = Qsat_coeff.*qsat(Tw,Pa);

% a big constant
bigc     = (16.*g.*cpw.*(rhow.*viscw).^3)./(tcondw.^2.*rhoa.^2);

% constant for correction of dq; slope of sat. vap. 
wetc     = 0.622.*Le.*Qs./(Rgas.*(Tw+CtoK).^2);

% compute fluxes out of the ocean (i.e., up = positive)
hsb  = - rhoa.*cpa.*U_star.*T_star;
hlb  = - rhoa.*Le.*U_star.*Q_star;

% net longwave (positive up)
nlw = - lwhf(Tw,dlw,dsw);

% total heat flux out of the water surface
qout = nlw + hsb + hlb;

% compute deltaSc = fc*Sns, see sec. 2.4 (p. 1297-1298) in cool-skin paper
% 3 choices; comment out those that are not used! 
deltaSc  = zeros(size_data);
ipos_nsw = find(nsw > 0);
deltaSc(ipos_nsw) = f_c(delta(ipos_nsw),1).*nsw(ipos_nsw); % Paulson and Simpson (1981)
% deltaSc(ipos_nsw) = f_c(delta(ipos_nsw),2).*nsw(ipos_nsw); % COARE approx. to Paulson
% deltaSc(ipos_nsw) = f_c(delta(ipos_nsw),3).*nsw(ipos_nsw); % Hasse (1971)

qcol = qout - deltaSc;

% initialize
alphaQb   = zeros(size_data);
lamda     = zeros(size_data);
Dter      = zeros(size_data);

ipos_qcol = find(qcol > 0);

% eqn. 17 in cool-skin paper
alphaQb(ipos_qcol) = alpha(ipos_qcol).*qcol(ipos_qcol) + ...
                     sal(ipos_qcol).*beta_sal(ipos_qcol) ...
                     .*hlb(ipos_qcol).*cpw(ipos_qcol)./Le(ipos_qcol);

% eqn. 14 in cool-skin paper
lamda(ipos_qcol)   = 6./(1+(bigc(ipos_qcol).*alphaQb(ipos_qcol) ...
                           ./U_star(ipos_qcol).^4).^0.75).^(1/3);

% eqn. 12 in cool-skin paper
delta(ipos_qcol) = lamda(ipos_qcol).*viscw(ipos_qcol) ...
                   ./(sqrt(rhoa(ipos_qcol)./rhow(ipos_qcol)) ...
                   .*U_star(ipos_qcol));

% eqn. 13 in cool-skin paper
Dter(ipos_qcol)    = qcol(ipos_qcol).*delta(ipos_qcol)./tcondw(ipos_qcol);

Dqer          = wetc.*Dter; 




function fc = f_c(delta,option)
% F_C: computes the absorption coefficient fc. 
% fc=F_C(delta,option) computes the absorption coefficient fc. 
%
% INPUT: delta   -  thickness of cool-skin layer [m]
%        option  -  1  use Paulson and Simpson (1981) data for seawater; 
%                      See also p. 1298 of Fairall et al (1996) JGR, 101, 
%                      cool-skin and warm-layer paper. 
%    
%                   2  use approximation to Paulson as given in 
%                      p. 1298 of Fairall et al (1996) JGR, 101, cool-skin
%                      and warm-layer paper. 
%
%                   3  use fc = const. = 0.19, as suggested by Hasse (1971).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 8/5/99: version 1.2 (contributed by AA)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

if option == 1
  % Use Paulson and Simpson data

  % Wavelength bands for the coefficients [um]
  % 1) 0.2-0.6
  % 2) 0.6-0.9
  % 3) 0.9-1.2
  % 4) 1.2-1.5
  % 5) 1.5-1.8
  % 6) 1.8-2.1
  % 7) 2.1-2.4
  % 8) 2.4-2.7
  % 9) 2.7-3.0

  % F_i is the amplitude
  F_i    = [0.237 0.360 0.179 0.087 0.080 0.0246 0.025 0.007 0.0004];
  F_i1   = repmat(F_i,length(delta),1);

  % Gam_i is the absorption length [m]
  Gam_i  = [34.8 2.27 3.15e-2 5.48e-3 8.32e-4 1.26e-4 3.13e-4 7.82e-5 1.44e-5]; 
  Gam_i1 = repmat(Gam_i,length(delta),1);

  delta1 = repmat(delta,1,length(Gam_i));

  % fc is the absorption in the cool-skin layer of thickness delta
  fc = sum(F_i1.*(1-(Gam_i1./delta1).*(1-exp(-delta1./Gam_i1))), 2);

elseif option == 2
  % use COARE approximation to Paulson and Simpson data

  fc = 0.137+11.*delta-(6.6e-5./delta).*(1-exp(-delta/8e-4));

elseif option == 3
  % use Hasse simple approximation

  fc = 0.19;

end
