%
%
%

tindex=fix(julian(clock));

adate = gregorian(tindex);
rfile=datestr(adate,24);
rfile(6)=[]; rfile(3)=[];
rfile=[rfile(5) rfile(6) rfile(7) rfile(8) rfile(3) rfile(4) rfile(1) rfile(2)];
metfile = ['/home/chiggiato/Ligurian/Surface/Archive/','elm_',rfile,'00.nc'];

ncload(metfile);
load med_coastline.dat;

w=complex(U10,V10);

for i=1:3:73;
    if(i>1 & i<11); aa=num2str(i-1,'0%1d'); elseif(i==1); aa='00'; else; aa=num2str(i-1,'%2d'); end;
ah=figure('visible','off');
pcolor(lon,lat,squeeze(atemp(i,:,:))); shading interp; 
axis([5.5 13.5 40.5 45.5]);
hold on; xlabel('Longitude E'); ylabel('Latitude N'); 
plot(med_coastline(:,1),med_coastline(:,2),'k');
caxis([0 40]); load cosmocmap; colormap(cosmocmap); colorbar; dasp(43);
arrows(lon(1:3:end,1:3:end),lat(1:3:end,1:3:end),squeeze(w(i,1:3:end,1:3:end)),0.05,[0 0 0]);
text(11,45.35,'10 m/s');
arrows(11,45.25,complex(10,0),0.05,[0. 0. 0.]); 
str1=['COSMO-ME Forecast :',datestr(datenum(gregorian(time(i)+2440000)))]; str2=['10 m Wind (m/s) - 2 m Air Temp (degC) - MSLP (mb)'];
title(sprintf('%s\n%s',str1,str2));
[cs,bh]=contour(lon,lat,squeeze(apress(i,:,:)),[980:2:1030]'); set(bh,'linewidth',1.5);
format long G;
ht=clabel(cs,bh,'labelspacing',200,'fontsize',8,'color',[0 0 0],'backgroundcolor',[1 1 1],'rotation',0);
set(gcf,'renderer','zbuffer');
plotname1=strcat('/home/chiggiato/Ligurian/Surface/Plot/',rfile,'00.0',aa,'.ps');
plotname2=strcat('/home/chiggiato/Ligurian/Surface/Plot/',rfile,'00.0',aa,'_small.ps');
print('-painters','-dpsc','-cmyk','-r300',plotname1);
print('-painters','-dpsc','-cmyk','-r300',plotname2);
close;
end
exit
%
