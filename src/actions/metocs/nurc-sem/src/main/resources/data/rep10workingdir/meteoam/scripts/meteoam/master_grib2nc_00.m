% Read and convert METEOAM GRIB files into NetCDF
% 00:00 UTC base date forecast
% chiggiato@nurc.nato.int
%

%
% define here the time window. It can be just 1 or multi-day
% 

%start=julian([2010 6 6 0 0 0]);
%stop =julian([2010 6 6 0 0 0]);
 
% 

%for tindex=fix(start):fix(stop);
 tindex=fix(clock);
%tindex=[2010 6 6 0 0 0];
% building yyyymmdd string for dir and file names
     
adate = tindex;
rfile=datestr(adate,24);
rfile(6)=[]; rfile(3)=[];
rfile=[rfile(5) rfile(6) rfile(7) rfile(8) rfile(3) rfile(4) rfile(1) rfile(2)];

% 00:00 UTC forecast

 % convert ELM data (00:00 UTC forecast)
 
ddir=['./',rfile,'/'];

fdate  = rfile; base='00';
ncfile = ['./Archive/','elm_',rfile,base,'.nc'];
 
 elm2nc(ddir,fdate,ncfile,base);
 
% convert ILM data (00:00 UTC forecast)

ddir=['./',rfile,'/'];

fdate  = rfile;
ncfile = ['./Archive/','ilm_',rfile,base,'.nc'];

ilm2nc(ddir,fdate,ncfile,base);

% convert NETTUNO data (00:00 UTC forecast)

ddir=['./',rfile,'/'];

fdate  = rfile;
ncfile = ['./Archive/','nettuno_',rfile,base,'.nc'];

nettuno2nc(ddir,fdate,ncfile,base);

%end
exit
