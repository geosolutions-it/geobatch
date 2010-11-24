function [hour,min,secs]=h2hms(hours);
% H2HMS: converts hours to hours, minutes, and seconds
%
%  Usage:  [hour,min,sec]=h2hms(hours);
%
%  Rich Signell rsignell@usgs.gov
%
hour=floor(hours);
%
mins=rem(hours,1)*60;
min=floor(mins);
%
secs=round(rem(mins,1)*60);

