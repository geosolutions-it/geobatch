%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%  
% function ilm2nc(ddir,fdate,ncfile);
%
%  This script reads ILM gribs and converts to NetCDF
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
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

function ilm2nc(ddir,fdate,ncfile,base)

% The location of the rotated pole is encoded here

pole_lon=10.;
pole_lat=43; 

% processing grib file forecast by forecast


deltantimes=1; maxntimes=24; mystr=[base,'_']; % mystr='12_';

    nn=0;
for ntimes=0:deltantimes:maxntimes;
    nn=nn+1;
    if(ntimes<10); hr=['0',num2str(ntimes)]; else hr=num2str(ntimes); end;
    gfile  = [ddir,'ILM_',fdate,mystr,hr,'.grb'];
    
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
[rlon,rlat]=meshgrid(rlon,rlat);
[alon,alat]=rtll(pole_lon,pole_lat,rlon,rlat);

% NetCDF metadata

f = netcdf(ncfile, 'clobber');

% Preamble.

f.type = 'COSMO-IT forecast';
f.title='COSMO-IT forecast';
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

f{'U10'}=ncfloat('time','jm','im');
f{'U10'}.units = 'm/s';
f{'U10'}.FillValue_=ncfloat(1.e35);
f{'U10'}.long_name='Zonal Wind at 10m';
f{'U10'}.coordinates='lat lon';

f{'V10'}=ncfloat('time','jm','im');
f{'V10'}.units = 'm/s';
f{'V10'}.FillValue_=ncfloat(1.e35);
f{'V10'}.long_name='Meridional Wind at 10m';
f{'V10'}.coordinates='lat lon';

f{'atemp'}=ncfloat('time','jm','im');
f{'atemp'}.units = 'degC';
f{'atemp'}.FillValue_=ncfloat(1.e35);
f{'atemp'}.long_name = 'Air Temperature at 2 m';
f{'atemp'}.coordinates='lat lon';

f{'relhum'}=ncfloat('time','jm','im');
f{'relhum'}.units = '%';
f{'relhum'}.long_name = 'Relative Humidity at 2 m';
f{'relhum'}.FillValue_=ncfloat(1.e35);
f{'relhum'}.coordinates='lat lon';

f{'cldfrac'}=ncfloat('time','jm','im');
f{'cldfrac'}.units = '1';
f{'cldfrac'}.long_name = 'Cloud Fraction at surface';
f{'cldfrac'}.FillValue_=ncfloat(1.e35);
f{'cldfrac'}.coordinates='lat lon';

f{'apress'}=ncfloat('time','jm','im');
f{'apress'}.units = 'millibars';
f{'apress'}.long_name = 'Air Pressure at MSL';
f{'apress'}.FillValue_=ncfloat(1.e35);
f{'apress'}.coordinates='lat lon';

f{'swrad'}=ncfloat('time','jm','im');
f{'swrad'}.units = 'W m-2';
f{'swrad'}.FillValue_=ncfloat(1.e35);
f{'swrad'}.long_name='Net Shortwave Radiation averaged since initialization';
f{'swrad'}.coordinates='lat lon';

f{'lwrad'}=ncfloat('time','jm','im');
f{'lwrad'}.units = 'W m-2';
f{'lwrad'}.FillValue_=ncfloat(1.e35);
f{'lwrad'}.long_name='Net Longwave Radiation averaged since initialization';
f{'lwrad'}.coordinates='lat lon';

f{'sens'}=ncfloat('time','jm','im');
f{'sens'}.units = 'W m-2';
f{'sens'}.FillValue_=ncfloat(1.e35);
f{'sens'}.long_name='Sensible Heat Flux averaged since initialization';
f{'sens'}.coordinates='lat lon';

f{'latent'}=ncfloat('time','jm','im');
f{'latent'}.units = 'W m-2';
f{'latent'}.FillValue_=ncfloat(1.e35);
f{'latent'}.long_name='Latent Heat Flux averaged since initialization';
f{'latent'}.coordinates='lat lon';

f{'rain'}=ncfloat('time','jm','im');
f{'rain'}.units = 'Kg m-2';
f{'rain'}.FillValue_=ncfloat(1.e35);
f{'rain'}.long_name='Total Accumulated Precipitation';
f{'rain'}.coordinates='lat lon';

f{'lon'}(:)=alon;
f{'lat'}(:)=alat;

close(f);

    end


