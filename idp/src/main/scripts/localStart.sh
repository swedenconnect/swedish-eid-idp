#!/bin/bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Replace /cygdrive/c with c:/ (if running on Windows)
SCRIPT_DIR_WIN=`echo $SCRIPT_DIR | sed 's/\/cygdrive\/c/c:/g'`

# Remove /src/main/scripts
BASE_DIR_WIN=`echo $SCRIPT_DIR_WIN | sed 's/\/src\/main\/scripts//g'`

IDP_DEVEL_MODE=false

# Tomcat
TOMCAT_HOME=$BASE_DIR_WIN/target/dependency/apache-tomcat-8.5.23
CATALINA_HOME=$TOMCAT_HOME

# Home
IDP_HOME=$BASE_DIR_WIN/target/shibboleth
CREDENTIALS_BASE=$BASE_DIR_WIN/target/credentials

#
# Set up the IdP server URL
#
IDP_SERVER_SCHEME=https
IDP_SERVER_HOSTNAME=localhost
IDP_SERVER_PORT=9160
IDP_SERVER_PORT_SUFFIX=":${IDP_SERVER_PORT}"
IDP_SERVER_SERVLET_NAME=idp

if [ "$IDP_SERVER_SCHEME" == "https" ] && [ "$IDP_SERVER_PORT" == "443" ]; then
  IDP_SERVER_PORT_SUFFIX=""
fi
if [ "$IDP_SERVER_SCHEME" == "http" ] && [ "$IDP_SERVER_PORT" == "80" ]; then
  IDP_SERVER_PORT_SUFFIX=""
fi

IDP_BASE_URL=${IDP_SERVER_SCHEME}://${IDP_SERVER_HOSTNAME}${IDP_SERVER_PORT_SUFFIX}/${IDP_SERVER_SERVLET_NAME}
s
#
# Tomcat settings
#
TOMCAT_TLS_PORT=$IDP_SERVER_PORT
TOMCAT_AJP_PORT=8099
TOMCAT_HOSTNAME=$IDP_SERVER_HOSTNAME
TOMCAT_PROXY_NAME=${IDP_SERVER_HOSTNAME}
TOMCAT_TLS_KEYSTORE=$TOMCAT_HOME/conf/tls-test-localhost.jks
TOMCAT_TLS_KEYSTORE_PASSWORD=secret
TOMCAT_TLS_ALIAS=localhost

#
# IdP settings
#
IDP_ENTITY_ID=https://idp.svelegtest.se/idpref

IDP_CREDENTIALS=$IDP_HOME/credentials
IDP_SEALER_STORE_RESOURCE=$IDP_CREDENTIALS/sealer.jks
IDP_SEALER_PASSWORD=JeiferDRIoOplYy89
IDP_SEALER_VERSION_RESOURCES=$IDP_CREDENTIALS/sealer.kver
IDP_SIGNING_KEY=$IDP_CREDENTIALS/idp-signing.key
IDP_SIGNING_CERT=$IDP_CREDENTIALS/idp-signing.crt
IDP_ENCRYPTION_KEY=$IDP_CREDENTIALS/idp-encryption.key
IDP_ENCRYPTION_CERT=$IDP_CREDENTIALS/idp-encryption.crt
IDP_METADATA_SIGNING_KEY=$IDP_CREDENTIALS/metadata-signing.key
IDP_METADATA_SIGNING_CERT=$IDP_CREDENTIALS/metadata-signing.crt

IDP_PERSISTENT_ID_SALT=jkio98gbnmklop0Pr5WTvCgh

IDP_METADATA_VALIDITY_MINUTES=10800
IDP_METADATA_CACHEDURATION_MILLIS=3600000

#
# Metadata
#
IDP_METADATA_RESOURCES_BEAN=shibboleth.MetadataResolverResources

FEDERATION_METADATA_URL=https://eid.svelegtest.se/metadata/feed
FEDERATION_METADATA_VALIDATION_CERT=$IDP_HOME/metadata/metadata-validation-cert.crt

#
# Log settings
#

