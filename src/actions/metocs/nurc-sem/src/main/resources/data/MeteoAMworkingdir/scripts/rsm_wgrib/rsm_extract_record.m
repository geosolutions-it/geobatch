
function [field, s2] = rsm_extract_record(gribfile, record);
% function [FIELD, S] = rsm_extract_record(gribfile, record);
%	Extract field corresponding to record number RECORD 
%     from GRIBFILE into FIELD. S returns the inventory of
%     the selected record. To get a full inventory of the GRIBFILE
%     use  INV = rsm_get_inv(GRIBFILE)
% Manu - (edl@gatech.edu)

% generate a tmp file for local dump
rand('state',sum(100*clock))
file=['/tmp/WGRIB_',num2str(rand(1))];
disp(['Using random file : ',file]);

% find the local path of WGRIB 
wgrib=which ('rsm_extract_record');
wgrib=[wgrib(1:end-20),'wgrib'];

% dump record in tmp file
str=[wgrib,' ',gribfile,' -d ',num2str(record), ' -text -o ', file,'  | grep -v "NCEP reanalysis"'];
[s1,s2]=unix(str);

% read the tmp file and assign record
fid=fopen(file);
dim1=fscanf(fid,'%f', [1]);
dim2=fscanf(fid,'%f', [1]);
field=fscanf(fid,'%f', [dim1 dim2]);
fclose(fid);

% delete tmp file
str=['rm ',file];
unix(str);

