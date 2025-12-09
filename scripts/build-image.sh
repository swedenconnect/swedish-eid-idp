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
# Builds the application and after that a Docker image (and pushes the image)
#

usage() {
    echo "Usage: $0 [options...]" >&2
    echo
    echo "   -d, --builddir         Source build dir (where POM is located)"
    echo "   -t, --tag              Optional docker tag for image (default is latest)"
    echo "   -r, --registry         Docker registry to push to"
    echo "                          If not given, a local build is made"
    echo "   -h, --help             Prints this help"
    echo
}

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

REGISTRY_NAME=""
DOCKER_TAG=""
BUILD_DIR=""

while :
do
    case "$1" in
  -h | --help)
      usage
      exit 0
      ;;
  -r | --registry)
      REGISTRY_NAME="$2"
      shift 2
      ;;
  -d | --builddir)
      BUILD_DIR="$2"
      shift 2
      ;;
  -t | --tag)
      DOCKER_TAG="$2"
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

if [ "$BUILD_DIR" == "" ]; then
    echo "Missing build directory"
    usage
    exit 1
fi

if [ "$DOCKER_TAG" == "" ]; then
    DOCKER_TAG=latest
fi

if [ "$REGISTRY_NAME" == "" ]; then

  mvn -f ${BUILD_DIR}/pom.xml clean install jib:dockerBuild@local -Djib.to.tags=${DOCKER_TAG}

else

  export DOCKER_REPO=${REGISTRY_NAME}
  mvn -f ${BUILD_DIR}/pom.xml clean install jib:build -Djib.to.tags=${DOCKER_TAG}

fi
