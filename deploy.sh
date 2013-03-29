# use this line to check for ${root.basedir} variable
# for i in `find . -name pom.xml`; do echo $i;grep root.basedir $i; done

#Then use this to generate a report
# ./report.sh 1.2.0 v1.3.0 TEST > build/report_`date "+%d_%m_%y"`.log

#rm build -rf
cd src

# generate sphinx doc
MVN_OPTS="-Xmx1024m" mvn clean pre-site -DskipTests=true $1 $2

# generate war and demo zip
MVN_OPTS="-Xmx1024m" mvn clean install -Pjetty -Dall $1 $2

# deploy the src/build/ directory to remote site 
MVN_OPTS="-Xmx1024m" mvn site:deploy -N -Dall $1 $2

cd ..

