
% in_file 	-> roms input file
% out_file 	-> netcdf-cf resulting output file

function roms_f (in_file, out_file)

% read from file
nc=netcdf(in_file,'r');

% used on lat,lon,dept may not vary depending time

%grdroms=roms_get_grid(in_file);
%grdroms=roms_get_grid(in_file,in_file,tt,1);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% ROMS vertical interpolation
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% 1- Load variables lon, lat, VAL (temp,
% salinity,u,v,...)
% 
% 2- Take care: 
%   interpolation artefacts at the surface 
%    (MARS3D grid do not reach 0)
%   lon and lat are different for temperature/salinity (rho) and u/v
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% tt is time index

% strdepth='z_r'; if temperature or salinity
% strdepth='z_u'; if u
% strdepth='z_v'; if v

% strlon and strlat also depend on the variable 

% if u 
%strdepth='z_u';
%%%


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% time shift
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% From and including: gioved 23 maggio 1968
% To, but not including : marted 1 gennaio 1980

% calculating difference (in days)
diff=(datenum(1980,1,1)-datenum(1968,5,23));
% calculate seconds
diff=diff*3600*24;
% getting time (shifted)
time=nc{'ocean_time'}(:)-(diff);


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% START WRITING: out_file
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% write to file
nc_out=netcdf(out_file,'c');

% global attributes
nc_out._FillValue= ncfloat(1.0e+37);
nc_out.nodata= ncfloat(1.0e+37);
nc_out.base_time=int64(time(1));
% Preamble.
nc_out.type = 'ROMS';
nc_out.title='ROMS';
nc_out.author = 'Carlo Cancellieri, carlo.cancellieri@geo-solutions.it';
nc_out.date = datestr(now);

%write time
write_time(nc_out, time);

if (length(time)>1)
  % TAU is in hour(s)
  nc_out.tau=int64((time(2)-time(1))/3600);
else
  nc_out.tau=0;
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% We have 3 different coordinates matrices:
% lon_rho, lat_rho, z_r (used for -> water-temperature and salinity)
% lon_u, lat_u, z_u	(used for -> water u component)
% lon_v, lat_v, z_v	(used for -> water v component)

% lat and lon do not depend on time (using first time)
grd=roms_get_grid(in_file,in_file,1,1); % activate ZETA

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% dept is a fixed vector
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%depth=[2 5 linspace(10,2500,30)];
depth=linspace(
		-(max(max(max(grd.z_r(:,1,1)),max(grd.z_u(:,1,1))),max(grd.z_v(:,1,1)))),		%stop
		-(min(min(min(grd.z_r(:,1,1)),min(grd.z_u(:,1,1))),min(grd.z_v(:,1,1)))),		%start
		max(max(length(grd.z_r(:,1,1)),length(grd.z_u(:,1,1))),length(grd.z_v(:,1,1)))) 	%size
%write depth
write_depth(nc_out, depth);

%size(grd.lat_rho)
%size(grd.lon_rho)
%size(grd.z_r)
%size(grd.lat_u)
%size(grd.lon_u)
%size(grd.z_u)
%size(grd.lat_v)
%size(grd.lon_v)
%size(grd.z_v)



% Building a new horizontal regular choordinate system

%Building lat
lat_i=linspace(
		min(min(min(grd.lat_rho(:,1)),min(grd.lat_u(:,1))),min(grd.lat_v(:,1))),		%start
		max(max(max(grd.lat_rho(:,1)),max(grd.lat_u(:,1))),max(grd.lat_v(:,1))),		%stop
		max(max(length(grd.lat_rho(:,1)),length(grd.lat_u(:,1))),length(grd.lat_v(:,1)))); 	%size
%write lat
write_lat(nc_out, lat_i);

%Building lon
lon_i=linspace(
		min(min(min(grd.lon_rho(1,:)),min(grd.lon_u(1,:))),min(grd.lon_v(1,:))),		%start
		max(max(max(grd.lon_rho(1,:)),max(grd.lon_u(1,:))),max(grd.lon_v(1,:))),		%stop
		max(max(length(grd.lon_rho(1,:)),length(grd.lon_u(1,:))),length(grd.lon_v(1,:)))); 	%size
