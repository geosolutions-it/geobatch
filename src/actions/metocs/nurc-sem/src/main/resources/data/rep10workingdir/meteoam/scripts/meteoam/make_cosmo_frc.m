%
% Creating COSMO met forcing for ROMS 
% chiggiato@nurc.nato.int
%
% need to split in several forcing files because of the size!!

  grid_file='../Grid/LigurianSea.nc';   % input: ROMS grid file

% time window: DAYS to be processed (i.e., COSMO netcdf files)

    %start=fix(julian([2010 6 6 0 0 0]));
    start=fix(julian(clock));
% building yyyymmdd string for dir and file names

adate = gregorian(start);
rfile=datestr(adate,24);
rfile(6)=[]; rfile(3)=[];
rfile=[rfile(5) rfile(6) rfile(7) rfile(8) rfile(3) rfile(4) rfile(1) rfile(2)];
out_file1=['./Archive/cosmo_',rfile,'_frc.nc'];        % output: ROMS forcing file

% main loop over daily files ...

inow=0;

tmstep=1; numfield=73;

for tindex=start

daynow=gregorian(tindex);

% LOCAL directory for COSMO netcdf files

ddir='./Archive/'; % root directory where COSMO data are

% surface meteo input file

tmpstr=sprintf('%04d%02d%02d',daynow(1),daynow(2),daynow(3));
filename=[ddir 'elm_' tmpstr '00.nc'];

nc_bulk=netcdf(filename);

% read COSMO grid locations from surface File

alon=nc_bulk{'lon'}(:);
alat=nc_bulk{'lat'}(:);

% read ROMS grid locations 

nc_grid=netcdf(grid_file);
rlon=nc_grid{'lon_rho'}(:);
rlat=nc_grid{'lat_rho'}(:);
mask_rho=nc_grid{'mask_rho'}(:);
ang=nc_grid{'angle'}(:);
close(nc_grid);
iwater=find(mask_rho==1.);
% Set up to interpolate on ij, rather than lon/lat
[nx,ny]=size(alon);
[ii,jj]=meshgrid(1:ny,1:nx);

%  load('forcing_cache.mat','ib','jb');
  xv=[alon(:,1); alon(end,:).'; alon([end:-1:1],end); alon(1,[end:-1:1]).'];
  yv=[alat(:,1); alat(end,:).'; alat([end:-1:1],end); alat(1,[end:-1:1]).'];

  in=1:length(rlon(:));
  ib=griddata(alon,alat,ii,rlon(:),rlat(:));
  jb=griddata(alon,alat,jj,rlon(:),rlat(:));
%  save('forcing_cache.mat','ib','jb');

if(tindex==start);
    
% Open netcdf output forcing file 

f1 = netcdf(out_file1, 'clobber');


if isempty(f1)
   disp(' ROMS forcing test file not created.')
   return
end

% Preamble.

f1.title='COSMO-ME forecast file';
f1.type = 'ROMS forcing file';
f1.author = 'Jacopo Chiggiato; chiggiato@nurc.nato.int';
f1.date = datestr(now);

% define dimensions
f1('wind_time') = numfield;
f1('tair_time') = numfield;
f1('qair_time') = numfield;
f1('pair_time') = numfield;
f1('cloud_time')= numfield;
f1('srf_time')  = numfield;
f1('rain_time') = numfield;

[nn,mm]=size(mask_rho);
f1('xi_rho') = mm;
f1('eta_rho') = nn;
f1('xi_rho') = mm;
f1('eta_rho') = nn;
f1('xi_rho') = mm;
f1('eta_rho') = nn;

% define independent variables

f1{'wind_time'}  = ncdouble('wind_time');    % Uwind,Vwind (m/s)
f1{'tair_time'}  = ncdouble('tair_time');    % Air Temperature - Tair (C)
f1{'qair_time'}  = ncdouble('qair_time');    % Relative Humidity - Qair (%)
f1{'pair_time'}  = ncdouble('pair_time');    % Air pressure - Pair (mb)
f1{'cloud_time'} = ncdouble('cloud_time');   % total cloud cover (fraction [0-1])
f1{'srf_time'}   = ncdouble('srf_time');     % net shortwave radiation  (watts/m2)
f1{'rain_time'}  = ncdouble('rain_time');    % total precipitation (kg/m2/s)

f1{'wind_time'}.units  = 'days since 1968-05-23';
f1{'tair_time'}.units  = 'days since 1968-05-23';
f1{'qair_time'}.units  = 'days since 1968-05-23';
f1{'pair_time'}.units  = 'days since 1968-05-23';
f1{'cloud_time'}.units = 'days since 1968-05-23';
f1{'srf_time'}.units   = 'days since 1968-05-23';
f1{'rain_time'}.units  = 'days since 1968-05-23';

% tell ROMS that the time dimension for swrad is "srf_time"
f1{'swrad'}.time = 'srf_time';

% set fill values 
fillval(f1{'wind_time'},1e35);
fillval(f1{'tair_time'},1e35);
fillval(f1{'qair_time'},1e35);
fillval(f1{'pair_time'},1e35);
fillval(f1{'cloud_time'},1e35);
fillval(f1{'srf_time'},1e35);
fillval(f1{'rain_time'},1e35);

% define dependent variables

% Wind at 10 m  (will be rotated onto grid coordinates)

