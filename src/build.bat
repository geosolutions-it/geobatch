@echo off
rem supported actions
set PROFILES=geotiff,shapefile,task-executor,freemarker,scripting,commons

rem unsupported
rem PROFILES="$PROFILES,xstream,shp2pg,geonetwork,geostore,octave,jmx,jms"
rem configure the command

set CMD=mvn clean install eclipse:clean eclipse:eclipse -P%PROFILES% -e %1%

%CMD%
