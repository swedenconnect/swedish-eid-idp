#!/usr/bin/env bash
#
# runDocker.sh
#   Example script for how to run the IdP in a Docker instance.
#
#   The settigs assume that we have built the project using:
#   > mvn clean install
#   > mvn dockerfile:build
#
#   And then we invoke scripts/runDocker.sh
#

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# Replace /cygdrive/c with c:/ (if running on Windows)
SCRIPT_DIR_WIN=`echo $SCRIPT_DIR | sed 's/\/cygdrive\/c/c:/g'`
# Remove /scripts
BASE_DIR=`echo $SCRIPT_DIR_WIN | sed 's/\/scripts//g'`

mkdir $BASE_DIR/target 2>/dev/null
mkdir $BASE_DIR/target/logs 2>/dev/null

docker run -d --name swedish-eid-idp --restart=always \
  -p 9160:8443 \
  -p 8099:8009 \
  -e IDP_SERVER_HOSTNAME=localhost \
  -e IDP_SERVER_PORT=9200 \
  -e IDP_ENTITY_ID=https://idp.svelegtest.se/idpref \
  -e IDP_FEDERATION_METADATA_URL=https://eid.svelegtest.se/metadata/feed \
  -e IDP_FEDERATION_METADATA_VALIDATION_CERT=/etc/swedish-eid-idp/credentials/trust/sveleg-metadata-validation-cert.crt \
  -e IDP_LOG_SETTINGS_FILE=/opt/swedish-eid-idp/shibboleth/conf/logback-devel.xml \
  -e IDP_LOG_CONSOLE=true \
  -e IDP_SEALER_PASSWORD=JeiferDRIoOplYy89 \
  -e IDP_PERSISTENT_ID_SALT=jkio98gbnmklop0Pr5WTvCgh \
  -v $BASE_DIR/test-credentials:/etc/swedish-eid-idp/credentials \
  -v $BASE_DIR/target/logs:/var/log/swedish-eid-idp \
  docker.eidastest.se:5000/swedish-eid-idp
    
docker logs -f swedish-eid-idp


