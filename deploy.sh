#!/bin/bash
LAST_TAG="v1.3.0"

#rm build -rf
cd src

# generate sphinx doc
MVN_OPTS="-Xmx1024m" mvn clean pre-site -DskipTests=true $1 $2

# generate war and demo zip
MVN_OPTS="-Xmx1024m" mvn clean install -Pjetty -Dall $1 $2

cd ..

#Then use this to generate a report
#./report.sh v1.3.0 HEAD "Nightly report" > build/report_`date "+%d_%m_%y"`.log
BUILD_DIR=src/build/$(ls -t --group-directories-first src/build/ | head -1)
./report.sh ${LAST_TAG} HEAD "Nightly report" > $BUILD_DIR/report.log

cd src

# deploy the src/build/ directory to remote site 
MVN_OPTS="-Xmx1024m" mvn site:deploy -N -Dall $1 $2

cd ..

# OBSOLETE:
# use this line to check for ${root.basedir} variable
# for i in `find . -name pom.xml`; do echo $i;grep root.basedir $i; done
