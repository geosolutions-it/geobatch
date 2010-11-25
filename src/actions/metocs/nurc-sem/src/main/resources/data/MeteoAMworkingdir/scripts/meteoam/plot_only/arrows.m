function [h]=arrows(x,y,w,fac,color)
%
% arrows:          draws arrows
%
% Usage: [h]=arrows(x,y,w,fac,[color])
%
% where: h ....... the returned handle
%
%        (x,y) ... the coordinates where the arrow should be plotted
%        w ....... the velocity vector (complex)
%        fac ..... scaling factor for vector
%        color ... color of vector
%
% Acknowledgement: this function draws heavily on the "omviz" package
% written by Rich Signell (USGS)
%
% Initial version, JRH 11/12/2001
%
% Geometry of arrowheads (choosing HEADA and HEADL):
%
%  If the arrow is defined by the points A B C B D where A is the base of 
%  the arrow, B is the head, and C and D are the corners of the arrowhead, then
%  HEADA is the angle BAC (or BAD), and HEADL is the ratio of distances AC/AB.
%
HEADA=10*pi/180; HEADL=0.75;
%
z=x(:)+i*y(:);
%
if nargin < 4 | nargin > 5
  help arrows
elseif nargin < 5
  color='red';
end
%
w=w(:)*fac;
r=w*HEADL;
wr1=r*exp(+i*HEADA); 
wr2=r*exp(-i*HEADA);
%
wplot=ones(length(z),6); 
wplot(:,1)=z; 
wplot(:,[2,4])=(z+w)*ones(1,2);
wplot(:,3)=z+wr1; wplot(:,5)=z+wr2;
wplot(:,6)=z*nan;
wplot=wplot.';
wplot=wplot(:);
%
h=line(real(wplot),imag(wplot),'color',color);
set(h(1),'userdata',fac);