% grab data

  uhdr=read_grib(gfile,{'UGRD'},1,0,0);      % zonal wind
  vhdr=read_grib(gfile,{'VGRD'},1,0,0);      % meridional wind
  ehdr=read_grib(gfile,{'TMP'},1,0,0);       % air temperature
  dhdr=read_grib(gfile,{'DPT'},1,0,0);       % dew temperature
  whdr=read_grib(gfile,{'TCDC'},1,0,0);      % total cloud cover
  xhdr=read_grib(gfile,{'PRMSL'},1,0,0);     % mean sea level pressure
  shdr=read_grib(gfile,{'NSWRS'},1,0,0);     % net shortwave radiation
  lhdr=read_grib(gfile,{'NLWRS'},1,0,0);     % net longwave radiation
  bhdr=read_grib(gfile,{'SHTFL'},1,0,0);     % sensible heat flux
  hhdr=read_grib(gfile,{'LHTFL'},1,0,0);     % latent heat flux
  rhdr=read_grib(gfile,{'APCP'},1,0,0);      % total precipitation
  
  ut=read_grib(gfile,uhdr(1).record,0,1,0); 
  vt=read_grib(gfile,vhdr(1).record,0,1,0); 
  et=read_grib(gfile,ehdr(1).record,0,1,0); 
  dt=read_grib(gfile,dhdr(1).record,0,1,0); 
  wt=read_grib(gfile,whdr(1).record,0,1,0); 
  xt=read_grib(gfile,xhdr(1).record,0,1,0); 
  st=read_grib(gfile,shdr(1).record,0,1,0);
  lt=read_grib(gfile,lhdr(1).record,0,1,0);
  bt=read_grib(gfile,bhdr(1).record,0,1,0);
  ht=read_grib(gfile,hhdr(1).record,0,1,0);
  rt=read_grib(gfile,rhdr(1).record,0,1,0);

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

  yr=2000+wt.pds.year; 
  mo=wt.pds.month;
  da=wt.pds.day;       
  hr=wt.pds.hour+wt.pds.P1+wt.pds.P2;  
  jdw=julian([yr mo da hr 0 0]);

  yr=2000+xt.pds.year; 
  mo=xt.pds.month;
  da=xt.pds.day;       
  hr=xt.pds.hour+xt.pds.P1+xt.pds.P2;  
  jdx=julian([yr mo da hr 0 0]);

  yr=2000+st.pds.year; 
  mo=st.pds.month;
  da=st.pds.day;       
  hr=st.pds.hour+st.pds.P1+st.pds.P2;  
  jds=julian([yr mo da hr 0 0]);

  yr=2000+lt.pds.year; 
  mo=lt.pds.month;
  da=lt.pds.day;       
  hr=lt.pds.hour+lt.pds.P1+lt.pds.P2;  
  jdl=julian([yr mo da hr 0 0]);
  
  yr=2000+bt.pds.year; 
  mo=bt.pds.month;
  da=bt.pds.day;       
  hr=bt.pds.hour+bt.pds.P1+bt.pds.P2;  
  jdb=julian([yr mo da hr 0 0]);
  
  yr=2000+ht.pds.year; 
  mo=ht.pds.month;
  da=ht.pds.day;       
  hr=ht.pds.hour+ht.pds.P1+ht.pds.P2;  
  jdh=julian([yr mo da hr 0 0]);
  
  yr=2000+rt.pds.year; 
  mo=rt.pds.month;
  da=rt.pds.day;       
  hr=rt.pds.hour+rt.pds.P1+rt.pds.P2;  
  jdr=julian([yr mo da hr 0 0]);
  
  if(max(abs(diff([jdu jdv jde jdd jdw jdx jds jdl jdb jdh jdr])))>eps), 
      disp('time mismatch');return
  else
     jd=jdu;
  end

% reshape the data

  ru=reshape(ut.fltarray,ut.gds.Ni,ut.gds.Nj);
  rv=reshape(vt.fltarray,vt.gds.Ni,vt.gds.Nj);
  e=reshape(et.fltarray-273.15,et.gds.Ni,et.gds.Nj);
  d=reshape(dt.fltarray-273.15,dt.gds.Ni,dt.gds.Nj);
  w=reshape(wt.fltarray/104.,wt.gds.Ni,wt.gds.Nj);   % Cloud Fraction range 0 to 1
  x=reshape(xt.fltarray/100.,xt.gds.Ni,xt.gds.Nj);   % Air pressure Pa => mb
  s=reshape(st.fltarray,st.gds.Ni,st.gds.Nj);  
  l=reshape(lt.fltarray,lt.gds.Ni,lt.gds.Nj); 
  b=reshape(bt.fltarray,bt.gds.Ni,bt.gds.Nj); 
  h=reshape(ht.fltarray,ht.gds.Ni,ht.gds.Nj); 
  r=reshape(rt.fltarray,rt.gds.Ni,rt.gds.Nj); 

% antirotation of wind vectors

  [u,v]=rot_wind(alon,alat,ru',rv');
  clear ru rv;
  www=u+sqrt(-1)*v;

% writing

  f = netcdf(ncfile, 'write');
  f{'U10'}(nn,:,:)=real(www);
  f{'V10'}(nn,:,:)=imag(www);
  f{'atemp'}(nn,:,:)=e.';
  f{'relhum'}(nn,:,:)=(qsat(d.')./qsat(e.'))*100;   % rel humidity calc
  f{'cldfrac'}(nn,:,:)=w.';
  f{'apress'}(nn,:,:)=x.';
  f{'swrad'}(nn,:,:)=s.';
  f{'lwrad'}(nn,:,:)=l.';
  f{'sens'}(nn,:,:)=b.';
  f{'latent'}(nn,:,:)=h.';
  f{'rain'}(nn,:,:)=r.';                     
  f{'time'}(nn)=jd-2440000;
  close(f);

end
  
return
 
% end
