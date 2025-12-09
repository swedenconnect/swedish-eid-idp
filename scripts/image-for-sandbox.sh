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
# Build script for building and pushing a Docker image to docker repo that is used for Sweden Connect Sandbox
#

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BUILD_DIR=${SCRIPT_DIR}/..

SANDBOX_DOCKER_REPO=docker.eidastest.se:5000

IMAGE_NAME=${SANDBOX_DOCKER_REPO}/swedenconnect-ref-idp

if [ -z "$SANDBOX_DOCKER_USER" ]; then
  echo "The SANDBOX_DOCKER_USER variable must be set"
  exit 1
fi

if [ -z "$SANDBOX_DOCKER_PW" ]; then
  echo "The SANDBOX_DOCKER_PW variable must be set"
  exit 1
fi

echo "Logging in to ${SANDBOX_DOCKER_REPO} ..."
echo $SANDBOX_DOCKER_PW | docker login $SANDBOX_DOCKER_REPO -u $SANDBOX_DOCKER_USER --password-stdin

mvn -f ${BUILD_DIR}/pom.xml clean install

docker build -f ${BUILD_DIR}/Dockerfile -t ${IMAGE_NAME} --platform linux/amd64 ${BUILD_DIR}
docker push ${IMAGE_NAME}
