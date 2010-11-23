function c = reedcf(yd,lat,dsw);
% REEDCF: computes daily mean cloud cover following Reed (1977).
% c = REEDCF(yd,lat,dsw) computes daily averaged cloud cover c from
% yearday, latitude, and measured insolation following Reed (1977), J. Phys. 
% Oceanog., 7, 482-485. Assumes hourly input series are either both 
% column or both row vectors of equal length. c is output as a boxcar 
% function with c constant over a 24 hr period.
%
%   INPUT:  yd  - yearday (e.g., Jan 10 is yd=10)
%           lat - latitude  [deg]
%           dsw - (measured) insolation  [W/m^2] 
%
%   OUTPUT: c - daily averaged cloud cover

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% determine if input is either row or column vectors
[m,n] = size(dsw);

% first daily average swr and yd
davesw = binave(dsw,24);
daveyd = binave(yd,24);

% estimate daily averaged cloud factor
c=cloudfac(daveyd,lat,davesw);

% cloudfac always outputs a column vector, if initial input is
% row vector, convert cf to row vector
if m==1
  c  = c';
end

% convert daily averaged cloudfactor to boxcar function
if n== 1
  c = c*ones(1,24);
  c = reshape(c',prod(size(c)),1);

elseif m==1
  c = ones(24,1)*c;
  c = reshape(c,1,prod(size(c)));
end
c(length(dsw)+1:end)=[];


function cf=cloudfac(yd,lat,davesw)
% CLOUDFAC: computes daily mean cloud factor following Reed (1977).
% cf=CLOUDFAC(yd,lat,davesw) computes the daily average cloud factor 
% based on Reed (1977), J. Phys. Ocean., 7, 482-485.  Assumes year is 
% not a leap year. If yd is negative, then yd=yd+365. 
%
%   INPUT:  yd - yearday (e.g., Jan 10th is 10)
%           lat - latitude [deg]
%           davesw - average measured insolation  [W/m^2]
%
%   OUTPUT: cf - daily average cloud factor 

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/5/99: version 2.0 
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% convert to column vectors
[N,M]=size(davesw);
if N==1
  davesw=davesw';
  yd=yd';
end

% check if yd is negative
ind=find(yd<0);
yd(ind)=yd(ind)+365;
% truncate to integer yearday for use with formula
yd=fix(yd);

% determine noon solar altitude
nsa=nsunang(yd,lat);

% compute ratio of observed to clear sky insolation
cssw=clskswr(yd,lat);
QdQ=davesw./cssw;

% correct for measurement error

ind1=find(QdQ>1);
QdQ(ind1)=ones(length(ind1),1);

% compute cloud factor
cf=(1-QdQ+.0019.*nsa)./.62;

% truncate limits

ind2=find(cf>1);
cf(ind2)=ones(length(ind2),1);
ind3=find(cf<0.3);
cf(ind3)=zeros(length(ind3),1);

% reconvert if necessary
if N==1
  cf=cf';
end


function nsa=nsunang(yd,lat)
% NSUNANG: computes noon solar altitude angle.
% nsa=NSUNANG(yd,lat) computes the noonday solar altitude angle as a 
% function of yearday and latitude using Smithsonian table (see 
% Reed (1976), NOAA Tech Memo., ERL PMEL-8, 20 pp., or Reed (1977), 
% J. Phys. Ocean., 7, 482-485).
%
%  INPUT:  yd  - yearday (e.g., Jan 10 is 10)
%          lat - latitude  [deg]
%
%  OUTPUT: nsa - noonday solar altitude  [deg]

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% ensure that yd is positive
ind=find(yd<0);
yd(ind)=yd(ind)+365;
yd=fix(yd);

% compute noon solar altitude
k=pi./180;
t=2.*pi.*(yd./365);
d=.397+3.630*sin(t)-22.98.*cos(t)+.040.*sin(2.*t)-.388.*cos(2.*t)...
             +.075.*sin(3.*t)-.160.*cos(3.*t);
dl=k.*d;
ll=k.*lat;
z=sin(ll).*sin(dl)+cos(ll).*cos(dl);
nsa=asin(z)./k;
