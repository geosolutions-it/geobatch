%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%  
% ecmwf2nc
%
%  This script reads ECMWF forecast gribs and converts to NetCDF
%  chiggiato@nurc.nato.int
%  
%  requires:
%  1) read_grib-1.3.1 package (or higher) 
%  2) qsat.m & as_const.m (part of air_sea package, 
%     used here to compute relative humidity)
%
%  Input:
%  ddir   = local directory where GRIB files are
%  fdate  = reference date for the forecast (yyyymmdd)
%  ncfile = NetCDF output filename
%  base   = forecast base time (only hours, format 'hh')
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%ddir='20100613/';
%fdate='0613';
%ncfile='out_meteo.nc';
%base='00'; % forecast emission (either 00 or 12 for 00:00 UTC or 12:00 UTC)

%function ecmwf_meteo2nc(ddir,fdate,ncfile,base)
function ecmwf_meteo2nc(ddir,ncfile)

  % we get all the times for all the dates
  %elenco=dir([ddir,'/J4D',fdate,base,'*']);
  elenco=dir([ddir,'/J4D','*','*','*']);

  % processing grib file forecast by forecast


  deltantimes=3; maxntimes=72; 

  nn=0;
  for ntimes=3:deltantimes:maxntimes;
    nn=nn+1;

    gfile  = [ddir '/' elenco(nn+1).name];

    % grab data

    uhdr=read_grib(gfile,{'CFNLF'},1,0,0);      % zonal wind
    vhdr=read_grib(gfile,{'VBDSF'},1,0,0);      % meridional wind
    ehdr=read_grib(gfile,{'VDDSF'},1,0,0);      % air temperature
    dhdr=read_grib(gfile,{'NBDSF'},1,0,0);      % dew temperature
%    whdr=read_grib(gfile,{'CFNSF'},1,0,0);      % total cloud cover
%    xhdr=read_grib(gfile,{'COVTZ'},1,0,0);      % mean sea level pressure
%    shdr=read_grib(gfile,{'NLAT'},1,0,0);       % net shortwave radiation
%    rhdr=read_grib(gfile,{'PEVAP'},1,0,0);      % total acummulated rain

    ut=read_grib(gfile,uhdr(1).record,0,1,0); 
    vt=read_grib(gfile,vhdr(1).record,0,1,0); 
    et=read_grib(gfile,ehdr(1).record,0,1,0); 
    dt=read_grib(gfile,dhdr(1).record,0,1,0); 
