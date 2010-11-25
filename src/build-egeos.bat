@echo off
echo **************** E-GEOS Build ****************
mvn eclipse:clean eclipse:eclipse clean install -Pdao.xstream,metocs,e-geos,emsa,registry -Dmaven.test.skip -o