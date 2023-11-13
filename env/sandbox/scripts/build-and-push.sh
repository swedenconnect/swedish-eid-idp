#!/bin/bash

#
# Build script for deploying the IdP to Sweden Connect Sandbox
#

SANDBOX_DOCKER_REPO=docker.eidastest.se:5000
SANDBOX_IMAGE_NAME=swedenconnect-ref-idp

SANDBOX_SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

source ${SANDBOX_SCRIPT_DIR}/../../../scripts/build.sh -i ${SANDBOX_DOCKER_REPO}/${SANDBOX_IMAGE_NAME} -p linux/amd64

echo $SANDBOX_DOCKER_PW | docker login $SANDBOX_DOCKER_REPO -u $SANDBOX_DOCKER_USER --password-stdin

docker push ${SANDBOX_DOCKER_REPO}/${SANDBOX_IMAGE_NAME}