%    wt=read_grib(gfile,whdr(1).record,0,1,0); 
%    xt=read_grib(gfile,xhdr(1).record,0,1,0); 
%    st=read_grib(gfile,shdr(1).record,0,1,0);
%    rt=read_grib(gfile,rhdr(1).record,0,1,0);

    % grab the dates (and check to make sure they match)

    %yr=2000+ut.pds.year;
    %mo=ut.pds.month;
    %da=ut.pds.day;
    %hr=ut.pds.hour+ut.pds.P1+ut.pds.P2;
    %jdu=julian([yr mo da hr 0 0]);
    du=datenum(2000+ut.pds.year,ut.pds.month,ut.pds.day,ut.pds.hour+ut.pds.P1+ut.pds.P2);

    %yr=2000+vt.pds.year; 
    %mo=vt.pds.month;
    %da=vt.pds.day;       
    %hr=vt.pds.hour+vt.pds.P1+vt.pds.P2; 
    %jdv=julian([yr mo da hr 0 0]);
    dv=datenum(2000+vt.pds.year,vt.pds.month,vt.pds.day,vt.pds.hour+vt.pds.P1+vt.pds.P2);

    %yr=2000+et.pds.year; 
    %mo=et.pds.month;
    %da=et.pds.day;
    %hr=et.pds.hour+et.pds.P1+et.pds.P2;  
    %jde=julian([yr mo da hr 0 0]);
    de=datenum(2000+et.pds.year,et.pds.month,et.pds.day,et.pds.hour+et.pds.P1+et.pds.P2);

    %yr=2000+dt.pds.year; 
    %mo=dt.pds.month;
    %da=dt.pds.day;       
    %hr=dt.pds.hour+dt.pds.P1+dt.pds.P2;  
    %jdd=julian([yr mo da hr 0 0]);
    dd=datenum(2000+dt.pds.year,dt.pds.month,dt.pds.day,dt.pds.hour+dt.pds.P1+dt.pds.P2);

    %yr=2000+wt.pds.year; 
    %mo=wt.pds.month;
    %da=wt.pds.day;       
    %hr=wt.pds.hour+wt.pds.P1+wt.pds.P2;  
    %jdw=julian([yr mo da hr 0 0]);
    %dw=datenum(2000+wt.pds.year,wt.pds.month,wt.pds.day,wt.pds.hour+wt.pds.P1+wt.pds.P2);

    %yr=2000+xt.pds.year;
    %mo=xt.pds.month;
    %da=xt.pds.day;       
    %hr=xt.pds.hour+xt.pds.P1+xt.pds.P2;  
    %jdx=julian([yr mo da hr 0 0]);
    %dx=datenum(2000+xt.pds.year,xt.pds.month,xt.pds.day,xt.pds.hour+xt.pds.P1+xt.pds.P2);

    %yr=2000+st.pds.year; 
    %mo=st.pds.month;
    %da=st.pds.day;       
    %hr=st.pds.hour+st.pds.P1+st.pds.P2;  
    %jds=julian([yr mo da hr 0 0]);
    %ds=datenum(2000+st.pds.year,st.pds.month,st.pds.day,st.pds.hour+st.pds.P1+st.pds.P2);

    %yr=2000+rt.pds.year; 
    %mo=rt.pds.month;
    %da=rt.pds.day;       
    %hr=rt.pds.hour+rt.pds.P1+rt.pds.P2;  
    %jdr=julian([yr mo da hr 0 0]);
    %dr=datenum(2000+rt.pds.year,rt.pds.month,rt.pds.day,rt.pds.hour+rt.pds.P1+rt.pds.P2);


    if(max(abs(diff([du dv de dd ])))>eps)
      disp('time mismatch');
      return
    else
      %% we have to transform days in seconds
      seconds=(du-datenum(1980,1,1))*3600*24;
      % base time is in days
      _base_time=du;
    end


    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    % writing global attributes
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

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

      %[alon,alat]=meshgrid(rlon,rlat); % UNUSED

      %%[alon,alat]=rtll(pole_lon,pole_lat,rlon,rlat); %ALREADY COMMENTED

      % NetCDF metadata

      f = netcdf(ncfile, 'clobber');

      % Preamble.

      f.type = 'ECMWF METEO forecast';
      f.title='ECMWF METEO forecast';
      f.author = 'Jacopo Chiggiato, chiggiato@nurc.nato.int - Carlo Cancellieri, carlo.cancellieri@geo-solutions.it';
      f.date = datestr(now);

      f('time') = 0;   % unlimited dimension
      f{'time'}=ncdouble('time');
      f{'time'}.long_name=('time since initialization');
      f{'time'}.units=('seconds since 1980-1-1 0:0:0');
      %f{'time'}.units=('days since 1968-5-23 00:00:00 UTC');
      %f{'time'}.calendar='MJD';
      f{'time'}.time_origin = datestr(datenum(1980,1,1),"yyyymmddTHHMMSS");
      
      f('lon') = im;
      f{'lon'}=ncfloat('lon');%'lat',
      f{'lon'}.long_name='Longitude';
      f{'lon'}.units = 'degrees_east';
      f{'lon'}(:)=rlon;%alon;
      
      f('lat') = jm;
      f{'lat'}=ncfloat('lat');%,'lon'
      f{'lat'}.long_name='Latitude';
      f{'lat'}.units = 'degrees_north';
      f{'lat'}(:)=rlat;%alat;

      %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
      % Meteo Fields
      %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% U
      f{'U10'}=ncfloat('time','lat','lon');
      f{'U10'}.units = 'm/s';
      f{'U10'}.FillValue_=ncfloat(1.e35);
      f{'U10'}.long_name='zonal wind';
      f{'U10'}.coordinates='lat lon';
% V
      f{'V10'}=ncfloat('time','lat','lon');
      f{'V10'}.units = 'm/s';
      f{'V10'}.FillValue_=ncfloat(1.e35);
      f{'V10'}.long_name='meridional wind';
      f{'V10'}.coordinates='lat lon';
% TEMP
      %f{'atemp'}=ncfloat('time','lat','lon');
      f{'airtemp'}=ncfloat('time','lat','lon');
      f{'airtemp'}.units = 'K';
      %f{'atemp'}.units = 'degC';
      f{'airtemp'}.FillValue_=ncfloat(1.e35);
      %f{'atemp'}.long_name = 'Air Temperature at 2 m';
      f{'airtemp'}.long_name = 'air temperature';
      f{'airtemp'}.coordinates='lat lon';
% HUM
      f{'relhum'}=ncfloat('time','lat','lon');
%TODO (how to convert from % to %25???)
      f{'relhum'}.units = '%25';
