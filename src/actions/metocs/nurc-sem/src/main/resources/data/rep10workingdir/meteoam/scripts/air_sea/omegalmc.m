function y=omegalmc(x)
% OMEGALMC: estimates wind log profile correction due to surface waves.
% y=OMEGALMC(x) computes the log profile correction function due to wind
% distortion associated with surface waves. Input is x=za/Hw, where za
% is the measurement height and Hw is the dominant surface wave height. 
% Functional form is simplified (analytic) version of empirical omega
% curves shown in Fig. 9b of Large, Morzel, and Crawford (1995), J. Phys.
% Oceanog., 25, 2959-2971, with the wave-induced roughness length xr=0.15. 
% Assumes x is a vector with all elements greater than zr. 

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

xr=0.15;
ylimit=-6;
y=ylimit.*ones(size(x));
i=find(x<3.2967);
% polynomial fit
a=-2.6;
p1=-0.0199;
p2=0.0144;
p3=0.7660;
p4=0.0654;
x2=x(i).^2;x3=x2.*x(i);
y(i)=a.*log(x(i)./xr)+p1.*x3+p2.*x2+p3.*x(i)+p4;

  

