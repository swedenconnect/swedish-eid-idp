#!/usr/bin/env bash
#
# runStandalone.sh [-d]
#   Example script for how to run the IdP in stand-alone mode against a distribution
#

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# Replace /cygdrive/c with c:/ (if running on Windows)
SCRIPT_DIR_WIN=`echo $SCRIPT_DIR | sed 's/\/cygdrive\/c/c:/g'`
# Remove /scripts
BASE_DIR=`echo $SCRIPT_DIR_WIN | sed 's/\/scripts//g'`

export IDP_CREDENTIALS=$BASE_DIR/test-credentials

export IDP_SERVER_HOSTNAME=localhost
export IDP_SERVER_PORT=9160

export IDP_TOMCAT_TLS_PORT=$IDP_SERVER_PORT
export IDP_TOMCAT_AJP_PORT=8099

export IDP_ENTITY_ID=https://idp.svelegtest.se/idpref

export IDP_SEALER_PASSWORD=JeiferDRIoOplYy89
export IDP_PERSISTENT_ID_SALT=jkio98gbnmklop0Pr5WTvCgh

export IDP_FEDERATION_METADATA_URL=https://eid.svelegtest.se/metadata/feed
export IDP_FEDERATION_METADATA_VALIDATION_CERT=$IDP_CREDENTIALS/trust/sveleg-metadata-validation-cert.crt

export IDP_LOG_SETTINGS_FILE=$BASE_DIR/shibboleth/conf/logback-devel.xml


"$SCRIPT_DIR"/standaloneStart.sh $BASE_DIR $1
