% mars3d
% EXAMPLE: PREVIMER_F2-MARS3D-MENOR_20100920T1500Z_REP10.nc
% name	data_type	descr	dimensions	shape units
% time	double	time in seconds (UT)	time	1	seconds since 1900-01-01T00:00:00Z
% longitude	double	longitude	longitude	275	degree_east
% level	float	sigma level	level	30	level
% latitude	double	latitude	latitude	185	degree_north
% XE	float	mean sea surface height	time,latitude,longitude	1,185,275	m
% VZ	float	3d meridional velocity	time,level,latitude,longitude	1,30,185,275	m.s-1
% VWND	float	meridional wind	time,latitude,longitude	1,185,275	m/s
% UZ	float	3d zonal velocity	time,level,latitude,longitude	1,30,185,275	m.s-1
% UWND	float	zonal wind	time,latitude,longitude	1,185,275	m/s
% TEMP	float	temperature	time,level,latitude,longitude	1,30,185,275	degree_Celsius
% SIG	float	niveaux sigma	level	30	
% SAL	float	salinity	time,level,latitude,longitude	1,30,185,275	1e-3
% HY	float	mean water depth at v location	time,latitude,longitude	1,185,275	m
% HX	float	mean water depth at u location	time,latitude,longitude	1,185,275	m
% H0	float	bathymetry relative to the mean level	latitude,longitude	185,275	m

function mars3d (in_file, out_file)
nc=netcdf(in_file,'r');
FILLDATA=1e37;
lon = nc{'longitude'}(:);
lat = nc{'latitude'}(:);

H0=nc{'H0'}(:);
SIG=nc{'SIG'}(:);
XE = nc{'XE'}(:);

