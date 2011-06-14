#!/bin/bash
###################################################
# usage
function usage {
echo -e "
  USAGE:\n\
    $0 -s GEOBATCH_SOURCE_DIR ... [OPTION VALUE]...\n\
  \n\
  OPTIONS\n\
  [-s] 	where to get the geobatch sources\n\
	ACCEPTED VALUE: [default: './src/']\n\
	  Any valid path pointing to the GB you want to use as source dir\n\
\n\
  [-d]	where to put the project\n\
	ACCEPTED VALUE: [default: './out/']\n\
\n\
	  Any valid path pointing to a directory\n\
  [-b]  The version of GeoBatch to use \n\
	ACCEPTED VALUE: [default: '1.0']\n\
	  1.0 or 1.1-SNAPSHOT\n\
\n\
  [-n]	The application name\n\
	ACCEPTED VALUE: [default: 'my_application']
	  Any valid mvn project name (lowercase '_' or '-' separed)\n\
\n\
  [-v]	The application project version\n\
	ACCEPTED VALUE: [default: '0.1-SNAPSHOT']\n\
	  A valid version (mvn project application version)\n\
  \n\
  [-a]	The name of the action to prepare\n\
	ACCEPTED VALUE: [default: 'MyAction']\n\
	  Any valid java class name (CamelCase format)\n";
return;
}

###################################################
# Variables
# where to get the geobatch sources [-s]
GB_DIR="./src/";
# Version of GeoBatch to use
#	 1.0 or 1.1-SNAPSHOT [-b]
GB_VER="1.0"; #"1.1-SNAPSHOT"

# where to put the project [-d]
DEST_DIR="./out/"

# The application name [-n]
NAME_APP="my_application"
# The application project version [-v]
PROJ_VER="0.1-SNAPSHOT"
# The name of the action to prepare [-a]
NAME_ACT="MyAction"
# used for folder and pom.xml stuff
NAME_ACT_LOWER=$(echo $NAME_ACT | tr "[:upper:]" "[:lower:]")
###################################################

###################################################
# checking arguments
args=("${@}");
#for arg in $@; do
for ((i=0; i<$#; i++)){
  arg=${args[$i]};
  case $arg in
  "-s")
	  echo "Setting Geobatch Source dir (-s)"
	  GB_DIR=${args[$((++i))]};
	  if [ -d "$GB_DIR" ]; then
	    echo "GeoBatch source dir set to-> $GB_DIR"
	  else
	    echo -e "ERROR:\n Bad GeoBatch source directory! ($GB_DIR)"
	    usage;
	    exit -1;
	  fi
	  ;;
  "-b")
	  echo "Setting Geobatch Source version (-b)"
	  GB_VER=${args[$((++i))]};
	  case $GB_VER in
	  "1.0")
		  ;;
	  "1.1-SNAPSHOT")
		  ;;
	  *)
	    echo -e "ERROR:\n Bad GeoBatch source variable! ($GB_VER)"
	    usage;
	    exit -1;
	  esac
	  echo "GeoBatch source dir set to-> $GB_VER"
	  ;;
  "-d")
	  echo "Setting Application destination dir (-d)"
	  DEST_DIR=${args[$((++i))]};
	  if [ -d "$DEST_DIR" ]; then
	    echo "Application destination directory set to-> $DEST_DIR"
	  else
	    echo -e "ERROR:\n Bad Application destination directory! ($DEST_DIR)"
	    usage;
	    exit -1;
	  fi
	  ;;
  "-n")
	  echo "Setting Application name (-n)"
	  # set name application to lowercase
	  NAME_APP=$(echo ${args[$((++i))]} | tr "[:upper:]" "[:lower:]")
	  case $NAME_APP in
	  -*)
	      echo -e "ERROR:\n Bad application name! ($NAME_APP)"
	      usage;
	      exit -1;  
	      ;;
	  *)
	      echo "Application name set to-> $NAME_APP"
	  esac
	  ;;
  "-v")
	  echo "Setting Application project version (-v)"
	  PROJ_VER=${args[$((++i))]}
	  case $PROJ_VER in
	  -*)
	      echo -e "ERROR:\n Bad application project version ! ($PROJ_VER)"
	      usage;
	      exit -1;  
	      ;;
	  *)
	      echo "Application project version set to-> $PROJ_VER"
	  esac
	  ;;
  "-a")
	  echo "Setting Action name (-a)"
	  # set name first character to Uppercase (to respect CamelCase format)
	  NAME_ACT=${args[$((++i))]:0:1}
	  case $NAME_ACT in
	  -*)
	      echo -e "ERROR:\n Bad action name! ($NAME_ACT)"
	      usage;
	      exit -1;  
	      ;;
	  *)
	      echo "Action name set to-> $NAME_ACT"
	  esac
	  # used for folder and pom.xml stuff
	  NAME_ACT_LOWER=$(echo $NAME_ACT | tr "[:upper:]" "[:lower:]")
	  ;;
  "--help")
	  usage;
	  exit -1;
	  ;;
  "-h")
	  usage;
	  exit -1;
	  ;;
  *)
	  echo "Default: calling usage ($arg)"
	  usage;
	  exit -1;
	  ;;
  esac
}

