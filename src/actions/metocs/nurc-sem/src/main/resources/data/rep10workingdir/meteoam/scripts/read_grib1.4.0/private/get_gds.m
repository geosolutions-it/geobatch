%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%% GET THE GDS STRUCTURE %%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function gds_struct=get_gds(fid,lengds)
gds=fread(fid,lengds);
gds_struct.len=lengds;
gds_struct.NV=gds(4);
gds_struct.PV=gds(5);
gds_struct.DRT=table6(gds(6));
if gds(6)==201
   error(['DRT ARAWAKA semi-staggered E-GRID grid type not yet supported.'])
end
if gds(6)==202
   error(['DRT ARAWAKA filled E-GRID grid type not yet supported.'])
end

% 01 Apr 2004: BOB GDS table D (Sundry Grid Definitions)

% switch gds_struct.DRT
%    case 'Lambert Conf.'
%       % For Lambert Conformal Grids
%       % GDS Octets 7-42
%       gds_struct.Nx=bitshift2(gds(7),gds(8));
%       gds_struct.Ny=bitshift2(gds(9),gds(10));
%       gds_struct.La1=int3(gds(11),gds(12),gds(13))/1000;
%       gds_struct.Lo1=int3(gds(14),gds(15),gds(16))/1000;
%       gds_struct.rcf=gds(17);
%       gds_struct.LOV=int3(gds(18),gds(19),gds(20))/1000;
%       gds_struct.Dx=int3(gds(21),gds(22),gds(23))/1000;
%       gds_struct.Dy=int3(gds(24),gds(25),gds(26))/1000;
%       gds_struct.Latin1=int3(gds(29),gds(30),gds(31))/1000;
%       gds_struct.Latin2=int3(gds(32),gds(33),gds(34))/1000;
%       gds_struct.Lat_of_SP=int3(gds(35),gds(36),gds(37))/1000;
%       gds_struct.Lon_of_SP=int3(gds(38),gds(39),gds(40))/1000;
% 
%  case {'Gaussian Lat/Lon' , 'Equidis. Cyl. Lat/Lon'}
%       % For Gaussian Grids and some others.
%       % GDS Octets 7-32
      gds_struct.Ni=bitshift2(gds(7),gds(8));
      gds_struct.Nj=bitshift2(gds(9),gds(10));
      gds_struct.La1=int3(gds(11),gds(12),gds(13))/1000;
      gds_struct.Lo1=int3(gds(14),gds(15),gds(16))/1000;
      gds_struct.rcf=gds(17);
      gds_struct.La2=int3(gds(18),gds(19),gds(20))/1000;
      gds_struct.Lo2=int3(gds(21),gds(22),gds(23))/1000;
      gds_struct.Di=bitshift2(gds(24),gds(25))/1000;
      gds_struct.N=bitshift2(gds(26),gds(27));
      gds_struct.smf=gds(28);
      gds_struct.oct29to32=gds(29:32);                                          

%    otherwise
%       error(['DRT ' int2str(gds(6)) ' not yet coded.'])
% end

gds_struct.gdsvals=gds;
