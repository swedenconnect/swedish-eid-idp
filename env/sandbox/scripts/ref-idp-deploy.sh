#!/bin/bash

#
# Deployment script for the Sandbox environment
#
IDP_HTTPS_PORT=8413
IDP_AJP_PORT=8013

echo Pulling swedenconnect-ref-idp docker image ...
docker pull docker.eidastest.se:5000/swedenconnect-ref-idp

echo Undeploying swedenconnect-ref-idp container ...
docker rm swedenconnect-ref-idp --force

IDP_HOME=/opt/refidp
AJP_SECRET="TODO: insert secret"

echo Redeploying docker container swedenconnect-ref-idp ...
docker run -d --name swedenconnect-ref-idp --restart=always \
  -p ${IDP_AJP_PORT}:8009 \
  -p ${IDP_HTTPS_PORT}:8443 \
  -e SPRING_PROFILES_ACTIVE=sandbox \
  -e SPRING_CONFIG_ADDITIONAL_LOCATION=${IDP_HOME}/config/ \
  -e IDP_HOME=${SS_HOME} \
  -e IDP_CONFIG_DIR=${IDP_HOME}/config \
  -e IDP_DIR=${IDP_HOME} \
  -e TOMCAT_AJP_SECRET=${AJP_SECRET} \
  -e "TZ=Europe/Stockholm" \
  -v /etc/localtime:/etc/localtime:ro \
  -v /opt/docker/refidp:${IDP_HOME} \
  docker.eidastest.se:5000/swedenconnect-ref-idp

echo Done!