%---> %f{'relhum'}.units = '%';

      f{'relhum'}.long_name = 'relative humidity';%Relative Humidity at 2 m';
      f{'relhum'}.FillValue_=ncfloat(1.e35);
      f{'relhum'}.coordinates='lat lon';

      %<metoc defaultUom="urn:ogc:def:uom:UCUM::Pa" type="meteo">
      %<name>mean sea level pressure</name>
      %<brief>meansealevpress</brief>
      %</metoc>
      %f{'apress'}=ncfloat('time','lat','lon');
      %f{'apress'}.units = 'millibars';
      %f{'apress'}.long_name = 'Air Pressure at MSL';
      %f{'apress'}.FillValue_=ncfloat(1.e35);
      %f{'apress'}.coordinates='lat lon';

      %f{'cldfrac'}=ncfloat('time','lat','lon');
      %f{'cldfrac'}.units = '1';
      %f{'cldfrac'}.long_name = 'Cloud Fraction at surface';
      %f{'cldfrac'}.FillValue_=ncfloat(1.e35);
      %f{'cldfrac'}.coordinates='lat lon';

      %f{'swrad'}=ncfloat('time','lat','lon');
      %f{'swrad'}.units = 'W m-2 s';
      %f{'swrad'}.FillValue_=ncfloat(1.e35);
      %f{'swrad'}.long_name='Net Shortwave Radiation accumulated since initialization';
      %f{'swrad'}.coordinates='lat lon';

      %f{'rain'}=ncfloat('time','lat','lon');
      %f{'rain'}.units = 'm';
      %f{'rain'}.FillValue_=ncfloat(1.e35);
      %f{'rain'}.long_name='Total Precipitation accumulated since initialization';
      %f{'rain'}.coordinates='lat lon';

      %base time attribute
      %"yyyyMMddTHHmmssSSSZ"
      f.base_time=datestr(_base_time,"yyyymmddTHHMMSS");

      % nodata
      f.nodata=ncdouble(1.e35);
      % fillvalue
      f._FillValue= ncdouble(1.e35);
      %base time attribute
      %"yyyyMMddTHHmmssSSSZ"
      f.base_time=datestr(_base_time,"yyyymmddTHHMMSS");
      % time origin
      f.time_origin = datestr(datenum(1980,1,1),"yyyymmddTHHMMSS");
      % save seconds to calculate TAU
      first_time=seconds;

      f.tau=ncint(0);

      close(f);

    end


    % reshape the data

    ru=reshape(ut.fltarray,ut.gds.Ni,ut.gds.Nj);
    rv=reshape(vt.fltarray,vt.gds.Ni,vt.gds.Nj);
    % qsat works with Celsius temperature
    dc=reshape(dt.fltarray-273.15,dt.gds.Ni,dt.gds.Nj); %from Kelvin to Celsius
    ec=reshape(et.fltarray-273.15,et.gds.Ni,et.gds.Nj); %from Kelvin to Celsius

    e=reshape(et.fltarray,et.gds.Ni,et.gds.Nj); % we need temp in Kelvin
    %d=reshape(dt.fltarray,dt.gds.Ni,dt.gds.Nj); % we do not need it

% UNUSED
%    w=reshape(wt.fltarray,wt.gds.Ni,wt.gds.Nj);       % Cloud Fraction range 0 to 1
%    x=reshape(xt.fltarray/100,xt.gds.Ni,xt.gds.Nj);   % Air pressure Pa => mb
%    s=reshape(st.fltarray,st.gds.Ni,st.gds.Nj);  
%    r=reshape(rt.fltarray,rt.gds.Ni,rt.gds.Nj); 

    % writing

    f = netcdf(ncfile, 'write');

    if (nn==2) % if there are more than 1 times
      % update TAU
      % tau attribute in hour
      f.tau=ncint(int8((seconds/3600)-(first_time/3600)));
      clear fist_time;
    end

    f{'time'}(nn)=int64(seconds);%jd-2440000;
    f{'U10'}(nn,:,:)=ru';
    f{'V10'}(nn,:,:)=rv';
    f{'airtemp'}(nn,:,:)=e.';
    f{'relhum'}(nn,:,:)=(qsat(dc.')./qsat(ec.'))*100;   % rel humidity calc

    % UNUSED
    %f{'apress'}(nn,:,:)=x.';
    %f{'cldfrac'}(nn,:,:)=w.';
    %f{'swrad'}(nn,:,:)=s.';
    %f{'rain'}(nn,:,:)=r.';                

    close(f);

  end

  return

end
