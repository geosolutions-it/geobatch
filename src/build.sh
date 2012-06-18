#!/bin/bash

# supported actions
PROFILES="geotiff,shapefile,task-executor,freemarker,scripting,commons"

# unsupported
# PROFILES="$PROFILES,xstream,shp2pg,geonetwork,geostore,octave,jmx,jms"

# configure the command
CMD="mvn clean install eclipse:clean eclipse:eclipse \
 -DdownloadSources=true -DdownloadJavadocs=true -Declipse.addVersionToProjectName=true \
 -P${PROFILES} -e $1"

# run the command
$CMD

