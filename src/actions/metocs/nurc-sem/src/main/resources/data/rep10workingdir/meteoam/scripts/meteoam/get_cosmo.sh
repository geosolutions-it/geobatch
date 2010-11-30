#!/bin/sh

. /etc/profile
. ~/.profile

date

# Getting the date

yy="$(date --date -0days +%Y)"
mm="$(date --date -0days +%m)"
dd="$(date --date -0days +%d)"

dirday="$yy""$mm""$dd"

# now ftping...
dirtoday="/home/chiggiato/Ligurian/Surface/""$dirday"

mkdir $dirtoday
cd $dirtoday
pftp -n geos2.nurc.nato.int << EOF
user jchiggiato jjcc09nu
cd rep10/rep10data/FTP/METEOAM/COSMO
bin
prompt
mget "ELM_""$dirday""00.tar.bz2"
mget "ILM_""$dirday""00.tar.bz2"
mget "NETTUNO_CNMCA_""$dirday""00.tar.bz2"
bye
EOF

cd $dirtoday

tar xf ELM*
tar xf ILM*
tar xf NETTUNO*

cd /home/chiggiato/Ligurian/Surface
/usr/local/bin/matlab -nojvm -nosplash -nodisplay < master_grib2nc_00.m 2>&1 > out1.log
/usr/local/bin/matlab -nojvm -nosplash -nodisplay < make_cosmo_frc.m 2>&1 > out2.log
/usr/local/bin/matlab -nojvm -nosplash -nodisplay < plot_cosmo.m 2>&1 > out3.log

cd /home/chiggiato/Ligurian/Surface/Plot
mogrify -density 640x640 -quality 100 -geometry 15% -colors 256 -format png *.ps
rm *ps
rm *small*

pftp -n geos2.nurc.nato.int << EOF
user jchiggiato jjcc09nu
cd rep10/rep10data/meteomod/cosmo
mkdir $dirday
cd $dirday
bin
prompt
mput *.png
bye
EOF

rm /home/chiggiato/Ligurian/Surface/Plot/*.png

cd $dirtoday 
rm *grb

cd /home/chiggiato/Ligurian/Surface/Archive
gzip *

# now ftping...
lftp -u jacopo,ligurian+sea410 sftp://gher-diva.phys.ulg.ac.be << EOF
cd COSMO-Forecast
bin
put "cosmo_""$dirday""_frc.nc.gz"
bye
EOF

date
