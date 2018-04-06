#!/usr/bin/env bash

: ${IDP_APP_NAME:=swedish-eid-idp}

mkdir -p /var/log/$IDP_APP_NAME

# Source this app's config
#
for i in /etc/${IDP_APP_NAME}/*.conf; do
    echo sourcing $i
    . ${i}
done

: ${IDP_ROOT:=/opt/$IDP_APP_NAME}
: ${IDP_HOME:=$IDP_ROOT/shibboleth}
: ${IDP_TOMCAT_HOME:=$IDP_ROOT/tomcat}

: ${IDP_CREDENTIALS:=/etc/$IDP_APP_NAME/credentials}

#
# Setup IdP URL
#
: ${IDP_SERVER_SCHEME:=https}
: ${IDP_SERVER_HOSTNAME:=tools.swedenconnect.se}
: ${IDP_SERVER_PORT:=443}
: ${IDP_SERVER_SERVLET_NAME:=idp}

IDP_SERVER_PORT_SUFFIX=":${IDP_SERVER_PORT}"

if [ "x$IDP_SERVER_SCHEME" = "xhttps" -a "x$IDP_SERVER_PORT" = "x443" ]; then
  IDP_SERVER_PORT_SUFFIX=""
fi

if [ "x$IDP_SERVER_SCHEME" = "xhttp" -a "x$IDP_SERVER_PORT" = "x80" ]; then
  IDP_SERVER_PORT_SUFFIX=""
fi

IDP_BASE_URL=${IDP_SERVER_SCHEME}://${IDP_SERVER_HOSTNAME}${IDP_SERVER_PORT_SUFFIX}/${IDP_SERVER_SERVLET_NAME}

#
# Tomcat settings
#
: ${IDP_TOMCAT_TLS_PORT:=8443}
: ${IDP_TOMCAT_AJP_PORT:=8009}
: ${IDP_TOMCAT_HOSTNAME:=localhost}
: ${IDP_TOMCAT_PROXY_NAME:=$IDP_SERVER_HOSTNAME}

: ${IDP_TOMCAT_TLS_KEYSTORE:=$IDP_CREDENTIALS/tomcat/localhost-snakeoil.p12}
: ${IDP_TOMCAT_TLS_KEYSTORE_TYPE:=pkcs12}
: ${IDP_TOMCAT_TLS_KEYSTORE_PASSWORD:=changeit}
: ${IDP_TOMCAT_TLS_ALIAS:=localhost}

if [ ! -f "$IDP_TOMCAT_TLS_KEYSTORE" ]; then
  echo "Tomcat keystore - $IDP_TOMCAT_TLS_KEYSTORE - does not exist" >&2
  exit 1
fi

#
# IdP settings
#
: ${IDP_ENTITY_ID:=http://$IDP_SERVER_HOSTNAME/refidp}

: ${IDP_SEALER_STORE_RESOURCE:=$IDP_CREDENTIALS/sealer.jks}
: ${IDP_SEALER_PASSWORD:=changeme}
: ${IDP_SEALER_VERSION_RESOURCES:=$IDP_CREDENTIALS/sealer.kver}
: ${IDP_SIGNING_KEY:=$IDP_CREDENTIALS/idp-signing.key}
: ${IDP_SIGNING_CERT:=$IDP_CREDENTIALS/idp-signing.crt}
: ${IDP_ENCRYPTION_KEY:=$IDP_CREDENTIALS/idp-encryption.key}
: ${IDP_ENCRYPTION_CERT:=$IDP_CREDENTIALS/idp-encryption.crt}
: ${IDP_METADATA_SIGNING_KEY:=$IDP_CREDENTIALS/metadata-signing.key}
: ${IDP_METADATA_SIGNING_CERT:=$IDP_CREDENTIALS/metadata-signing.crt}

: ${IDP_PERSISTENT_ID_SALT:=this_needs_to_be_supplied}

# Verification that all IdP credentials are in place ...
if [ ! -f "$IDP_SEALER_STORE_RESOURCE" ]; then
	echo "$IDP_SEALER_STORE_RESOURCE does not exist" >&2
    exit 1
fi
if [ ! -f "$IDP_SIGNING_KEY" ]; then
	echo "IdP signature key - $IDP_SIGNING_KEY - does not exist" >&2
  exit 1
fi
if [ ! -f "$IDP_SIGNING_CERT" ]; then
	echo "IdP signature certificate - $IDP_SIGNING_CERT - does not exist" >&2
  exit 1
fi
if [ ! -f "$IDP_ENCRYPTION_KEY" ]; then
	echo "IdP encryption key - $IDP_ENCRYPTION_KEY - does not exist" >&2
  exit 1
fi
if [ ! -f "$IDP_ENCRYPTION_CERT" ]; then
	echo "IdP encryption certificate - $IDP_ENCRYPTION_CERT - does not exist" >&2
  exit 1
fi
if [ ! -f "$IDP_METADATA_SIGNING_KEY" ]; then
	echo "IdP metadata signing key - $IDP_METADATA_SIGNING_KEY - does not exist" >&2
  exit 1
fi
if [ ! -f "$IDP_METADATA_SIGNING_CERT" ]; then
	echo "IdP metadata signing certificate - $IDP_METADATA_SIGNING_CERT - does not exist" >&2
  exit 1
fi


#
# Metadata
#


IDP_METADATA_RESOURCES_BEAN=shibboleth.MetadataResolverResources
if [ -n "$IDP_SECONDARY_FEDERATION_METADATA_URL" ]; then
  if [ -z "$IDP_SECONDARY_FEDERATION_METADATA_VALIDATION_CERT" ]; then
    echo "IDP_SECONDARY_FEDERATION_METADATA_VALIDATION_CERT must be set" >&2
    exit 1
  fi
  IDP_METADATA_RESOURCES_BEAN=shibboleth.MetadataResolverResources2
fi

: ${IDP_METADATA_VALIDITY_MINUTES:=10800}
: ${IDP_METADATA_CACHEDURATION_MILLIS:=3600000}

: ${IDP_FEDERATION_METADATA_URL:=https://qa.md.swedenconnect.se/entities}
: ${IDP_FEDERATION_METADATA_VALIDATION_CERT:=$IDP_CREDENTIALS/trust/sc-qa-metadata-validation-cert.crt}

#
# Log settings
#

# Log settings may be overridden by setting IDP_LOG_SETTINGS_FILE to point to a logback include file. 
: ${IDP_LOG_SETTINGS_FILE:=""}
: ${IDP_SYSLOG_PORT:=514}

IDP_AUDIT_APPENDER=IDP_AUDIT
IDP_SYSLOG_HOST_INT=localhost

if [ -n "$IDP_SYSLOG_HOST" ]; then
  IDP_AUDIT_APPENDER=IDP_AUDIT_SYSLOG
  IDP_SYSLOG_HOST_INT=$IDP_SYSLOG_HOST
fi

: ${IDP_AUDIT_SYSLOG_FACILITY:=AUTH}
: ${IDP_LOG_CONSOLE:=false}
IDP_PROCESS_APPENDER=IDP_PROCESS
if [ "$IDP_LOG_CONSOLE" = true ]; then
  IDP_PROCESS_APPENDER=CONSOLE
fi

export JAVA_OPTS="\
          -Djava.net.preferIPv4Stack=true \
          -Didp.home=$IDP_HOME \
          -Didp.baseurl=$IDP_BASE_URL \
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
          -Didp.metadata.federation.url=${IDP_FEDERATION_METADATA_URL} \
          -Didp.metadata.federation.validation-certificate=${IDP_FEDERATION_METADATA_VALIDATION_CERT} \
          -Didp.service.metadata.resources=${IDP_METADATA_RESOURCES_BEAN} \
          -Didp.log-settings.file=$IDP_LOG_SETTINGS_FILE \
          -Didp.audit.appender=$IDP_AUDIT_APPENDER \
          -Didp.syslog.host=$IDP_SYSLOG_HOST_INT \
          -Didp.syslog.facility=$IDP_AUDIT_SYSLOG_FACILITY \
          -Didp.syslog.port=$IDP_SYSLOG_PORT \
          -Didp.consent.appender=NOOP_APPENDER \
          -Didp.warn.appender=NOOP_APPENDER \
          -Didp.process.appender=$IDP_PROCESS_APPENDER \
          ${JAVA_OPTS}"

#
# Secondary metadata source
#
if [ -n "$IDP_SECONDARY_FEDERATION_METADATA_URL" ]; then
  export JAVA_OPTS="${JAVA_OPTS} -Didp.metadata.secondary.federation.url=${IDP_SECONDARY_FEDERATION_METADATA_URL} -Didp.metadata.secondary.federation.validation-certificate=${IDP_SECONDARY_FEDERATION_METADATA_VALIDATION_CERT}"
fi

#
# JVM and JMX
#
: ${JVM_MAX_HEAP:=1536m}
: ${JVM_START_HEAP:=512m}

export JVM_MAX_HEAP JVM_START_HEAP

export CATALINA_OPTS="\
          -Xmx${JVM_MAX_HEAP}\
          -Xms${JVM_START_HEAP}\
          -Dtomcat.hostname=$IDP_TOMCAT_HOSTNAME \
          -Dtomcat.tls.port=$IDP_TOMCAT_TLS_PORT \
          -Dtomcat.ajp.port=$IDP_TOMCAT_AJP_PORT \
          -Dtomcat.tls.keystore=$IDP_TOMCAT_TLS_KEYSTORE \
          -Dtomcat.tls.keystore-type=$IDP_TOMCAT_TLS_KEYSTORE_TYPE \
          -Dtomcat.tls.password=$IDP_TOMCAT_TLS_KEYSTORE_PASSWORD \
          -Dtomcat.tls.alias=$IDP_TOMCAT_TLS_ALIAS \
          -Dtomcat.proxyname=$IDP_TOMCAT_PROXY_NAME \
"

${IDP_TOMCAT_HOME}/bin/catalina.sh run
