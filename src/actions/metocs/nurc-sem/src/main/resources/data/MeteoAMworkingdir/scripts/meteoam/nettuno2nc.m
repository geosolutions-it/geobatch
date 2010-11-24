%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%  
% function nettuno2nc(ddir,fdate,ncfile,base);
%
%  This script reads COSMO-ME gribs and converts to NetCDF
%  chiggiato@nurc.nato.int
%  
%  requires:
%  1) read_grib-1.3.1 package (or higher) 
%  2) rot_wind.m (antirotation for LM vectors)
%  3) rtll.m     (antirotation for LM grid)
%  4) qsat.m & as_const.m (part of air_sea package, 
%     used here to compute relative humidity)
%
%  Input:
%  ddir   = local directory where GRIB files are
%  fdate  = reference date for the forecast (yyyymmdd)
%  ncfile = NetCDF output filename
%  base   = forecast base time (only hours, format 'hh')
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function nettuno2nc(ddir,fdate,ncfile,base)

% processing grib file forecast by forecast

deltantimes=3; maxntimes=72; mystr=[base,'_'];

    nn=0;
for ntimes=0:deltantimes:maxntimes;
    nn=nn+1;
    if(ntimes<10); hr=['0',num2str(ntimes)]; else hr=num2str(ntimes); end;
    gfile  = [ddir,'NETTUNO_',hr,'.grb'];
    
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

f.type ='NETTUNO-METEOAM forecast';
f.title='NETTUNO-METEOAM forecast';
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
f{'Hwave'}.FillValue_= ncfloat(1.e35);
f{'Hwave'}.long_name='Significant Wave Heigth';
f{'Hwave'}.coordinates='lat lon';

f{'Dwave'}=ncfloat('time','jm','im');
f{'Dwave'}.units = 'degrees';
f{'Dwave'}.FillValue_= ncfloat(1.e35);
f{'Dwave'}.long_name='Mean Wave Direction';
f{'Dwave'}.coordinates='lat lon';

f{'Twave'}=ncfloat('time','jm','im');
f{'Twave'}.units = 's';
f{'Twave'}.FillValue_= ncfloat(1.e35);
f{'Twave'}.long_name = 'Mean Wave Period';
f{'Twave'}.coordinates='lat lon';

f{'Tpeack'}=ncfloat('time','jm','im');
f{'Tpeack'}.units = 's';
f{'Tpeack'}.long_name = 'Peack Period';
f{'Tpeack'}.FillValue_= ncfloat(1.e35);
f{'Tpeack'}.coordinates='lat lon';

f{'lon'}(:)=alon;
f{'lat'}(:)=alat;

close(f);

    end


% grab data

  uhdr=read_grib(gfile,{'SNOHF'},1,0,0);      % H sig
  vhdr=read_grib(gfile,{'5WAVA'},1,0,0);      % Mean Wave Dir
  ehdr=read_grib(gfile,{'DTRF'},1,0,0);       % Mean Wave Per
  dhdr=read_grib(gfile,{'MFLUX'},1,0,0);      % Peack Per
  
  
  ut=read_grib(gfile,uhdr(1).record,0,1,0); 
  vt=read_grib(gfile,vhdr(1).record,0,1,0); 
  et=read_grib(gfile,ehdr(1).record,0,1,0); 
  dt=read_grib(gfile,dhdr(1).record,0,1,0); 
  
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

  yr=2000+dt.pds.year; 
  mo=dt.pds.month;
  da=dt.pds.day;       
  hr=dt.pds.hour+dt.pds.P1+dt.pds.P2;  
  jdd=julian([yr mo da hr 0 0]);

  
  if(max(abs(diff([jdu jdv jde jdd])))>eps), 
      disp('time mismatch');return
  else
     jd=jdu;
  end

% reshape the data
  %size(ut.fltarray)
  %ut.gds.Ni
  %ut.gds.Nj
  %size(ru')
  %nn

  ru=reshape(ut.fltarray,ut.gds.Ni,ut.gds.Nj);
  rv=reshape(vt.fltarray,vt.gds.Ni,vt.gds.Nj);
  e=reshape(et.fltarray,et.gds.Ni,et.gds.Nj);
  d=reshape(dt.fltarray,dt.gds.Ni,dt.gds.Nj);

% writing
ru
  f = netcdf(ncfile, 'write');
  f{'Hwave'}(1,:,:)=ru';
  f{'Dwave'}(nn,:,:)=rv';
  f{'Twave'}(nn,:,:)=e.';
  f{'Tpeack'}(nn,:,:)=d.';   
  f{'time'}(nn)=jd-2440000;
  close(f);

end
  
return
 
end
