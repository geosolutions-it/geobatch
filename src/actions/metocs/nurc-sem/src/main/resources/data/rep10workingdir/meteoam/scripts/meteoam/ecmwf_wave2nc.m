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

%function ecmwf_wave2nc(ddir,fdate,ncfile,base)
function ecmwf_wave2nc(ddir,ncfile)

  % we get all the times for all the dates
  %elenco=dir([ddir,'/J4M',fdate,base,'*']);
  elenco=dir([ddir,'/J4M','*','*','*']);

  % processing grib file forecast by forecast

  deltantimes=3; maxntimes=72; 

  nn=0;
  for ntimes=3:deltantimes:maxntimes;
    nn=nn+1;

    gfile  = [ddir '/' elenco(nn+1).name];

    % grab data

    uhdr=read_grib(gfile,{'SNOHF'},1,0,0);      % H sig
    vhdr=read_grib(gfile,{'5WAVA'},1,0,0);      % Mean Wave Dir
    ehdr=read_grib(gfile,{'DTRF'},1,0,0);       % Mean Wave Per

    ut=read_grib(gfile,uhdr(1).record,0,1,0); 
    vt=read_grib(gfile,vhdr(1).record,0,1,0); 
    et=read_grib(gfile,ehdr(1).record,0,1,0); 

    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    % grab the dates (and check to make sure they match)
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
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

    if(max(abs(diff([du dv de ])))>eps)
      disp('time mismatch');
      return
    else
      %% we have to transform time from days (returned by datenum) in seconds
      seconds=(du-datenum(1980,1,1))*3600*24;
      _base_time=du;
    end %if

    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    % FIRST CYCLE: do only once
    %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    if(nn==1);
      
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
    
      %[alon,alat]=meshgrid(rlon,rlat); %UNUSED

      % NetCDF metadata

      f = netcdf(ncfile, 'clobber');

      % Preamble.

      f.type ='ECMWF WAVE forecast';
      f.title='ECMWF WAVE forecast';
      f.author = 'Jacopo Chiggiato, chiggiato@nurc.nato.int - Carlo Cancellieri, carlo.cancellieri@geo-solutions.it';
      f.date = datestr(now);

      f('time') =0;   % unlimited dimension
      f{'time'}=ncdouble('time');
      f{'time'}.long_name=('time since initialization');
      f{'time'}.units=('seconds since 1980-1-1 0:0:0');%'days since 1968-5-23 00:00:00 UTC');
      f{'time'}.time_origin = datestr(datenum(1980,1,1),"yyyymmddTHHMMSS");
      %f{'time'}.calendar='MJD';

      f('lon') = im;
      f{'lon'}=ncfloat('lon');%'lat',
      f{'lon'}.long_name='Longitude';
      f{'lon'}.units = 'degrees_east';
      f{'lon'}(:)=rlon; %alon;

      f('lat') = jm;
      f{'lat'}=ncfloat('lat');%,'lon'
      f{'lat'}.long_name='Latitude';
      f{'lat'}.units = 'degrees_north';
      f{'lat'}(:)=rlat; %alat;

      % Meteo Fields
      %f{'Hwave'}=ncfloat('time','lat','lon');
      f{'hs'}=ncfloat('time','lat','lon');
      f{'hs'}.units = 'm';
      f{'hs'}.missing_value = ncfloat(1.e35);
      f{'hs'}.FillValue_=ncfloat(1.e35);
      f{'hs'}.long_name='sea surface swell wave significant height';
      %f{'Hwave'}.long_name='Significant Wave Heigth';
      f{'hs'}.coordinates='lat lon';

% UNUSED
      %f{'Dwave'}=ncfloat('time','lat','lon');
      f{'meanwavdir'}=ncfloat('time','lat','lon');
      f{'meanwavdir'}.units = 'deg';
      f{'meanwavdir'}.missing_value = ncfloat(1.e35);
      f{'meanwavdir'}.FillValue_=ncfloat(1.e35);
      f{'meanwavdir'}.long_name='mean wave direction';
      %f{'Dwave'}.long_name='Mean Wave Direction';
      f{'meanwavdir'}.coordinates='lat lon';

      %f{'Twave'}=ncfloat('time','lat','lon');
      f{'meanwavperiod'}=ncfloat('time','lat','lon');
      f{'meanwavperiod'}.units = 's';
      f{'meanwavperiod'}.missing_value = ncfloat(1.e35);
      f{'meanwavperiod'}.FillValue_=ncfloat(1.e35);
      %f{'Twave'}.long_name = 'Mean Wave Period';
      f{'meanwavperiod'}.long_name = 'mean wave period';
      f{'meanwavperiod'}.coordinates='lat lon';


      % nodata
      f.nodata=ncdouble(1.e35);
      % fillvalue
      f._FillValue= ncdouble(1.e35);
      %base time attribute "yyyyMMddTHHmmssSSSZ"
      f.base_time=[datestr(datenum(1980,1,1)+datenum(0,0,0,0,0,t(1)),"yyyymmddTHHMMSS"),'000Z'];
      % time origin
      f.time_origin = [datestr(datenum(1980,1,1),"yyyymmddTHHMMSS"),'000Z'];
      % save seconds to (eventually) calculate TAU
      first_time=seconds;
      % setting TAU
      f.tau=ncint(0);

      close(f);

    end %if nn==1


    % reshape the data

    ru=reshape(ut.fltarray,ut.gds.Ni,ut.gds.Nj);
    rv=reshape(vt.fltarray,vt.gds.Ni,vt.gds.Nj);
    e=reshape(et.fltarray,et.gds.Ni,et.gds.Nj);

    ru(ru>10000)=1.e35;%NaN;
    rv(rv>10000)=1.e35;%NaN;
    e(e>10000)=1.e35;%NaN;

    % writing

    f = netcdf(ncfile, 'write');

    % update TAU
    if (nn==2) % if there are more than 1 times
      % tau attribute in hour
      f.tau=ncint(int8((seconds/3600)-(first_time/3600)));
      clear fist_time;
    end %if

    f{'hs'}(nn,:,:)=ru'; 
    f{'meanwavdir'}(nn,:,:)=rv';
    f{'meanwavperiod'}(nn,:,:)=e.';
    f{'time'}(nn)=int64(seconds);%jd-2440000;

    close(f);

  end %for

return

end %function
