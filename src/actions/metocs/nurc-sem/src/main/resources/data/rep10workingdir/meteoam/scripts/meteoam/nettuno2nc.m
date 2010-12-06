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

function nettuno2nc(ddir,ncfile)

  % processing grib file forecast by forecast
  deltantimes=3;
  maxntimes=72;
  nn=0;
  % for each forecast time
  for ntimes=0:deltantimes:maxntimes;
    nn=nn+1;

    if(ntimes<10)
      hr=['0',num2str(ntimes)];
    else
      hr=num2str(ntimes);
    end
    
    gfile  = [ddir,'NETTUNO_',hr,'.grb'];

    % grab data
    uhdr=read_grib(gfile,{'SNOHF'},1,0,0);      % H sig
    vhdr=read_grib(gfile,{'5WAVA'},1,0,0);      % Mean Wave Dir
    ehdr=read_grib(gfile,{'DTRF'},1,0,0);       % Mean Wave Per
%    dhdr=read_grib(gfile,{'MFLUX'},1,0,0);      % Peack Per UNUSED

    % grab the dates (and check to make sure they match)    
    ut=read_grib(gfile,uhdr(1).record,0,1,0); 
    vt=read_grib(gfile,vhdr(1).record,0,1,0); 
    et=read_grib(gfile,ehdr(1).record,0,1,0); 
%    dt=read_grib(gfile,dhdr(1).record,0,1,0); UNUSED

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
%  
%      yr=2000+dt.pds.year;
%      mo=dt.pds.month;
%      da=dt.pds.day;       
%      hr=dt.pds.hour+dt.pds.P1+dt.pds.P2;  
%      jdd=julian([yr mo da hr 0 0]); UNUSED

    
    if(max(abs(diff([du dv de ])))>eps), 
      disp('time mismatch');
      return
    else
      %% we have to transform days in seconds
      seconds=(du-datenum(1980,1,1))*3600*24;
      % base time is in days
      _base_time=du;
    end

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
      %[alon,alat]=meshgrid(rlon,rlat); UNUSED

      % NetCDF metadata

      f = netcdf(ncfile, 'clobber');

      % Preamble.

      f.type ='NETTUNO-METEOAM forecast';
      f.title='NETTUNO-METEOAM forecast';
      f.author = 'Jacopo Chiggiato, chiggiato@nurc.nato.int - Carlo Cancellieri, carlo.cancellieri@geo-solutions.it';
      f.date = datestr(now);

      f('time') =0;   % unlimited dimension
      f('lon') = im;
      f('lat') = jm;

      % Meteo Fields

      f{'time'}=ncdouble('time');
      f{'time'}.long_name=('time since initialization');
      %f{'time'}.units=('days since 1968-5-23 00:00:00 UTC');
      f{'time'}.units=('seconds since 1980-1-1 0:0:0');
      f{'time'}.time_origin = [datestr(datenum(1980,1,1),"yyyymmddTHHMMSS"),'000Z'];
      %f{'time'}.calendar='MJD';

      f{'lat'}=ncfloat('lat');%,'lon'
      f{'lat'}.long_name='Latitude';
      f{'lat'}.units = 'degrees_north';
      f{'lat'}(:)=rlat;%alat;

      f{'lon'}=ncfloat('lon');%'lat',
      f{'lon'}.long_name='Longitude';
      f{'lon'}.units = 'degrees_east';
      f{'lon'}(:)=rlon;%alon;

      f{'hs'}=ncfloat('time','lat','lon');
      f{'hs'}.units = 'm';
      f{'hs'}.FillValue_= ncfloat(1.e35);
      f{'hs'}.missing_value = ncfloat(1.e35);
      f{'hs'}.long_name='sea surface swell wave significant height';
      %f{'Hwave'}.long_name='Significant Wave Heigth';
      f{'hs'}.coordinates='lat lon';

      f{'meanwavdir'}=ncfloat('time','lat','lon');
      f{'meanwavdir'}.units = 'deg';
      f{'meanwavdir'}.FillValue_= ncfloat(1.e35);
      f{'meanwavdir'}.missing_value = ncfloat(1.e35);
      f{'meanwavdir'}.long_name='mean wave direction';
      f{'meanwavdir'}.coordinates='lat lon';

      f{'meanwavperiod'}=ncfloat('time','lat','lon');
      f{'meanwavperiod'}.units = 's';
      f{'meanwavperiod'}.FillValue_= ncfloat(1.e35);
      f{'meanwavperiod'}.missing_value = ncfloat(1.e35);
      f{'meanwavperiod'}.long_name = 'mean wave period';
      f{'meanwavperiod'}.coordinates='lat lon';

%        f{'Tpeack'}=ncfloat('time','lat','lon');
%        f{'Tpeack'}.units = 's';
%        f{'Tpeack'}.long_name = 'Peack Period';
%        f{'Tpeack'}.FillValue_= ncfloat(1.e35);
%        f{'Tpeack'}.coordinates='lat lon';

      %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
      % writing global attributes
      %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
      % nodata
      f.nodata=ncdouble(1.e35);
      % fillvalue
      f._FillValue= ncdouble(1.e35);
      %base time attribute %"yyyyMMddTHHmmssSSSZ"
      f.base_time=[datestr(datenum(1980,1,1)+datenum(0,0,0,0,0,seconds),"yyyymmddTHHMMSS"),'000Z'];
      % time origin
      f.time_origin = [datestr(datenum(1980,1,1),"yyyymmddTHHMMSS"),'000Z'];
      % save seconds to (eventually) calculate TAU
      first_time=seconds;
      % setting TAU
      f.tau=ncint(0);

      close(f);

    end % if nn==1

  % reshape the data
    %size(ut.fltarray)
    %ut.gds.Ni
    %ut.gds.Nj
    %size(ru')
    %nn

    ru=reshape(ut.fltarray,ut.gds.Ni,ut.gds.Nj);
ru(ru==0)=1e35;
    rv=reshape(vt.fltarray,vt.gds.Ni,vt.gds.Nj);
rv(rv==0)=1e35;
    e=reshape(et.fltarray,et.gds.Ni,et.gds.Nj);
e(e==0)=1e35;
%    d=reshape(dt.fltarray,dt.gds.Ni,dt.gds.Nj); UNUSED

  % writing

    f = netcdf(ncfile, 'write');

    if (nn==2) % if there are more than 1 times
      % update TAU
      % tau attribute in hour
      f.tau=ncint(int8((seconds/3600)-(first_time/3600)));
      clear fist_time;
    end

    f{'hs'}(nn,:,:)=ru';
    f{'meanwavdir'}(nn,:,:)=rv';
    f{'meanwavperiod'}(nn,:,:)=e.';
    % this fix a problem of precision writing times into netCDF
    f{'time'}(nn)=int64(seconds);%jd-2440000;

%    f{'Tpeack'}(nn,:,:)=d.';   UNUSED

    close(f);

  end
    
  return
 
end
