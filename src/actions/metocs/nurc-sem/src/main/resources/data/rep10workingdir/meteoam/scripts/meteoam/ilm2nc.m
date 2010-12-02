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

%function ilm2nc(ddir,fdate,ncfile,base)
function ilm2nc(ddir,ncfile)

  % The location of the rotated pole is encoded here

  pole_lon=10.;
  pole_lat=43; 

  % processing all grib file
  filelist = dir([ddir,'ILM_','*','.grb']);
  %nn=0;
  maxntimes=length({filelist(:).name});
  for ntimes=1:1:maxntimes;
    %nn=nn+1;
    gfile = [ddir,'/',filelist(ntimes).name]

%    deltantimes=1; maxntimes=24; % mystr=[base,'_']; % mystr='12_';
%    nn=0;
%    for ntimes=0:deltantimes:maxntimes;
%      nn=nn+1;
%  
%      if(ntimes<10)
%        hr=['0',num2str(ntimes)];
%      else 
%        hr=num2str(ntimes);
%      end
%      %gfile  = [ddir,'ILM_',fdate,mystr,hr,'.grb'];
%      gfile = dir([ddir,'ILM_','*','*',hr,'.grb']);

    % grab data
    uhdr=read_grib(gfile,{'UGRD'},1,0,0);      % zonal wind
    vhdr=read_grib(gfile,{'VGRD'},1,0,0);      % meridional wind
    ehdr=read_grib(gfile,{'TMP'},1,0,0);       % air temperature
    dhdr=read_grib(gfile,{'DPT'},1,0,0);       % dew temperature
%      whdr=read_grib(gfile,{'TCDC'},1,0,0);      % total cloud cover
%      xhdr=read_grib(gfile,{'PRMSL'},1,0,0);     % mean sea level pressure
%      shdr=read_grib(gfile,{'NSWRS'},1,0,0);     % net shortwave radiation
%      lhdr=read_grib(gfile,{'NLWRS'},1,0,0);     % net longwave radiation
%      bhdr=read_grib(gfile,{'SHTFL'},1,0,0);     % sensible heat flux
%      hhdr=read_grib(gfile,{'LHTFL'},1,0,0);     % latent heat flux
%      rhdr=read_grib(gfile,{'APCP'},1,0,0);      % total precipitation
% grab the dates (and check to make sure they match)
    ut=read_grib(gfile,uhdr(1).record,0,1,0);
    vt=read_grib(gfile,vhdr(1).record,0,1,0); 
    et=read_grib(gfile,ehdr(1).record,0,1,0); 
    dt=read_grib(gfile,dhdr(1).record,0,1,0); 
%      wt=read_grib(gfile,whdr(1).record,0,1,0); 
%      xt=read_grib(gfile,xhdr(1).record,0,1,0); 
%      st=read_grib(gfile,shdr(1).record,0,1,0);
%      lt=read_grib(gfile,lhdr(1).record,0,1,0);
%      bt=read_grib(gfile,bhdr(1).record,0,1,0);
%      ht=read_grib(gfile,hhdr(1).record,0,1,0);
%      rt=read_grib(gfile,rhdr(1).record,0,1,0);

%      yr=2000+ut.pds.year; 
%      mo=ut.pds.month;
%      da=ut.pds.day;       
%      hr=ut.pds.hour+ut.pds.P1+ut.pds.P2;  
%      jdu=julian([yr mo da hr 0 0]);
    du=datenum(2000+ut.pds.year,ut.pds.month,ut.pds.day,ut.pds.hour+ut.pds.P1+ut.pds.P2);
%      
%      yr=2000+vt.pds.year; 
%      mo=vt.pds.month;
%      da=vt.pds.day;       
%      hr=vt.pds.hour+vt.pds.P1+vt.pds.P2; 
%      jdv=julian([yr mo da hr 0 0]);
    dv=datenum(2000+vt.pds.year,vt.pds.month,vt.pds.day,vt.pds.hour+vt.pds.P1+vt.pds.P2);
