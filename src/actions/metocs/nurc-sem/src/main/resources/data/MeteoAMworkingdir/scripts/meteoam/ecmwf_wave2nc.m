%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%  
% ecmwf2nc
%
%  This script reads ECMWF forecast gribs and converts to NetCDF
%  chiggiato@nurc.nato.int
%  
%  requires:
%  1) read_grib-1.3.1 package (or higher) 
%  
%
%  Input:
%  ddir   = local directory where GRIB files are
%  fdate  = reference date for the forecast (yyyymmdd)
%  ncfile = NetCDF output filename
%  base   = forecast base time (only hours, format 'hh')
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%ddir='20100613/';
%fdate='0613'; $
%ncfile='out_wave.nc';
%base='00'; % forecast emission (either 00 or 12 for 00:00 UTC or 12:00 UTC)

function ecmwf_wave2nc(ddir,fdate,ncfile,base)

elenco=dir([ddir,'/J4M',fdate,base,'*']);

% processing grib file forecast by forecast

deltantimes=3; maxntimes=72; 

    nn=0;
for ntimes=3:deltantimes:maxntimes;
    nn=nn+1;
    
    gfile  = [ddir '/' elenco(nn+1).name];
    
    if(nn==1); % do only once
        
% acquire grid 

tt=read_grib(gfile,1,0,1,0);
rlon_min=tt.gds.Lo1;
rlon_max=tt.gds.Lo2;
rlat_min=tt.gds.La1;
rlat_max=tt.gds.La2;
im=tt.gds.Ni;
jm=tt.gds.Nj;

rlon=linspace(rlon_min,rlon_max,im);
rlat=linspace(rlat_min,rlat_max,jm);
[alon,alat]=meshgrid(rlon,rlat);

% NetCDF metadata

f = netcdf(ncfile, 'clobber');

% Preamble.

f.type ='ECMWF forecast';
f.title='ECMWF forecast';
f.author = 'Jacopo Chiggiato, chiggiato@nurc.nato.int';
f.date = datestr(now);

f('time') =0;   % unlimited dimension
f('im') = im;
f('jm') = jm;

% Meteo Fields

f{'time'}=ncdouble('time');
f{'time'}.long_name=('time since initialization');
f{'time'}.units=('days since 1968-5-23 00:00:00 UTC');
f{'time'}.calendar='MJD';

f{'lat'}=ncfloat('jm','im');
f{'lat'}.long_name='Latitude';
f{'lat'}.units = 'degrees_north';

f{'lon'}=ncfloat('jm','im');
f{'lon'}.long_name='Longitude';
f{'lon'}.units = 'degrees_east';

f{'Hwave'}=ncfloat('time','jm','im');
f{'Hwave'}.units = 'm';
f{'Hwave'}.FillValue_=ncfloat(1.e35);
f{'Hwave'}.long_name='Significant Wave Heigth';
f{'Hwave'}.coordinates='lat lon';

f{'Dwave'}=ncfloat('time','jm','im');
f{'Dwave'}.units = 'degrees';
f{'Dwave'}.FillValue_=ncfloat(1.e35);
f{'Dwave'}.long_name='Mean Wave Direction';
f{'Dwave'}.coordinates='lat lon';

f{'Twave'}=ncfloat('time','jm','im');
f{'Twave'}.units = 's';
f{'Twave'}.FillValue_=ncfloat(1.e35);
f{'Twave'}.long_name = 'Mean Wave Period';
f{'Twave'}.coordinates='lat lon';

f{'lon'}(:)=alon;
f{'lat'}(:)=alat;

close(f);

    end


% grab data

  uhdr=read_grib(gfile,{'SNOHF'},1,0,0);      % H sig
  vhdr=read_grib(gfile,{'5WAVA'},1,0,0);      % Mean Wave Dir
  ehdr=read_grib(gfile,{'DTRF'},1,0,0);       % Mean Wave Per
  
  
  ut=read_grib(gfile,uhdr(1).record,0,1,0); 
  vt=read_grib(gfile,vhdr(1).record,0,1,0); 
  et=read_grib(gfile,ehdr(1).record,0,1,0); 
  
% grab the dates (and check to make sure they match)

  yr=2000+ut.pds.year; 
  mo=ut.pds.month;
  da=ut.pds.day;       
  hr=ut.pds.hour+ut.pds.P1+ut.pds.P2;  
  jdu=julian([yr mo da hr 0 0]);
  
  yr=2000+vt.pds.year; 
  mo=vt.pds.month;
  da=vt.pds.day;       
  hr=vt.pds.hour+vt.pds.P1+vt.pds.P2; 
  jdv=julian([yr mo da hr 0 0]);

  yr=2000+et.pds.year; 
  mo=et.pds.month;
  da=et.pds.day;       
  hr=et.pds.hour+et.pds.P1+et.pds.P2;  
  jde=julian([yr mo da hr 0 0]);

  
  if(max(abs(diff([jdu jdv jde ])))>eps), 
      disp('time mismatch');return
  else
     jd=jdu;
  end

% reshape the data

  ru=reshape(ut.fltarray,ut.gds.Ni,ut.gds.Nj);
  rv=reshape(vt.fltarray,vt.gds.Ni,vt.gds.Nj);
  e=reshape(et.fltarray,et.gds.Ni,et.gds.Nj);

  ru(ru>10000)=NaN;
  rv(rv>10000)=NaN;
  e(e>10000)=NaN;
  
% writing

  f = netcdf(ncfile, 'write');
  f{'Hwave'}(nn,:,:)=ru'; 
  f{'Dwave'}(nn,:,:)=rv';
  f{'Twave'}(nn,:,:)=e.';
  f{'time'}(nn)=jd-2440000;
  close(f);

end
  
return

% end
