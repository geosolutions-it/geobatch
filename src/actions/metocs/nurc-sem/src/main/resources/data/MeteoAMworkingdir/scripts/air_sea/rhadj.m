function rha = rhadj(rh,rhmax)
% RHADJ: rescales RH to have a maximum of 100%.
% RHADJ(rh,rhmax) rescales RH so that the maximum values do not
% exceed 100%. Assumes values between 93% and rhmax should be
% rescaled to 93 - 100% (the calibration curves of RH sensors usually
% become nonlinear above ~ 90%, and may peak above 100% in this range 
% above ~ 90% where their calibration becomes unstable.) 

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 4/10/98: version 1.0
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

rhl=93;

rhn=rh;
a=(100-rhl)./(rhmax-rhl);
drh=rh-rhl;
j=find(drh>0);
rhn(j)=rhl+a.*drh(j);

rha=rhn;