##############################################################################
# REPLACE pom FUNCTION
##################
#KEYS (do not modify)
GB_VER_KEY="##GB_VER##"
NAME_APP_KEY="##NAME_APP##"
PROJ_VER_KEY="##PROJ_VER##"
NAME_ACT_KEY="##NAME_ACT##"
function replace_pom {
cat "$1" | \
  awk '{if ($0 ~ /.*'"$GB_VER_KEY"'.*/)  { gsub(/'"$GB_VER_KEY"'/,"'"$GB_VER"'",$0);} \
  if ($0 ~ /.*'"$NAME_APP_KEY"'.*/) { gsub(/'"$NAME_APP_KEY"'/,"'"$NAME_APP"'",$0);} \
  if ($0 ~ /.*'"$PROJ_VER_KEY"'.*/){ gsub(/'"$PROJ_VER_KEY"'/,"'"$PROJ_VER"'",$0);} \
  if ($0 ~ /.*'"$NAME_ACT_KEY"'.*/){ gsub(/'"$NAME_ACT_KEY"'/,"'"$NAME_ACT_LOWER"'",$0);} \
  { print $0}}' > "$2"
}

##############################################################################
# REPLACE replace_application_pom FUNCTION
##################
#VARIABLEs (do not modify)
APP_START_DELIM="<!-- START APPLICATION DESCRIPTION -->"
APPLICATION_DES="\
	<parent>\n\
		<groupId>it.geosolutions.geobatch.$NAME_APP</groupId>\n\
		<artifactId>gb-$NAME_APP</artifactId>\n\
		<version>$PROJ_VER</version>\n\
	</parent>\n\
	<!-- =========================================================== -->\n\
	<!-- Module Description -->\n\
	<!-- =========================================================== -->\n\
	<groupId>it.geosolutions.geobatch.$NAME_APP</groupId>\n\
	<artifactId>gb-application-$NAME_APP</artifactId>\n\
	<packaging>war</packaging>\n"
APP_STOP_DELIM="<!-- STOP APPLICATION DESCRIPTION -->"
ACTION_PROFILE="\
		<!--<profile>\n\
			<id>$NAME_ACT_LOWER</id>\n\
			<activation>\n\
				<property>\n\
					<name>all</name>\n\
					<value>true</value>\n\
				</property>\n\
			</activation>\n\
			<dependencies>\n\
				<dependency>\n\
					<groupId>it.geosolutions.geobatch.$NAME_APP</groupId>\n\
					<artifactId>gb-action-$NAME_APP-$NAME_ACT_LOWER</artifactId>\n\
				</dependency>\n\
			</dependencies>\n\
			<build>\n\
				<plugins>\n\
					<plugin>\n\
						<groupId>org.apache.maven.plugins</groupId>\n\
						<artifactId>maven-dependency-plugin</artifactId>\n\
						<executions>\n\
							<execution>\n\
								<id>unpack_$NAME_ACT_LOWER</id>\n\
								<phase>package</phase>\n\
								<goals>\n\
									<goal>unpack</goal>\n\
								</goals>\n\
								<configuration>\n\
									<artifactItems>\n\
										<artifactItem>\n\
											<groupId>it.geosolutions.geobatch.$NAME_APP</groupId>\n\
											<artifactId>gb-action-$NAME_APP-$NAME_ACT_LOWER</artifactId>\n\
											<classifier>flowdata</classifier>\n\
											<type>jar</type>\n\
											<overWrite>false</overWrite>\n\
											<outputDirectory>\${project.build.directory}/\${flow.dir}</outputDirectory>\n\
											<includes>data/**</includes>\n\
										</artifactItem>\n\
									</artifactItems>\n\
									<overWriteReleases>true</overWriteReleases>\n\
									<overWriteSnapshots>true</overWriteSnapshots>\n\
								</configuration>\n\
							</execution>\n\
						</executions>\n\
					</plugin>\n\
				</plugins>\n\
			</build>\n\
		</profile>-->\n"
function replace_application_pom {
cat "$1" | awk 'BEGIN{appl_des=0;profile=0;}\
		/'"$APP_START_DELIM"'/,/'"$APP_STOP_DELIM"'/{if (appl_des==0){appl_des=1;}; next}\
		{if (appl_des==1){print "'"$APPLICATION_DES"'"; appl_des=0;} else {print $0}}\
		/<profiles>/,/<\/profiles>/{if (profile==0){print "'"$ACTION_PROFILE"'"; profile=1; }}' > "$2"
}
################
# PROJECT FOLDER(s) STRUCTURE
################
mkdir -p "$DEST_DIR/src"

