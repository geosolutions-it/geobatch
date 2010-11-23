function [hours]=hms2h(h,m,s);
% HMS2H: converts hours, minutes, and seconds to hours.
% [hours]=HMS2H(h,m,s) converts hours, minutes, and seconds to hours
%
%   INPUT:  h  - integer hours
%           m  - minutes
%           s  - secs
%
%   OUTPUT: hours - decimal hours
%
%   Usage:  [hours]=hms2h(h,m,s);   or [hours]=hms2h(hhmmss);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

if nargin== 1,
   hms=h;
   h=floor(hms/10000);
   ms=hms-h*10000;
   m=floor(ms/100);
   s=ms-m*100;
   hours=h+m/60+s/3600;
else
   hours=h+(m+s/60)/60;
end