%  
%      yr=2000+et.pds.year; 
%      mo=et.pds.month;
%      da=et.pds.day;       
%      hr=et.pds.hour+et.pds.P1+et.pds.P2;  
%      jde=julian([yr mo da hr 0 0]);
    de=datenum(2000+et.pds.year,et.pds.month,et.pds.day,et.pds.hour+et.pds.P1+et.pds.P2);

%      yr=2000+dt.pds.year; 
%      mo=dt.pds.month;
%      da=dt.pds.day;       
%      hr=dt.pds.hour+dt.pds.P1+dt.pds.P2;  
%      jdd=julian([yr mo da hr 0 0]);
    dd=datenum(2000+dt.pds.year,dt.pds.month,dt.pds.day,dt.pds.hour+dt.pds.P1+dt.pds.P2);
%  
%      yr=2000+wt.pds.year; 
%      mo=wt.pds.month;
%      da=wt.pds.day;       
%      hr=wt.pds.hour+wt.pds.P1+wt.pds.P2;  
%      jdw=julian([yr mo da hr 0 0]);
%  
%      yr=2000+xt.pds.year; 
%      mo=xt.pds.month;
%      da=xt.pds.day;       
%      hr=xt.pds.hour+xt.pds.P1+xt.pds.P2;  
%      jdx=julian([yr mo da hr 0 0]);
%  
%      yr=2000+st.pds.year; 
%      mo=st.pds.month;
%      da=st.pds.day;       
%      hr=st.pds.hour+st.pds.P1+st.pds.P2;  
%      jds=julian([yr mo da hr 0 0]);
%  
%      yr=2000+lt.pds.year; 
%      mo=lt.pds.month;
%      da=lt.pds.day;       
%      hr=lt.pds.hour+lt.pds.P1+lt.pds.P2;  
%      jdl=julian([yr mo da hr 0 0]);
%  
%      yr=2000+bt.pds.year; 
%      mo=bt.pds.month;
%      da=bt.pds.day;       
%      hr=bt.pds.hour+bt.pds.P1+bt.pds.P2;  
%      jdb=julian([yr mo da hr 0 0]);
%  
%      yr=2000+ht.pds.year; 
%      mo=ht.pds.month;
%      da=ht.pds.day;       
%      hr=ht.pds.hour+ht.pds.P1+ht.pds.P2;  
%      jdh=julian([yr mo da hr 0 0]);
%  
%      yr=2000+rt.pds.year; 
%      mo=rt.pds.month;
%      da=rt.pds.day;       
%      hr=rt.pds.hour+rt.pds.P1+rt.pds.P2;  
%      jdr=julian([yr mo da hr 0 0]);

    if(max(abs(diff([du dv de dd ])))>eps), 
      disp('time mismatch');
      return
    else
      %% we have to transform days in seconds
      seconds=((du-datenum(1980,1,1))*3600*24);
      % base time is in days
      _base_time=du;
    end

    if(ntimes==1); % do only once
	
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
      [alon,alat]=rtll(pole_lon,pole_lat,alon,alat);

      % NOTE now alon and alat are not regular
      % Building a new horizontal regular choordinate system

      %Building lon
      rlon=linspace(
		min(alon(1,:)),		%start
		max(alon(1,:)),		%stop
		im); 			%size

      %RE-Building lat
      rlat=linspace(
		min(alat(:,1)),	%start
		max(alat(:,1)),	%stop
		jm);	%size

      % NetCDF metadata
      f = netcdf(ncfile, 'clobber');
      
      % Preamble.
      f.type = 'COSMO-IT forecast';
      f.title='COSMO-IT forecast';
      f.author = 'Jacopo Chiggiato, chiggiato@nurc.nato.int - Carlo Cancellieri, carlo.cancellieri@geo-solutions.it';
      f.date = datestr(now);

      f('lon') = im;
      f{'lon'}=ncfloat('lon');%'lat',
      f{'lon'}.long_name='Longitude';
      f{'lon'}.units = 'degrees_east';
      f{'lon'}(:)=rlon;
      
      f('lat') = jm;
      f{'lat'}=ncfloat('lat');%,'lon'
      f{'lat'}.long_name='Latitude';
      f{'lat'}.units = 'degrees_north';
      f{'lat'}(:)=rlat;
      
      % BUILD a regular grid to apply interp2 on data
      [rlon,rlat]=meshgrid(rlon,rlat);

      % Meteo Fields
      f('time')=0;   % unlimited dimension
      f{'time'}=ncdouble('time');
      f{'time'}.long_name=('time since initialization');
      %f{'time'}.units=('days since 1968-5-23 00:00:00 UTC');
      f{'time'}.units=('seconds since 1980-1-1 0:0:0');
      f{'time'}.time_origin = datestr(datenum(1980,1,1),"yyyymmddTHHMMSS");
      %f{'time'}.calendar='MJD';

      f{'U10'}=ncfloat('time','lat','lon');
      f{'U10'}.units = 'm/s';
      f{'U10'}.FillValue_=ncfloat(1.e35);
      f{'U10'}.long_name='zonal wind';
      f{'U10'}.coordinates='lat lon';

      f{'V10'}=ncfloat('time','lat','lon');
      f{'V10'}.units = 'm/s';
      f{'V10'}.FillValue_=ncfloat(1.e35);
      f{'V10'}.long_name='meridional wind';
      f{'V10'}.coordinates='lat lon';

      f{'airtemp'}=ncfloat('time','lat','lon');
      f{'airtemp'}.units = 'K';
      %f{'atemp'}.units = 'degC';
      f{'airtemp'}.FillValue_=ncfloat(1.e35);
      f{'airtemp'}.long_name = 'air temperature';
      f{'airtemp'}.coordinates='lat lon';

      f{'relhum'}=ncfloat('time','lat','lon');