%write lon
write_lon(nc_out, lon_i);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Writing water temperature
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% if temp or sal
% strdepth='z_r';
% eval(['dep3d=grdroms.' strdepth ';']);
% -> dep3d=grdroms.z_r;

%lon=grdroms.lon_rho(1,:);
%lat=grdroms.lat_rho(:,1);

%debug
%size(depth)
%size(grdroms.lat_rho(:,1))
%size(grdroms.lon_rho(1,:))
%size(dep3d)
%size(TEMP(:,:,:,:))

nc_out{'wattemp'}=ncfloat('time','depth','latitude','longitude');
nc_out{'wattemp'}.long_name='water temperature';
nc_out{'wattemp'}.units='cel';
nc_out{'wattemp'}.FillValue_= ncfloat(1.0E37);
nc_out{'wattemp'}.nodata= ncfloat(1.0E37);
% for each time slice
for tk=1:length(time)
	
	grd=roms_get_grid(in_file,in_file,tk); % do not activate ZETA
	T = squeeze(nc{'temp'}(tk,:,:,:));
	
%TODO A BETTER INTERPOLATION MESHGRID NOT USING roms_get_grid matrices
	
	[X,Y,Z]=meshgrid(grd.lat_rho(:,1),grd.z_r(:,1,1),grd.lon_rho(1,:));
	
	%[X,Y]=meshgrid(grd.lon_rho(1,:),grd.lat_rho(:,1));
%	T=squeeze(TEMP(tk,:,:,:));

	
	T=interp3(X,Y,Z,T,lat_i,-depth,lon_i,"linear",1.0E37);
	% write to netcdf adding a time slice per cycle
	nc_out{'wattemp'}(tk,:,:,:)=T;
	
	%debug
	%TEMP
end
%clear
clear TEMP;
% if temp or sal           
%strdepth='z_r';
% eval(['dep3d=grdroms.' strdepth ';']);

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% sal
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%lon=grdroms.lon_rho(1,:);
%lat=grdroms.lat_rho(:,1);
%dep3d=grdroms.z_r;

% for each time slice
nc_out{'salt'}=ncfloat('time','depth','latitude','longitude');
nc_out{'salt'}.long_name='salinity';
nc_out{'salt'}.units='psu';
nc_out{'salt'}.FillValue_= ncfloat(1.0E37);
nc_out{'salt'}.nodata= ncfloat(1.0E37);
for tk=1:length(time)
	grd=roms_get_grid(in_file,in_file,tk); % do not activate ZETA
	T=squeeze(nc{'salt'}(tk,:,:,:));
%	SAL(1,:,:,:)=roms_interp(depth,grd.lon_rho(1,:),grd.lat_rho(:,1),grdroms.z_r,SAL(1,:,:,:));

	[X,Y,Z]=meshgrid(grd.lat_rho(:,1),grd.z_r(:,1,1),grd.lon_rho(1,:));
	T=interp3(X,Y,Z,T,lat_i,-depth,lon_i,"linear",1.0E37);
	
	nc_out{'salt'}(tk,:,:,:)=T;
	%debug
	%T
end
%clear
clear T;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%% water component u				%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% if u or v lat_u ...
nc_out{'watvel-u'}=ncfloat('time','depth','latitude','longitude');
nc_out{'watvel-u'}.long_name='water velocity u-component';
nc_out{'watvel-u'}.units='m/s';
nc_out{'watvel-u'}.FillValue_= ncfloat(1.0E37);
nc_out{'watvel-u'}.nodata= ncfloat(1.0E37);
for tk=1:length(time)

	grd=roms_get_grid(in_file,in_file,tk,1);
%	lon=grd.lon_u(1,:);
%	lat=grd.lat_u(:,1);
%	dep3d=grd.z_u;
	
	T =squeeze(nc{'u'}(tk,:,:,:));