replace_pom ".build/src-pom.xml" "$DEST_DIR/src/pom.xml" -p
cp ".build/build.sh" "$DEST_DIR/src/build.sh" -p
# copy webapp
cp "$GB_DIR/gb-application" "$DEST_DIR/src/gb-application-$NAME_APP" -Rp
#cp ".build/application-pom.xml" "$DEST_DIR/src/gb-application-$NAME_APP/pom.xml" -p
replace_application_pom "$GB_DIR/gb-application/pom.xml" "$DEST_DIR/src/gb-application-$NAME_APP/pom.xml"

#################################################################################################
# ACTION(s) FOLDER STRUCTURE
################
mkdir "$DEST_DIR/src/gb-actions-$NAME_APP" -p
replace_pom ".build/actions-pom.xml" "$DEST_DIR/src/gb-actions-$NAME_APP/pom.xml"

#################################################################################################
# function replace_java
# replace all the KEYS into java sources
function replace_java {
  cat "$1" | \
    awk '{if ($0 ~ /.*package.*/) { gsub(/'"$NAME_ACT_KEY"'/,"'"$NAME_ACT_LOWER"'",$0);} \
    if ($0 ~ /.*'"$NAME_APP_KEY"'.*/) { gsub(/'"$NAME_APP_KEY"'/,"'"$NAME_APP"'",$0);} \
    if ($0 ~ /.*'"$NAME_ACT_KEY"'.*/){ gsub(/'"$NAME_ACT_KEY"'/,"'"$NAME_ACT"'",$0);} \
    { print $0}}' > "$2"
}

#################################################################################################
# ACTION SOURCES
################
#remove double slash
ACTION_DIR="$DEST_DIR/src/gb-actions-$NAME_APP/$NAME_ACT_LOWER/"
mkdir -p "$ACTION_DIR"

#place pom replacing keys
replace_pom ".build/action/pom.xml" "$ACTION_DIR/pom.xml"

# build package structure
ACTION_SOURCE_DIR="$ACTION_DIR/src/main/java/it/geosolutions/geobatch/$NAME_APP/$NAME_ACT_LOWER/"
mkdir -p "$ACTION_SOURCE_DIR"

# copy all file templates replacing keys
for java in `ls .build/action/src/main/java/`; do
  replace_java ".build/action/src/main/java/$java" "$ACTION_SOURCE_DIR/$NAME_ACT$java"
done


#################################################################################################
# function replace_configuration
# replace all the KEYS into the flow configuration
NAME_ACT_LOWER_KEY="##NAME_ACT_LOWER##"
function replace_resources {
  cat "$1" | \
    awk '{if ($0 ~ /.*'"$NAME_ACT_LOWER_KEY"'.*/) { gsub(/'"$NAME_ACT_LOWER_KEY"'/,"'"$NAME_ACT_LOWER"'",$0);} \
    if ($0 ~ /.*'"$NAME_APP_KEY"'.*/) { gsub(/'"$NAME_APP_KEY"'/,"'"$NAME_APP"'",$0);} \
    if ($0 ~ /.*'"$NAME_ACT_KEY"'.*/){ gsub(/'"$NAME_ACT_KEY"'/,"'"$NAME_ACT"'",$0);} \
    { print $0}}' > "$2"
}
################
# ACTION RESOURCES
################
ACTION_RESOURCE_DIR="$ACTION_DIR/src/main/resources/"
mkdir -p "$ACTION_RESOURCE_DIR"

# copy and filter application context
replace_resources ".build/action/src/main/resources/applicationContext.xml" "$ACTION_RESOURCE_DIR""applicationContext.xml"

mkdir -p "$ACTION_RESOURCE_DIR/data/$NAME_ACT_LOWER/in/"
touch "$ACTION_RESOURCE_DIR/data/$NAME_ACT_LOWER/in/.placeholder"

# copy and filter configuration
replace_resources ".build/action/src/main/resources/data/Flow.xml" "$ACTION_RESOURCE_DIR/data/$NAME_ACT_LOWER.xml"

################
# ACTION TESTS
################
mkdir -p "$ACTION_DIR/src/test/java/it/geosolutions/geobatch/$NAME_ACT_LOWER/test"

#build
#mvn eclipse:clean eclipse:eclipse  -DdownloadSources=true  -DdownloadJavadocs=true  -Declipse.addVersionToProjectName=true -Pdao.xstream,netcdf2geotiff -Dall -Dmaven.test.skip=true -e -o
echo -e "\
\nApplication $NAME_APP SUCCESFULLY build!!!\n\
\nIf you want to activate the action ($NAME_ACT) you still have to uncomment:\n\
\t- Action dependency version into the dependency management of the $DEST_DIR/src/pom.xml\n\
\t- Action profile into the profiles node of the $DEST_DIR/src/gb-actions-$NAME_APP/pom.xml\n\
\t- Action profile into the profiles node of the $DEST_DIR/src/gb-application-$NAME_APP/pom.xml\n"
