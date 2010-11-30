function [hr,min,sec]=s2hms(secs)
% S2HMS: converts seconds to interger hour, minute, and seconds.
% [hr,min,sec]=S2HMS(secs) converts seconds to integer hour, minute,
% and seconds.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/11/96: version 1.0
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

sec=round(secs);
hr=floor(sec./3600);
min=floor(rem(sec,3600)./60);
sec=round(rem(sec,60));

