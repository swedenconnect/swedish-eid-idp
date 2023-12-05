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

source ${SCRIPT_DIR}/build-image.sh -i ${IMAGE_NAME} -s -d ${BUILD_DIR} -p -a linux/amd64 

