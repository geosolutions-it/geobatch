mvn clean install eclipse:clean eclipse:eclipse  -DdownloadSources=true  -DdownloadJavadocs=true  -Declipse.addVersionToProjectName=true -Pdao.xstream -Dall -DskipTests=true -e $1
