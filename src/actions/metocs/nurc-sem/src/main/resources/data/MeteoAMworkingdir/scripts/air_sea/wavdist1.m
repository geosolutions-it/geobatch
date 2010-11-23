function Ut=wavdist1(Ua,za,Hw)
% WAVDIST1: estimates wave effects on wind speed measured at za.  
% Ut=WAVDIST1(Ua,za,Hw) computes the 'true' wind speed Ut at the
% measurement height za using the wind speed Ua measured at za and
% measured wave height Hw.
% 
% INPUT:   Ua - wind speed  [m/s]
%          za - wind measurement height  [m]
%          Hw - wave height  [m]
%
% OUTPUT:  Ut - 'true' wind speed  [m/s]
  
% WAVDIST1 computes Ut from Ua using the neutral log profile corrected
% for the effects of low-level distortion of the wind profile by surface
% waves following Large, Morzel, and Crawford (1995), J. Phys. Oceanog.,
% 25, 2959-2971.
 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 5/5/97: version 1.0
% 7/28/99: version 1.1
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

k=0.4;
z10=10;
A=log(z10./za)./k;

% eliminate any Ua==0
jj=find(Ua==0);
Ua(jj)=0.01.*ones(size(Ua(jj)));

% compute uncorrected 10m wind speed 
u10=Ua; % initial guess
for n=1:10;
  ustar=sqrt(cdnve(u10).*u10.^2);
  u10=Ua+ustar.*A;
end

% compute corrected 10m wind speed 
Ustar=ustar;U10=u10; % initial guesses
Za=za./Hw;Z10=z10./Hw;
for n=1:10;
  Ustar=sqrt(cdnve(U10).*U10.^2);
  U10=Ua+Ustar.*(log(z10./za)-omegalmc(Z10)+omegalmc(Za))./k;
end

% compute 'true' wind speed at za using U10, Ustar
Ut=U10-Ustar.*A;

