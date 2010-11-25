@echo off
echo **************** NURC (SEM/TDA) Build ****************
mvn eclipse:clean eclipse:eclipse clean install -Pdao.xstream,metocs,nurc -Dmaven.test.skip -o