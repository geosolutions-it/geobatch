@echo off

set PROFILES=geotiff
rem profile needed for base demo (commons + geotiff + shapefiles)
rem set PROFILES=geotiff,shapefile,commons

rem profiles for base demo + task executor 
set PROFILES=geotiff,shapefile,task-executor,freemarker,scripting,commons,imagemosaic,ds2ds

rem other profiles unsupported yet
rem PROFILES="$PROFILES,xstream,shp2pg,geonetwork,geostore,octave,jmx,jms"

rem configure the command
set CMD=mvn clean install eclipse:clean eclipse:eclipse -P%PROFILES% -e %1%

%CMD%