% UNUSED
%HX = nc{'HX'}(:);
%HY = nc{'HY'}(:);
%level = nc{'level'}(:);
%UZ = nc{'UZ'}(:);
%VZ = nc{'VZ'}(:);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% dep3d
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% MARS3D vertical interpolation
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% 1- Load variables lon, lat, SIG, H0, XE, VAL (water_temp,
% salinity,u,v,...)
% 2- depth levels ((~= lingth(SIG) to monimize errors)
% 
% 2- Take care: 
%   interpolation artefacts at the surface 
%    (MARS3D grid do not reach 0)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
dep3d=zeros(length(SIG),size(H0,1),size(H0,2));
for kj=1:length(lat)
    for ki=1:length(lon)
        for kk=1:length(SIG)
            if SIG(kk)<0
                dep3d(kk,kj,ki)=SIG(kk)*(H0(kj,ki)+XE(:,kj,ki))+XE(:,kj,ki);
            else
                dep3d(kk,kj,ki)=SIG(kk)*(H0(kj,ki)+XE(:,kj,ki))-H0(:,kj,ki);
            end
        end
    end
end

% clear no more used variables
clear H0;
clear XE;

% open a new NetCDF file to write into:
nc_out=netcdf(out_file,'c');

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% depth
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% defining depth:
%depth=[2 5 (10:10:200) 250 300 500 1500 2000 2500];
depth=linspace(-max(max(dep3d(:,:,1))),-min(min(dep3d(:,:,1))),length(SIG));

% clear no more used variables
clear SIG;
%write to output file
nc_out('depth')=length(depth);
nc_out{'depth'}=ncfloat('depth');
nc_out{'depth'}(:)=depth;
nc_out{'depth'}.long_name='depth';
nc_out{'depth'}.units='m';
nc_out{'depth'}.positive='down';
nc_out{'depth'}.FillValue_ = ncdouble(FILLDATA);
%DEBUG
%depth

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% latitude
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
nc_out('lon') = length(lon);
nc_out{'lon'} = ncdouble('lon');
nc_out{'lon'}(:) = lon;
nc_out{'lon'}.long_name='Longitude';
nc_out{'lon'}.units = 'degrees_east';
nc_out{'lon'}.FillValue_ = ncdouble(FILLDATA);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% longitude
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
nc_out('lat') = length(lat);
nc_out{'lat'} = ncdouble('lat');
nc_out{'lat'}(:) = lat;
nc_out{'lat'}.long_name='Latitude';
nc_out{'lat'}.units = 'degrees_north';
nc_out{'lat'}.FillValue_ = ncdouble(FILLDATA);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% TIME
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
time = nc{'time'}(:);
% http://www.timeanddate.com/
%	date/durationresult.html?d1=01&m1=01&y1=1900&h1
%	=00&i1=00&s1=00&d2=01&m2=01&y2=1980&h2=0&i2=0&s2=0
%
% times are to be delayed from 1-1-1900 at 00:00:00Z
% to 1-1-1980 at 00:00:00Z
%
% Monday, 1 January 1900, 00:00:00
% To, but not including : Tuesday, 1 January 1980, 00:00:00
% The duration is 29,219 days, 0 hours, 0 minutes and 0 seconds
% Or 80 years excluding the end date
% Note: This calculator does not take time zones,
%  daylight saving time or leap seconds into account.
% Alternative time units
% 29,219 days, 0 hours, 0 minutes and 0 seconds
%	can be converted to one of these units:
% 2,524,521,600 seconds
% 42,075,360 minutes
% 701,256 hours
% 4174 weeks (rounded down)

%UNUSED!
%time_origin=time-2524521600;

nc_out('time') = length(time);
nc_out{'time'} = ncdouble('time');
nc_out{'time'}(:) = int64(time);
nc_out{'time'}.long_name='time';
nc_out{'time'}.units = 'seconds since 1980-1-1 0:0:0';
nc_out{'time'}.FillValue_ = ncdouble(FILLDATA);
nc_out{'time'}.time_origin = datestr(datenum(1980,1,1),"yyyymmddTHHMMSS");

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% writing global attributes
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%base time attribute
%"yyyyMMddTHHmmssSSSZ"
nc_out.base_time=datestr(datenum(1900,1,1)+datenum(0,0,0,0,0,time),"yyyymmddTHHMMSS");
% tau attribute
if (length(time)>1)
  nc_out.tau=ncint(int8(time(2)-time(1)));
else
  nc_out.tau=ncint(0);
end

% Preamble.
nc_out.type = 'MARS3D';
nc_out.title='MARS3D';
nc_out.author = 'Carlo Cancellieri, carlo.cancellieri@geo-solutions.it';
nc_out.date = datestr(now);

% nodata
nc_out.nodata=ncdouble(FILLDATA);
% fillvalue
nc_out._FillValue= ncdouble(FILLDATA);
% time origin
nc_out.time_origin = datestr(datenum(1980,1,1),"yyyymmddTHHMMSS");
%"GMT+0"


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% SALT
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%read SAL(inity) from input file
SAL = nc{'SAL'}(:);
%interpolate function
SAL = mars3d_interp(SAL,depth,dep3d,lat,lon);
%SAL_ret
%write to output file
nc_out{'salt'}=ncfloat('time','depth','lat','lon');
nc_out{'salt'}(:)=SAL;
nc_out{'salt'}.long_name='salinity';
nc_out{'salt'}.units='psu';
nc_out{'salt'}.FillValue_ = ncdouble(FILLDATA);
%DEBUG
%SAL_ret
%delete var
clear SAL;
%clear SAL_ret;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% UWND
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%read
UZ = nc{'UZ'}(:);

% TODO: tests
% missing value
%UWND(UWND == nc{'UWND'}.missing_value) = NaN;

%NO NEED INTERPOLATION
%UWND_ret = mars3d_interp(UWND,depth,dep3d,lat,lon);

% STARTING FILE
%  float UZ(time=1, level=30, latitude=185, longitude=275);
%  :long_name = "3d zonal velocity";
%  :units = "m.s-1";
%  :standard_name = "eastward_sea_water_velocity";
% DICTIONARY
%  <metoc defaultUom="urn:ogc:def:uom:UCUM::m/s" type="ocean">
%  	<name>water velocity u-component</name>
%  	<brief>watvel-u</brief>
%  </metoc>

%write to output file
nc_out{'watvel-u'}=ncfloat('time','depth','lat','lon');

% INTERP
UZ = mars3d_interp(UZ,depth,dep3d,lat,lon);

nc_out{'watvel-u'}(:)=UZ;
nc_out{'watvel-u'}.long_name='water velocity u-component';
nc_out{'watvel-u'}.units='m/s';
nc_out{'watvel-u'}.FillValue_ = ncdouble(FILLDATA);
%DEBUG
%UWND
%delete var
clear UZ;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% VWND
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
VZ = nc{'VZ'}(:);
%NO NEED INTERPOLATION
%VWND = mars3d_interp(VWND,depth,dep3d,lat,lon);

% STARTING FILE
%  float VZ(time=1, level=30, latitude=185, longitude=275);
%  :long_name = "3d meridional velocity";
%  :units = "m.s-1";
%  :standard_name = "northward_sea_water_velocity";
% DICTIONARY
%  <metoc defaultUom="urn:ogc:def:uom:UCUM::m/s" type="ocean">
%  	<name>water velocity v-component</name>
%  	<brief>watvel-v</brief>
%  </metoc>

%write to output file
nc_out{'watvel-v'}=ncfloat('time','depth','lat','lon');

% INTERP
VZ = mars3d_interp(VZ,depth,dep3d,lat,lon);

nc_out{'watvel-v'}(:)=VZ;
nc_out{'watvel-v'}.long_name='water velocity v-component';
nc_out{'watvel-v'}.units='m/s';
nc_out{'watvel-v'}.FillValue_ = ncdouble(FILLDATA);
%DEBUG
%VWND
%delete var
clear VZ;


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Water TEMPerature
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
TEMP = nc{'TEMP'}(:);
%interpolate function
TEMP = mars3d_interp(TEMP,depth,dep3d,lat,lon);
%write to output file
nc_out{'wattemp'}=ncfloat('time','depth','lat','lon');
nc_out{'wattemp'}(:)=TEMP;
nc_out{'wattemp'}.long_name='water temperature';
nc_out{'wattemp'}.units='cel';
nc_out{'wattemp'}.FillValue_ = ncdouble(FILLDATA);
%DEBUG
%TEMP
%delete var
clear TEMP;

% close input file
ncclose(nc);

% close output file
ncclose(nc_out);

clear depth;
clear dep3d;
clear lat;
clear lon;

end %function

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% SUBFUNCTION
% VAR - 4D variable
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function VAL_vertinterp = mars3d_interp (VAR, depth, dep3d, lat, lon)
	% Take care: interpolation artefacts at the surface (MARS3D
	% grid do not reach 0)
	% Take care: lon and lat are different for temperature/salinity (rho) and u/v
%i=0;
	% depth is the new regular vertical grid
	VAL_vertinterp=zeros(length(depth),length(lat),length(lon));
	
    for kj=1:length(lat)
		for ki=1:length(lon)
%		printf('isempty(find(isnan(dep3d(:,kj,ki)))): (%d)\n',isempty(find(isnan(dep3d(:,kj,ki)))));
%		printf('~isempty(find(~isnan(VAR(1,:,kj,ki)))): (%d)\n', ~isempty(find(~isnan(VAR(1,:,kj,ki)))));
			if(isempty(find(isnan(dep3d(:,kj,ki)))) & isempty(find(dep3d(:,kj,ki)==99)) & ~isempty(find(~isnan(VAR(1,:,kj,ki)))))
			
				VAL_vertinterp(:,kj,ki)=interp1(-dep3d(:,kj,ki),VAR(1,:,kj,ki),depth);
%	i++;
%	printf('%d->VAL_vertinterp(:,kj,ki): (%d)\n', i,VAL_vertinterp(1,1,kj,ki));
			else
				VAL_vertinterp(:,kj,ki)=NaN;
			end
		end
	end
	%VAL_vertinterp
end %function

