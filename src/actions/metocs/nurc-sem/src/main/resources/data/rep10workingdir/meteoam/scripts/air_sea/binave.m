function bindat = binave(data,r)
% BINAVE: averages vector data in bins of length r.
% bindat=BINAVE(data,r) computes an average vector of the vector 
% data in bins of length r.  The last bin may be the average of 
% less than r elements. Useful for computing daily average time 
% series (with r=24 for hourly data).
%
% INPUT:   data - data vector
%          r - number of elements in bin to be averaged
%
% OUTPUT:  bindat - bin-averaged vector

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% 3/8/97: version 1.0
% 9/19/98: version 1.1 (vectorized by RP)
% 8/5/99: version 2.0
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

% check input
if nargin < 2
   error('Not enough input arguments.')
end
if abs(r-fix(r)) > eps
   error('Bin size R must be a positive integer.')
end
if fix(r) == 1
	bindat = data;
	return
end
if r <= 0
	error('Bin size R must be a positive integer.')
end

[N,M]=size(data);

% compute bin averaged series
l = length(data)/r;
l = fix(l);
bindat = mean(reshape(data(1:l*r),r,l));

if length(data)>l*r,
 bindat=[bindat,mean(data(l*r+1:end))];
end;

if N~=1
  bindat=bindat';
end



