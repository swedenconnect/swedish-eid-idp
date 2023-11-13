#!/bin/bash
#
# See https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

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

docker build -f ${SCRIPT_DIR}/../../../Dockerfile -t ${IMAGE_NAME} --platform linux/arm64 ${SCRIPT_DIR}/../../..

echo "Logging in to ${GITHUB_DOCKER_REPO} ..."
echo $GITHUB_ACCESS_TOKEN | docker login $GITHUB_DOCKER_REPO -u $GITHUB_USER --password-stdin

echo "Pushing image to ${GITHUB_DOCKER_REPO} ..."
docker push ${IMAGE_NAME}:latest

 

