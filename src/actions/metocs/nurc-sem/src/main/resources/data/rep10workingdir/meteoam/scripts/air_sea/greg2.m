  function [gtime]=greg2(yd,yr)
% GREG2: converts decimal yearday to standard Gregorian time.
% [gtime]=GREG2(yd,yr) converts decimal yearday to corresponding 
% Gregorian calendar dates.  In this convention, Julian day 2440000 
% begins at 0000 UT May 23 1968. 
%
%  INPUT:   yd - decimal yearday (e.g., 0000 UT Jan 1 is 0.0)
%           yr - year (e.g., 1995)
%
%  OUTPUT:  gtime is a six component Gregorian time vector
%                gtime=[year mo da hr mi sec]
%            
%     Example: [1995 01 01 12 00 00] = greg2(0.5, 1995)

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/10/97: version 1.0 
% 4/7/99: version 1.2 (simplified by AA)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

js = julianmd(yr,01,01,00);
julian = js + yd;
julian=julian+5.e-9;  % kludge to prevent roundoff error on seconds

%      if you want Julian Days to start at noon...
%      h=rem(julian,1)*24+12;
%      i=(h >= 24);
%      julian(i)=julian(i)+1;
%      h(i)=h(i)-24;  Otherwise,....

secs=rem(julian,1)*24*3600;

j = floor(julian) - 1721119;
in = 4*j -1;
y = floor(in/146097);
j = in - 146097*y;
in = floor(j/4);
in = 4*in +3;
j = floor(in/1461);
d = floor(((in - 1461*j) +4)/4);
in = 5*d -3;
m = floor(in/153);
d = floor(((in - 153*m) +5)/5);
y = y*100 +j;
mo=m-9;
yr=y+1;
i=(m<10);
mo(i)=m(i)+3;
yr(i)=y(i);
[hour,min,sec]=s2hms(secs);
gtime=[yr(:) mo(:) d(:) hour(:) min(:) sec(:)];


