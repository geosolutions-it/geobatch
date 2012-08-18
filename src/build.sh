#!/bin/bash

#profile needed for base demo (commons + geotiff + shapefiles)
PROFILES="geotiff,shapefile,commons"

#profiles for base demo + task executor 
#PROFILES="geotiff,shapefile,task-executor,freemarker,scripting,commons"

# other profiles unsupported yet
# PROFILES="$PROFILES,xstream,shp2pg,geonetwork,geostore,octave,jmx,jms"

# configure the command
CMD="mvn clean install eclipse:clean eclipse:eclipse \
 -DdownloadSources=true -DdownloadJavadocs=true -Declipse.addVersionToProjectName=true \
 -P${PROFILES} -e $1"

# run the command
$CMD

