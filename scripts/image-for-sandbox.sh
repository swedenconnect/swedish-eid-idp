#!/bin/bash
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

