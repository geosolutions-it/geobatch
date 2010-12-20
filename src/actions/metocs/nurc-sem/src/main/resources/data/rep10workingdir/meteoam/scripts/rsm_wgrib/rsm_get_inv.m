
function inv = rsm_get_inv(gribfile);
% function [inv] = rsm_get_inv(gribfile);
%	Load the invertory of the GRIBFILE into
%	a structure array.
% Manu - (edl@gatech.edu)

% generate a tmp file for local dump
rand('state',sum(100*clock))
file=['/tmp/WGRIB_',num2str(rand(1))];
disp(['Using random file : ',file]);

% find the local path of WGRIB 
wgrib=which ('rsm_extract_record');
wgrib=[wgrib(1:end-20),'wgrib'];

% extract record
str=[wgrib,' ',gribfile,' -s   > ', file];
[s1,s2]=unix(str);
invload = textread(file,'%s','delimiter','\n','whitespace','');

% save inventory into a structured array called inv
for i=1:length(invload);
str=invload{i};
n=strfind(str, ':');
inv.string{i}=str;

irec=1;
ind=1:n(irec)-1;
inv.recnum(i)=str2num( str(ind));

irec=3;
ind=n(irec-1)+1:n(irec)-1;
inv.datestr{i}=( str(ind));
inv.year(i)=str2num(inv.datestr{i}(3:4));
inv.month(i)=str2num(inv.datestr{i}(5:6));
inv.day(i)=str2num(inv.datestr{i}(7:8));
inv.hour(i)=str2num(inv.datestr{i}(9:10));


irec=4;
ind=n(irec-1)+1:n(irec)-1;
inv.varname{i}=( str(ind));
end

% delete tmp file
str=['rm ',file];
unix(str);

