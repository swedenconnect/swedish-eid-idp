#!/bin/bash

#
# Builds the IdP application and creates a Docker image 
#

usage() {
    echo "Usage: $0 [options...]" >&2
    echo
    echo "   -i, --image            Name of image to create (default is swedenconnect-ref-idp)"
    echo "   -t, --tag              Optional docker tag for image tag"
    echo "   -p, --platform         Optional platform parameter value to use when building Docker image"
    echo "   -h, --help             Prints this help"
    echo
}

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

IMAGE_NAME=""
DOCKER_TAG=""
PLATFORM_PAR=""

while :
do
    case "$1" in
  -h | --help)
      usage
      exit 0
      ;;
  -i | --image)
      IMAGE_NAME="$2"
      shift 2
      ;;
  -t | --tag)
      DOCKER_TAG="$2"
      shift 2
      ;;
  -p | --platform)
      PLATFORM_PAR="$2"
      shift 2
      ;;
  --)
      shift
      break;
      ;;
  -*)
      echo "Error: Unknown option: $1" >&2
      usage
      exit 0
      ;;
  *)
      break
      ;;
    esac
done

if [ "$IMAGE_NAME" == "" ]; then
    IMAGE_NAME=swedenconnect-ref-idp
    echo "Docker image name not given, defaulting to $IMAGE_NAME" >&1
fi

if [ "$PLATFORM_PAR" != "" ]; then
    PLATFORM_PAR="--platform $PLATFORM_PAR" 
fi

echo
echo "Building IdP source ..."
echo

mvn -f ${SCRIPT_DIR}/../pom.xml clean install

if [ "$DOCKER_TAG" != "" ]; then
    IMAGE_NAME="$IMAGE_NAME:$DOCKER_TAG"
fi

echo
echo "Building Docker image ${IMAGE_NAME} ..."
echo


docker build -f ${SCRIPT_DIR}/../Dockerfile -t ${IMAGE_NAME} ${PLATFORM_PAR} ${SCRIPT_DIR}/..