# Log settings may be overridden by setting IDP_LOG_SETTINGS_FILE to point to a logback include file. 
: ${IDP_LOG_SETTINGS_FILE:=""}

#
# Devel only
#

: ${IDP_DEVEL_MODE:=false}
DEVEL_TEST_SP_METADATA=https://localhost:8443/svelegtest-sp/metadata/all-metadata.xml

if [ "$IDP_DEVEL_MODE" == "true" ]; then
  IDP_METADATA_RESOURCES_BEAN="shibboleth.DevelMetadataResolverResources"
else
  IDP_DEVEL_MODE=false
fi

#
# JVM and JMX
#
: ${JVM_MAX_HEAP:=1536m}
: ${JVM_START_HEAP:=512m}
#: ${JMX_PORT:=9152}
#: ${JMX_ACCESS_FILE:=/etc/common-config/jmxremote.access}
#: ${JMX_PASSWORD_FILE:=/etc/common-config/jmxremote.password}

export JAVA_OPTS="\
          -Dtomcat.hostname=$TOMCAT_HOSTNAME \
          -Dtomcat.tls.port=$TOMCAT_TLS_PORT \
          -Dtomcat.ajp.port=$TOMCAT_AJP_PORT \
          -Dtomcat.tls.keystore=$TOMCAT_TLS_KEYSTORE \
          -Dtomcat.tls.password=$TOMCAT_TLS_KEYSTORE_PASSWORD \
          -Dtomcat.tls.alias=$TOMCAT_TLS_ALIAS \
          -Dtomcat.proxyname=$TOMCAT_PROXY_NAME \
          -Djava.net.preferIPv4Stack=true \
          -Didp.home=$IDP_HOME \
          -Didp.baseurl=$IDP_BASE_URL \
          -Didp.devel.mode=$IDP_DEVEL_MODE \
          -Didp.entityID=$IDP_ENTITY_ID \
          -Didp.sealer.storeResource=$IDP_SEALER_STORE_RESOURCE \
          -Didp.sealer.password=$IDP_SEALER_PASSWORD \
          -Didp.sealer.versionResource=$IDP_SEALER_VERSION_RESOURCES \
          -Didp.signing.key=$IDP_SIGNING_KEY \
          -Didp.signing.cert=$IDP_SIGNING_CERT \
          -Didp.encryption.key=$IDP_ENCRYPTION_KEY \
          -Didp.encryption.cert=$IDP_ENCRYPTION_CERT \
          -Didp.metadata.signing.key=$IDP_METADATA_SIGNING_KEY \
          -Didp.metadata.signing.cert=$IDP_METADATA_SIGNING_CERT \
          -Didp.metadata.validity=$IDP_METADATA_VALIDITY_MINUTES \
          -Didp.metadata.cacheDuration=$IDP_METADATA_CACHEDURATION_MILLIS \
          -Didp.persistentId.salt.value=${IDP_PERSISTENT_ID_SALT} \
          -Didp.metadata.federation.url=${FEDERATION_METADATA_URL} \
          -Didp.metadata.federation.validation-certificate=${FEDERATION_METADATA_VALIDATION_CERT} \
          -Didp.test.sp.metadata=${DEVEL_TEST_SP_METADATA} \
          -Didp.service.metadata.resources=${IDP_METADATA_RESOURCES_BEAN} \
          -Didp.log-settings.file=$IDP_LOG_SETTINGS_FILE \
          -Didp.consent.appender=NOOP_APPENDER \
          -Didp.warn.appender=NOOP_APPENDER \
          ${JAVA_OPTS}"


export CATALINA_OPTS="\
          -Xmx${JVM_MAX_HEAP}\
          -Xms${JVM_START_HEAP}\
"

echo "JAVA_OPTS=$JAVA_OPTS"
echo "CATALINA_OPTS=$CATALINA_OPTS"

export JPDA_ADDRESS=8788
export JPDA_TRANSPORT=dt_socket

if [ "$1" == "-d" ]; then
    echo "Running in debug"    
    $CATALINA_HOME/bin/catalina.sh jpda run
else
    $CATALINA_HOME/bin/catalina.sh run
fi