%      f{'relhum'}.units = '%';
      f{'relhum'}.units = '%25';
      f{'relhum'}.long_name = 'relative humidity';
      f{'relhum'}.FillValue_=ncfloat(1.e35);
      f{'relhum'}.coordinates='lat lon';

%        f{'cldfrac'}=ncfloat('time','lat','lon');
%        f{'cldfrac'}.units = '1';
%        f{'cldfrac'}.long_name = 'Cloud Fraction at surface';
%        f{'cldfrac'}.FillValue_=ncfloat(1.e35);
%        f{'cldfrac'}.coordinates='lat lon';
%  
%        f{'apress'}=ncfloat('time','lat','lon');
%        f{'apress'}.units = 'millibars';
%        f{'apress'}.long_name = 'Air Pressure at MSL';
%        f{'apress'}.FillValue_=ncfloat(1.e35);
%        f{'apress'}.coordinates='lat lon';
%  
%        f{'swrad'}=ncfloat('time','lat','lon');
%        f{'swrad'}.units = 'W m-2';
%        f{'swrad'}.FillValue_=ncfloat(1.e35);
%        f{'swrad'}.long_name='Net Shortwave Radiation averaged since initialization';
%        f{'swrad'}.coordinates='lat lon';
%  
%        f{'lwrad'}=ncfloat('time','lat','lon');
%        f{'lwrad'}.units = 'W m-2';
%        f{'lwrad'}.FillValue_=ncfloat(1.e35);
%        f{'lwrad'}.long_name='Net Longwave Radiation averaged since initialization';
%        f{'lwrad'}.coordinates='lat lon';
%  
%        f{'sens'}=ncfloat('time','lat','lon');
%        f{'sens'}.units = 'W m-2';
%        f{'sens'}.FillValue_=ncfloat(1.e35);
%        f{'sens'}.long_name='Sensible Heat Flux averaged since initialization';
%        f{'sens'}.coordinates='lat lon';
%  
%        f{'latent'}=ncfloat('time','lat','lon');
%        f{'latent'}.units = 'W m-2';
%        f{'latent'}.FillValue_=ncfloat(1.e35);
%        f{'latent'}.long_name='Latent Heat Flux averaged since initialization';
%        f{'latent'}.coordinates='lat lon';
%  
%        f{'rain'}=ncfloat('time','lat','lon');
%        f{'rain'}.units = 'Kg m-2';
%        f{'rain'}.FillValue_=ncfloat(1.e35);
%        f{'rain'}.long_name='Total Accumulated Precipitation';
%        f{'rain'}.coordinates='lat lon';

      % nodata
      f.nodata=ncdouble(1.e35);
      % fillvalue
      f._FillValue= ncdouble(1.e35);
      %base time attribute
      %"yyyyMMddTHHmmssSSSZ"
      f.base_time=datestr(_base_time,"yyyymmddTHHMMSS");
      % time origin
      f.time_origin = datestr(datenum(1980,1,1),"yyyymmddTHHMMSS");
      % save seconds to (eventually) calculate TAU
      first_time=seconds;
      % setting TAU
      f.tau=ncint(0);

      close(f);

    end % if nn==1

    % reshape the data

    ru=reshape(ut.fltarray,ut.gds.Ni,ut.gds.Nj);
    rv=reshape(vt.fltarray,vt.gds.Ni,vt.gds.Nj);
    
    ec=reshape(et.fltarray-273.15,et.gds.Ni,et.gds.Nj); %CELSIUS (used by qsat)
    e=reshape(et.fltarray,et.gds.Ni,et.gds.Nj); 
    
    dc=reshape(dt.fltarray-273.15,dt.gds.Ni,dt.gds.Nj); %CELSIUS (used by qsat)
    %d=reshape(dt.fltarray,dt.gds.Ni,dt.gds.Nj); 
