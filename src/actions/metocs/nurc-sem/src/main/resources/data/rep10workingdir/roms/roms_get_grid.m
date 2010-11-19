function grd = roms_get_grid(grd_file,scoord,tindex,calc_zuv)
% grd = roms_get_grid(grd_file,scoord,tindex,calc_zuv);
%
% Gets the lon,lat,mask,depth [and z coordinates] from netcdf grd_file
% 
% Input:
%     grd_file: The roms netcdf grid file name
%           or,
%               an existing grd structure to which the vertical coordinates 
%               are to be added or updated
%
% Optional inputs:
%     scoord:   ROMS his/rst/avg file from which the s-coord params can be
%               determined
%               or 4-element vector [theta_s theta_b Tcline N]
%     tindex:   Time index into his/rst/avg file from which to take the zeta
%               information when computing z. 
%               If tindex = 0 zeta is assumed to be zero
%     calc_zuv: If present, this argument (any value) activates computing
%               the depths z_u and z_v on the u and v points of the 
%               ROMS C-grid
%            
% Output is a structure containing all the grid information
%
% John Wilkin
% Updated (Sept 2002) to correct scoordinate formulation and optionally
% include zeta in the calculation

%if isstruct(grd_file)
if isstruct(grd_file)
  
  % if the first input is already a grd structure
  % the intention is to add vertical coordinates below 
  grd = grd_file;
  
else
  
  % get the grid information from a ROMS grid file  
  nc = netcdf(grd_file,'r');  
  grd.grd_file = grd_file;
  
  grd.lon_rho = nc{'lon_rho'}(:);
  grd.lat_rho = nc{'lat_rho'}(:);
  grd.mask_rho = nc{'mask_rho'}(:);
  grd.angle = nc{'angle'}(:);
  
  grd.h = nc{'h'}(:);
  
  grd.lon_psi = nc{'lon_psi'}(:);
  grd.lat_psi = nc{'lat_psi'}(:);
  grd.mask_psi = nc{'mask_psi'}(:);
  
  grd.lon_v = nc{'lon_v'}(:);
  grd.lat_v = nc{'lat_v'}(:);
  grd.mask_v = nc{'mask_v'}(:);
  
  grd.lon_u = nc{'lon_u'}(:);
  grd.lat_u = nc{'lat_u'}(:);
  grd.mask_u = nc{'mask_u'}(:);
  
  grd.pm = nc{'pm'}(:);
  grd.pn = nc{'pn'}(:);
  
  grd.mask_rho_nan = grd.mask_rho;
  land = find(grd.mask_rho_nan==0);
  grd.mask_rho_nan(land) = NaN;
  
  close(nc)
  
end

