function [U10,delU]=wavedist(Ua,za,Hw)
% WAVEDIST: estimates wind speed distortion due to surface waves.
% [U10,delU]=WAVEDIST(Ua,za,Hw) computes the true 10m wind speed U10
% using the wind speed Ua measured at the height za and measured wave 
% height Hw and the neutral log profile corrected for the effects of
% low-level distortion of the wind profile by surface waves following 
% Large, Morzel, and Crawford (1995), J. Phys. Ocean., 25, 2959-2971.
% 
% INPUT:   Ua - wind speed  [m/s]
%          za - wind measurement height  [m] 
%          Hw - wave height  [m]
%
% OUTPUT:  U10 - true 10m wind speed  [m/s]
%          delU - difference between true and uncorrected  
%                  10m wind speed  [m/s] 

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 8/31/98: version 1.1
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% get constants
as_consts;

tol=.001; % change in u10 to stop iteration

zs=10;    % reference height

Xia=za./Hw;Xis=zs./Hw;

% compute uncorrected 10m wind speed and ustar (as initial guess in iteration)
[cd10,u10]=cdnve(Ua,za);
Ustar=sqrt(cd10).*u10;

% compute corrected 10m wind speed 
U10=u10;
U10o=0;
k=0;
while max(abs(U10-U10o))>tol & k<15,
  U10o=U10;
  k=k+1;
  Ustar=sqrt(cdnve(U10,10).*U10.^2);
  U10=Ua+Ustar.*(log(zs./za)-omegalmc(Xis)+omegalmc(Xia))./kappa;
end

if k==15,
 warning('Iteration may not have converged');
end;

delU=U10-u10;


