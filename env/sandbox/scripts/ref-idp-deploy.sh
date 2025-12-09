#!/bin/bash

#
# Copyright 2023-2025 Sweden Connect
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
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