if nargin > 1  
  
  % get z_r and z_w for the given s-coordinate parameters
  
  if ~ischar(scoord)
    
    % warning([ 'The option of a 4-element s-coordinate parameter ' ...
    %	  'vector has not be checked fully'])
    
    theta_s = scoord(1);
    theta_b = scoord(2);
    Tcline  = scoord(3);
    N       = scoord(4);
    h = grd.h;
    
    % code lifted from hernan's scoord3.m
    c1=1.0;
    c2=2.0;
    p5=0.5;
    Np=N+1;
    ds=1.0/N;
    hmin=min(min(h));
    hmax=max(max(h));
    hc=min(hmin,Tcline);
    [Mp Lp]=size(h);    
    % rho points
    Nlev=N;
    lev=1:N;
    sc=-c1+(lev-p5).*ds;
    Ptheta=sinh(theta_s.*sc)./sinh(theta_s);
    Rtheta=tanh(theta_s.*(sc+p5))./(c2*tanh(p5*theta_s))-p5;
    Cs=(c1-theta_b).*Ptheta+theta_b.*Rtheta;
    sc_r = sc(:);
    Cs_r = Cs(:);    
    % w points
    Nlev=Np;
    lev=0:N;
    sc=-c1+lev.*ds;
    Ptheta=sinh(theta_s.*sc)./sinh(theta_s);
    Rtheta=tanh(theta_s.*(sc+p5))./(c2*tanh(p5*theta_s))-p5;
    Cs=(c1-theta_b).*Ptheta+theta_b.*Rtheta;
    sc_w = sc(:);
    Cs_w = Cs(:);
    
  else
  
    % input 'scoord' is the name of a his/avg/rst file name
    % attempt to get s-coord params from the file
    
    nc2 = netcdf(scoord,'r');
    theta_s = nc2{'theta_s'}(:);
    theta_b = nc2{'theta_b'}(:);
    Tcline = nc2{'Tcline'}(:);
    sc_r = nc2{'s_rho'}(:);
    sc_w = nc2{'s_w'}(:);
    if isempty(sc_r) & isempty(sc_w)
      warning('Reading old style non-CF-compliant scoord definitions')
      sc_r = nc2{'sc_r'}(:);
      sc_w = nc2{'sc_w'}(:);
    end    
    Cs_w = nc2{'Cs_w'}(:);
    Cs_r = nc2{'Cs_r'}(:);
    
    N = length(sc_r);
    Np = N+1;
    
    hc = nc2{'hc'}(:);
    if isempty(hc)
      hc = min(h(:));
    end
    
    if length(sc_w)==N
      sc_w = [-1; sc_w];
      Cs_w = [-1; Cs_w];
    end

    close(nc2)
    
  end
  
  % zeta  
  zeta = zeros(size(grd.h)); % default
  if nargin > 2 % option to include zeta in z calculation
    if tindex ~= 0
      if ~ischar(scoord)
	error([ 'Can''t process zeta from file in the case that ' ...
	      ' scoord parameters are input as a vector'])'
      end
      nc2 = netcdf(scoord,'r');
      zeta = nc2{'zeta'}(tindex,:,:);
      close(nc2)
      if isempty(zeta)
	warning([ 'zeta not found in ' scoord])
	zeta = zeros(size(grd.h));
      end	
    end
  end    
  grd.zeta = zeta;
  
  % rho-points  
  h = grd.h;
  scmCshc = (sc_r-Cs_r)*hc;
  z_r = repmat(scmCshc,[1 length(h(:))]) + Cs_r*h(:)';
  if any(zeta(:)~=0)
    z_r = z_r + scmCshc*[zeta(:)./h(:)]' + (1+Cs_r)*zeta(:)';
  end
  grd.z_r = reshape(z_r,[N size(h)]);
  
  % w-points  
  scmCshc_w = (sc_w-Cs_w)*hc;
  z_w = repmat(scmCshc_w,[1 length(h(:))]) + Cs_w*h(:)';
  if any(zeta(:)~=0)
    z_w = z_w + scmCshc_w*[zeta(:)./h(:)]' + (1+Cs_w)*zeta(:)';
  end
  grd.z_w = reshape(z_w,[Np size(h)]);
  clear z_r z_w
  
  if nargin > 3
    
    % u-points
    hu = 0.5*(h(:,1:end-1)+h(:,2:end));
    zu = 0.5*(squeeze(zeta)(:,1:end-1)+squeeze(zeta)(:,2:end));
    z_u = repmat(scmCshc,[1 length(hu(:))]) + Cs_r*hu(:)';
%size(h)
%size(squeeze(zeta))
%size(hu)
%size(zu)
    if any(zu(:)~=0)
      z_u = z_u + scmCshc*[zu(:)./hu(:)]' + (1+Cs_r)*zu(:)';
    end
    grd.z_u = reshape(z_u,[N size(hu)]);
    clear z_u;

    % v-points
    hv = 0.5*(h(1:end-1,:)+h(2:end,:));
    zv = 0.5*(squeeze(zeta)(1:end-1,:)+squeeze(zeta)(2:end,:));
%size(hv)
%size(zv)
    z_v = repmat(scmCshc,[1 length(hv(:))]) + Cs_r*hv(:)';
    if any(zeta(:)~=0)
      z_v = z_v + scmCshc*[zv(:)./hv(:)]' + (1+Cs_r)*zv(:)';
    end
    grd.z_v = reshape(z_v,[N size(hv)]);
    
  end 
  grd.theta_s = theta_s;
  grd.theta_b = theta_b;
  grd.Tcline = Tcline;
  grd.N = N;
  grd.hc = hc;
  grd.sc_w = sc_w;
  grd.Cs_w = Cs_w;
  grd.sc_r = sc_r;
  grd.Cs_r = Cs_r;

end

