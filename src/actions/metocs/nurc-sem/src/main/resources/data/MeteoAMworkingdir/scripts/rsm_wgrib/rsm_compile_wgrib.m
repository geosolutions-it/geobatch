
function rsm_compile_wgrib;
% function rsm_compile_wgrib
%	Compile wgrib.c program
% Manu - (edl@gatech.edu)


curr=pwd;
% find the local path of WGRIB 
wgrib=which ('rsm_extract_record'); wgrib=wgrib(1:end-20);
eval (['cd ',wgrib]);
str=['gcc -o wgrib wgrib.c'];
unix(str);
eval (['cd ',curr]);
