#!/bin/sh

# resolve absolute path of the running script
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

# go into the resolved dir
cd ${DIR}

# run geobatch
java -Xmx1024m -DGEOBATCH_CONFIG_DIR=${DIR}"/GEOBATCH_CONFIG_DIR" -classpath $CLASSPATH:${DIR}/geobatch/WEB-INF/lib/* it.geosolutions.geobatch.jetty.Start ${DIR}"/jetty.properties"