%      w=reshape(wt.fltarray/104.,wt.gds.Ni,wt.gds.Nj);   % Cloud Fraction range 0 to 1
%      x=reshape(xt.fltarray/100.,xt.gds.Ni,xt.gds.Nj);   % Air pressure Pa => mb
%      s=reshape(st.fltarray,st.gds.Ni,st.gds.Nj);  
%      l=reshape(lt.fltarray,lt.gds.Ni,lt.gds.Nj); 
%      b=reshape(bt.fltarray,bt.gds.Ni,bt.gds.Nj); 
%      h=reshape(ht.fltarray,ht.gds.Ni,ht.gds.Nj); 
%      r=reshape(rt.fltarray,rt.gds.Ni,rt.gds.Nj); 

    % antirotation of wind vectors
    [u,v]=rot_wind(alon,alat,ru',rv');
    clear ru rv;
    www=u+sqrt(-1)*v;

    % writing

    f = netcdf(ncfile, 'write');

    if (ntimes==2) % if there are more than 1 times
      % update TAU
      % tau attribute in hour
      f.tau=ncint(int8((seconds/3600)-(first_time/3600)));
      clear fist_time;
    end
  
  
    f{'U10'}(ntimes,:,:)=interp2(alon,alat,real(www),rlon,rlat,'linear',1.e35);
    f{'V10'}(ntimes,:,:)=interp2(alon,alat,imag(www),rlon,rlat,'linear',1.e35);
    f{'airtemp'}(ntimes,:,:)=interp2(alon,alat,e.',rlon,rlat,'linear',1.e35);
    f{'relhum'}(ntimes,:,:)=interp2(alon,alat,((qsat(dc.')./qsat(ec.'))*100),rlon,rlat,'linear',1.e35);   % rel humidity calc
    
    % this fix a problem of precision writing times into netCDF
    f{'time'}(ntimes)=int64(seconds);%jd-2440000;

%      f{'cldfrac'}(nn,:,:)=w.';
%      f{'apress'}(nn,:,:)=x.';
%      f{'swrad'}(nn,:,:)=s.';
%      f{'lwrad'}(nn,:,:)=l.';
%      f{'sens'}(nn,:,:)=b.';
%      f{'latent'}(nn,:,:)=h.';
%      f{'rain'}(nn,:,:)=r.';

    close(f);

  end

  return
 
end
