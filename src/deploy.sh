# use this line to check for variable
# for i in `find . -name pom.xml`; do echo $i;grep root.basedir $i; done

MVN_OPTS="-Xmx1024m" mvn site install site:deploy -Pjetty -DskipTests=true -Dall $1 $2
