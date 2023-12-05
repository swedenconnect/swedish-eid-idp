#!/bin/bash

#
# Builds the application and after that a Docker image (and pushes the image)
#

usage() {
    echo "Usage: $0 [options...]" >&2
    echo
    echo "   -i, --image            Name of image to create"
    echo "   -d, --builddir         Directory where Dockerfile (and Maven POM) is placed"
    echo "   -s, --buildsource      To build Java source"
    echo "                          If a build-source.sh is available in the same directory as this"
    echo "                          script, this script will be used to build the source. Otherwise it"
    echo "                          is assumed that the POM file under builddir should be used"
    echo "   -t, --tag              Optional docker tag for image (default is latest)"
    echo "   -p, --push             To push the image to the registry (read from image name)"
    echo "   -a, --platform         Optional platform/architechture parameter value to use when building Docker image"
    echo "                          Should be used when building for a single architechture"
    echo "                          Default is: linux/amd64,linux/arm64"     
    echo "   -h, --help             Prints this help"
    echo
}

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

MULTIHOST_BUILDER_NAME=multiplatformbuilder
IMAGE_NAME=""
DOCKER_TAG=""
BUILD_DIR=""
BUILD_SOURCE_FLAG=false
OUTPUT_PAR=""
PLATFORM_PAR=""
MULTIPLATFORM_BUILD=true

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
  -d | --builddir)
      BUILD_DIR="$2"
      shift 2
      ;;
  -s | --buildsource)
      BUILD_SOURCE_FLAG=true
      shift
      ;;
  -p | --push)
      OUTPUT_PAR="--push"
      shift
      ;;
  -t | --tag)
      DOCKER_TAG="$2"
      shift 2
      ;;
  -a | --platform)
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

if [ "$PLATFORM_PAR" != "" ]; then
    PLATFORM_PAR="--platform $PLATFORM_PAR"
    MULTIPLATFORM_BUILD=false
else
    PLATFORM_PAR="--platform linux/amd64,linux/arm64"
fi

#
# Make sure that the multi-host docker builder is installed
#
if [ "$MULTIPLATFORM_BUILD" == true ]; then

  BUILDER_INSTALLED=`docker buildx ls | grep $MULTIHOST_BUILDER_NAME`

  if [ "$BUILDER_INSTALLED" == "" ]; then
      echo "No Docker multi-host builder available"
      echo "Run the create-multi-builder.sh script"
      echo "  See https://github.com/swedenconnect/local-environment repository"
      echo
      exit 1 
  fi
  
fi

if [ "$IMAGE_NAME" == "" ]; then
    echo "Missing image name"
    usage
    exit 1
fi

if [ "$BUILD_DIR" == "" ]; then
    echo "Missing build directory (i.e., directory where Dockerfile is placed)"
    usage
    exit 1
fi

if [ "$OUTPUT_PAR" == "" ]; then
    OUTPUT_PAR="--load"
fi

if [ "$BUILD_SOURCE_FLAG" == true ]; then

  # Build the source ...
  # First we check if there is a build-source.sh and if so use that,
  # otherwise we assume that we should build the POM in BUILD_DIR
  #
  if ! command -v ${SCRIPT_DIR}/build-source.sh &> /dev/null
  then
  
    echo
    echo "Building source ..."
    echo
    mvn -f ${BUILD_DIR}/pom.xml clean install

  else
    source ${SCRIPT_DIR}/build-source.sh
  fi
  
fi

if [ "$DOCKER_TAG" == "" ]; then
    DOCKER_TAG=latest
fi
IMAGE_NAME="$IMAGE_NAME:$DOCKER_TAG"

echo
echo "Building Docker image ${IMAGE_NAME} ..."
echo

if [ "$MULTIPLATFORM_BUILD" == true ]; then

  docker buildx --builder multiplatformbuilder build -f ${BUILD_DIR}/Dockerfile -t ${IMAGE_NAME} ${PLATFORM_PAR} ${OUTPUT_PAR} ${BUILD_DIR}
  
else

  docker build -f ${BUILD_DIR}/Dockerfile -t ${IMAGE_NAME} ${PLATFORM_PAR} ${BUILD_DIR}
  if [ "$OUTPUT_PAR" != "--load" ]; then
    docker push ${IMAGE_NAME}
  fi 

fi  