f1{'Uwind'}=ncfloat('wind_time','eta_rho','xi_rho');  % Uwind (m/s)
f1{'Vwind'}=ncfloat('wind_time','eta_rho','xi_rho');  % Vwind (m/s)
f1{'Tair'}=ncfloat('tair_time','eta_rho','xi_rho');   % Air pressure - Pair (mb)
f1{'Qair'}=ncfloat('qair_time','eta_rho','xi_rho');   % Relative Humidity - Qair (%)
f1{'Pair'}=ncfloat('pair_time','eta_rho','xi_rho');   % Air Temperature - Tair (C)
f1{'cloud'}=ncfloat('cloud_time','eta_rho','xi_rho'); % total cloud cover (fraction [0-1])
f1{'rain'}=ncfloat('rain_time','eta_rho','xi_rho');   % totakl precipitation (kg/m2/s)
f1{'swrad'}=ncfloat('srf_time','eta_rho','xi_rho');   % net shortwave radiation  (watts/m2)

f1{'Uwind'}.units   = 'm/s';
f1{'Vwind'}.units   = 'm/s';
f1{'Tair'}.units    = 'celsius';
f1{'Qair'}.units    = '%';
f1{'Pair'}.units    = 'mbar';
f1{'cloud'}.units   = 'fraction';
f1{'rain'}.units    = 'Kg/m^2/s';
f1{'swrad'}.units   = 'W/m^2';

f1{'Uwind'}.long_name ='Zonal Wind component (10m)';
f1{'Vwind'}.long_name ='Meridional WInd Component (10m)';
f1{'Tair'}.long_name  ='Air Temperature (2m)';
f1{'Qair'}.long_name  ='Relative Humidity (2m)';
f1{'Pair'}.long_name  ='Mean Sea Level Pressure';
f1{'cloud'}.long_name ='Total Cloud Cover';
f1{'rain'}.long_name  ='Total Precipitation';
f1{'swrad'}.long_name ='Net ShortWave Radiation';

end

% Read and write meteo varas
 
im1=1; im2=73;

for i=im1:im2;   % use the forecast time window 00+00-->00+24

 jd=nc_bulk{'time'}(i)+2440000;
 gregorian(jd)
 it=i;

 w=nc_bulk{'U10',1}(it,:,:)+sqrt(-1)*nc_bulk{'V10',1}(it,:,:);
 wi=zeros(size(mask_rho));   
 wi(:)=interp2(ii,jj,w,ib,jb);  % data interpolated onto ROMS grid
 wr=wi.*exp(-sqrt(-1)*ang);     % rotate wind into ROMS xi, eta coordinates

 at=nc_bulk{'atemp',1}(it,:,:);
 ati=zeros(size(mask_rho));   
 ati(:)=interp2(ii,jj,at,ib,jb);  
 
 dpt=nc_bulk{'relhum',1}(it,:,:);
 dti=zeros(size(mask_rho));   
 dti(:)=interp2(ii,jj,dpt,ib,jb); 

 ap=nc_bulk{'apress',1}(it,:,:);
 api=zeros(size(mask_rho));   
 api(:)=interp2(ii,jj,ap,ib,jb);  

 cld=nc_bulk{'cldfrac',1}(it,:,:);
 cldi=zeros(size(mask_rho));   
 cldi(:)=interp2(ii,jj,cld,ib,jb);  

 swrnew=nc_bulk{'swrad',1}(it,:,:);
 %if(it>1)
 swrold=nc_bulk{'swrad',1}(it-1,:,:);
 swr= (swrnew*(it-1)-swrold*(it-2));   % conversion to "istantaneous" values
 %end
 swri=zeros(size(mask_rho));   
 swri(:)=interp2(ii,jj,swr,ib,jb);  

 rain=nc_bulk{'rain',1}(it,:,:);
 if(it>1)
 rainold=nc_bulk{'rain',1}(it-1,:,:);
 rain=(rain-rainold);                    % kg / m^2 last timestep
 end
 raini=zeros(size(mask_rho));   
 raini(:)=interp2(ii,jj,rain,ib,jb);  

 
 %inow=i+(tindex-inijd)*8;
 inow=inow+1;

 f1{'wind_time'}(inow)  = jd-2440000;
 f1{'qair_time'}(inow)  = jd-2440000;
 f1{'tair_time'}(inow)  = jd-2440000;
 f1{'pair_time'}(inow)  = jd-2440000;
 f1{'cloud_time'}(inow) = jd-2440000;
 f1{'srf_time'}(inow)   = jd-2440000-0.02083;  % due because swrad from COSMO is not an istantaneous field 
 f1{'rain_time'}(inow)  = jd-2440000-0.02083;  % due because rain  from COSMO is not an istantaneous field 

 f1{'Uwind'}(inow,:,:)  = real(wr);
 f1{'Vwind'}(inow,:,:)  = imag(wr);
 f1{'Tair'}(inow,:,:)   = ati;
 f1{'Qair'}(inow,:,:)   = dti;
 f1{'Pair'}(inow,:,:)   = api;
 f1{'cloud'}(inow,:,:)  = cldi;
 f1{'swrad'}(inow,:,:)  = swri;
 f1{'rain'}(inow,:,:)   = raini./3600;   % conversion to [kg / m^2 s]

end

close(nc_bulk);

end

close(f1); 
disp(' done')
exit