%	U(1,:,:,:)=roms_interp(depth,grd.lon_u(1,:),grd.lat_u(:,1),dep3d,U(1,:,:,:));

	[X,Y,Z]=meshgrid(grd.lat_u(:,1),grd.z_u(:,1,1),grd.lon_u(1,:));
%	T=squeeze(U(tk,:,:,:));

	T=interp3(X,Y,Z,T,lat_i,-depth,lon_i,"linear",1.0E37);
	
	nc_out{'watvel-u'}(tk,:,:,:)=T;
	%debug
	%T
end
%clear
clear T;

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% water component v
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% if u or v lat_u ...
% strdepth='z_v'; if v

nc_out{'watvel-v'}=ncfloat('time','depth','latitude','longitude');
nc_out{'watvel-v'}.long_name='water velocity v-component';
nc_out{'watvel-v'}.units='m/s';
nc_out{'watvel-v'}.FillValue_= ncfloat(1.0E37);
nc_out{'watvel-v'}.nodata= ncfloat(1.0E37);
for tk=1:length(time)

	grd=roms_get_grid(in_file,in_file,tk,1);
%	lon=grdroms.lon_v(1,:);
%	lat=grdroms.lat_v(:,1);
%	dep3d=grdroms.z_v;
	
	T=squeeze(nc{'v'}(tk,:,:,:));
%	V(1,:,:,:)=roms_interp(depth,grd.lon_v(1,:),grd.lat_v(:,1),dep3d,V(1,:,:,:));
	[X,Y,Z]=meshgrid(grd.lat_v(:,1),grd.z_v(:,1,1),grd.lon_v(1,:));
	T=interp3(X,Y,Z,T,lat_i,-depth,lon_i,"linear",1.0E37);
	nc_out{'watvel-v'}(tk,:,:,:)=T;
	%debug
	%T
end
%clear
clear T;

ncclose(nc_out);
ncclose(nc);
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% time
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function write_time(nc_out, time)
nc_out('time') = length(time);
nc_out{'time'} = ncdouble('time');
nc_out{'time'}(:) = int64(time);
nc_out{'time'}.long_name='time';
nc_out{'time'}.units = 'seconds since 1980-1-1 0:0:0';
nc_out{'time'}.time_origin = int64(time(1));
end


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% latitude
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function write_lat(nc_out, lat)
nc_out('latitude') = length(lat);
nc_out{'latitude'} = ncdouble('latitude');
nc_out{'latitude'}(:) = lat;
nc_out{'latitude'}.long_name='Latitude';
nc_out{'latitude'}.units = 'degrees_north';
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% longitude
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function write_lon(nc_out, lon)
nc_out('longitude') = length(lon);
nc_out{'longitude'} = ncdouble('longitude');
nc_out{'longitude'}(:) = lon;
nc_out{'longitude'}.long_name='Longitude';
nc_out{'longitude'}.units = 'degrees_east';
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% depth
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function write_depth(nc_out, depth)
nc_out('depth')=length(depth);
nc_out{'depth'}=ncfloat('depth');
nc_out{'depth'}(:)=depth;
nc_out{'depth'}.long_name='depth';
nc_out{'depth'}.units='m';
nc_out{'depth'}.positive='down';
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% interpolation
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%function [VAL_vertinterp] = roms_interp (depth,lon,lat,dep3d,VAL)
	% depth is the new regular vertical grid
%	VAL_vertinterp=zeros(length(depth),length(lat),length(lon));
%	for kj=1:length(lat)
%		for ki=1:length(lon)
%			if(isempty(find(isnan(dep3d(:,kj,ki)))) & ~isempty(find(~isnan(VAL(1,:,kj,ki)))))
%				VAL_vertinterp(:,kj,ki)=interp1(-dep3d(:,kj,ki),VAL(1,:,kj,ki),depth);
%			else
%				VAL_vertinterp(:,kj,ki)=NaN;
%			end
%		end
%	end
%end %function
