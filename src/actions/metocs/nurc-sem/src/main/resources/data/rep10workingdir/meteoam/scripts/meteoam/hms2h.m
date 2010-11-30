function [hours]=hms2h(h,m,s);
% HMS2H: converts hours, minutes, and seconds to hours
%
%  Usage:  [hours]=hms2h(h,m,s);   or [hours]=hms2h(hhmmss);
%
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
