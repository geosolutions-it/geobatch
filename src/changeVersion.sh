#!/bin/bash

# Copyright (C) 2011 GeoSolutions - http://geo-solutions.it
#
# Author: Carlo Cancellieri
#
#This library is free software; you can redistribute it and/or
#modify it under the terms of the GNU Lesser General Public
#License as published by the Free Software Foundation; either
#version 2.1 of the License, or (at your option) any later version.
#
#This library is distributed in the hope that it will be useful,
#but WITHOUT ANY WARRANTY; without even the implied warranty of
#MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#Lesser General Public License for more details.
#
#You should have received a copy of the GNU Lesser General Public
#License along with this library; if not, write to the Free Software
#Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
#

usage(){
 echo "USAGE: $0 {-n|-b|-c} <BASE_DIR> <NEW_VERSION> \n\
\t -n dry run\n\
\t -b run (perform backup)\n\
\t -c run (no backup)"
}

if [ $# -lt 3 ]; then
	usage;
fi


if [ -d $2 ]; then
	BASE_DIR=$2;
else
	echo "2nd argument is wrong (shoould be an existing dir): $3"
	usage;
	exit 0;
fi

NEW_VERSION=$3;
XML_COMMAND=xmlstarlet

XML_COMMAND_ARGS="ed -N x=http://maven.apache.org/POM/4.0.0 \
-u \"/x:project/x:version | /x:project/x:parent/x:version\" \
-v \"$NEW_VERSION\"";

 if [ $1 = "-n" ]; then
	for pom in `find $BASE_DIR -name pom.xml`; do 
#		echo $XML_COMMAND $XML_COMMAND_ARGS $pom;
		echo $XML_COMMAND_ARGS $pom|xargs $XML_COMMAND;
		if [ $? -ne 0 ]; then
			break;
		fi
	done
 elif [ $1 = "-b" ]; then
	for pom in `find $BASE_DIR -name pom.xml`; do 
	 	cp -fp $1 $1".old";
		echo $XML_COMMAND_ARGS $pom" > "$pom|xargs $XML_COMMAND;
		if [ $? -ne 0 ]; then
			break;
		fi
	done
 elif [ $1 = "-c" ]; then
	for pom in `find $BASE_DIR -name pom.xml`; do 
		echo $XML_COMMAND_ARGS $pom" > "$pom|xargs $XML_COMMAND;
		if [ $? -ne 0 ]; then
			break;
		fi
	done
 else
	echo "1st argument is wrong: $1"
	usage
	exit 0;
 fi
 return 1


