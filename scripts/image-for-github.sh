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
# Build script for building and depoying a Docker image to the GitHib docker repo
#
# See https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry
#

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BUILD_DIR=${SCRIPT_DIR}/..

GITHUB_DOCKER_REPO=ghcr.io

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

source ${SCRIPT_DIR}/build-image.sh -r $GITHUB_DOCKER_REPO -d ${BUILD_DIR}
