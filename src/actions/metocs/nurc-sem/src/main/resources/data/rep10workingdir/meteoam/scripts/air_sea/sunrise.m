function [rhr,rmin,shr,smin]=sunrise(mon,da,yr,lat,long)
% SUNRISE: computes sunrise and sunset times for specified day and location. 
% [rhr,rmin,shr,smin] = SUNRISE(mon,da,yr,lat,long) computes the time 
% of sunrise rhr:rmin and sunset shr:smin to the nearest minute in GMT 
% for a calendar day(s) and a specified (scalar) position.   
% 
% INPUT:  mon - month (e.g., Jan is 1)
%         da - day (e.g., Jan 10th is 10)
%         yr - year (e.g., 1995)
%         lat - latitude [deg]
%         long - longitude (west is positive)  [deg] 
%
% OUTPUT: rhr,rmin  - sunrise in GMT hours and minutes
%          shr,smin  - sunset in GMT hours and minutes.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 8/28/98: version 1.1 (contributed by RP)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% convert calender time to julian yd
j=julianmd(yr,mon,da,0);
j0=julianmd(yr,1,1,0);
yd=j(:)-j0(:);

% compute solar altitude for entire day
dt=1./2880;

% we don't want abs(long)>180...
if long<-180, long=long+360; end;
if long>180,  long=long-360; end;

time=dt.*[0:2879]'+long/360; % have a whole day, beginning at midnight (near enough)
yday=yd(ones(1,2880),:)+time(:,ones(length(yd),1));

if length(yr)>1,
  yr=yr(:,ones(1,2880))';
end;

[z,sorad]=soradna1(yday(:),yr(:),long,lat);

z=reshape(z,2880,length(yd));
sorad=reshape(sorad,2880,length(yd));

[ir,jr]=find(sorad(1:2879,:)==0 & sorad(2:2880,:)>0);
[is,js]=find(sorad(2:2880,:)==0 & sorad(1:2879,:)>0);

srise=zeros(length(yd),1);
sset=any(sorad>0);

srise(jr)=yday(ir+(jr-1)*2880);
sset(js) =yday(is+(js-1)*2880);

rhr=fix(rem(srise,1)*24);
rmin=rem(rem(srise,1)*1440,60);
shr=fix(rem(sset,1)*24);
smin=rem(rem(sset,1)*1440,60);




