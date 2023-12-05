#!/bin/bash
#
# Build script for building and depoying a Docker image to the GitHib docker repo
#
# See https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry
#

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BUILD_DIR=${SCRIPT_DIR}/..

GITHUB_DOCKER_REPO=ghcr.io
IMAGE_NAME=${GITHUB_DOCKER_REPO}/swedenconnect/swedish-eid-idp

if [ -z "$GITHUB_USER" ]; then
  echo "The GITHUB_USER variable must be set"
  exit 1
fi

if [ -z "$GITHUB_ACCESS_TOKEN" ]; then
  echo "The GITHUB_ACCESS_TOKEN variable must be set"
  exit 1
fi

echo "Logging in to ${GITHUB_DOCKER_REPO} ..."
echo $GITHUB_ACCESS_TOKEN | docker login $GITHUB_DOCKER_REPO -u $GITHUB_USER --password-stdin

source ${SCRIPT_DIR}/build-image.sh -i ${IMAGE_NAME} -s -d ${BUILD_DIR} -p
