
% Extract UV Fluxes of CA RSM 1948-2005

[lon,lat]=rsm_CA_coord;


% Process the 2 MASKS
gribfile='/sdd/kcobb/lsmsk00.grib';
mask1=rsm_extract_record(gribfile, 1);
mask1(mask1==1)=nan; mask1(mask1==0)=1;
gribfile='/sdd/kcobb/lsmsk70.grib';
mask2=rsm_extract_record(gribfile, 1);
mask1(mask2==1)=nan; mask2(mask2==0)=1;
MASK=mask1.*mask2;


gribfile='/sdd/kcobb/uvflx.monthly.grib';
inv = rsm_get_inv(gribfile);
inv.year(inv.year < 40) = inv.year(inv.year < 40)+100;

iu = find (strcmp( inv.varname,'UFLX'));
iv = find (strcmp( inv.varname,'VFLX'));
it=length(iv);

UFLX=zeros(size(lon,1), size(lon,2), it);
VFLX=zeros(size(lon,1), size(lon,2), it);

for irec=1:it
  disp(['Extracting record : ',num2str(irec)]);
  UFLX(:,:,irec) = -rsm_extract_record(gribfile, iu(irec) );
  VFLX(:,:,irec) = -rsm_extract_record(gribfile, iv(irec) );
end

% save date
rsm.datenum = datenum( 1900+ inv.year(iu), inv.month(iu), 15);
rsm.year=1900+ inv.year(iu);
rsm.month=inv.month(iu);
rsm.lon=lon;
rsm.lat=lat;
rsm.mask=MASK;

% Mask land values and extrapolate ocean to land with OA map
rsm.uflx=zeros(size(lon,1), size(lon,2), it);
rsm.vflx=zeros(size(lon,1), size(lon,2), it);

i1=find(~isnan(MASK));
i2=find(isnan(MASK));
tracer=UFLX(:,:,1).*MASK;
pmap=rnt_oapmap(lon(i1),lat(i1),tracer(i1),lon(i2),lat(i2),20);
tracer(:)=0;
tracer(i2)=error;
rsm.error=tracer;


for i=1:it 
    disp(['Processing land for record : ',num2str(i)]);
    tracer=UFLX(:,:,i).*MASK;
    [dataout,error]=rnt_oa2d(lon(i1),lat(i1),tracer(i1),lon(i2),lat(i2),3,3,pmap,20);    
    tracer(i2) = dataout;
    rsm.uflx(:,:,i) = tracer;
    
    tracer=VFLX(:,:,i).*MASK;
    [dataout,error]=rnt_oa2d(lon(i1),lat(i1),tracer(i1),lon(i2),lat(i2),3,3,pmap,20);    
    tracer(i2) = dataout;
    rsm.vflx(:,:,i) = tracer;
end    
    

save /drive/edl/NEPD/nepd-data/RSM_uv_flux.mat rsm
