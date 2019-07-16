#!/bin/sh

# Exit on error
set -e

# use DEBUG=echo ./docker.sh to print all commands
export DEBUG=${DEBUG:-""}

# TODO replace this later with tagging
# Find out what branch we are on
BRANCH=${BRANCH:-"$(git rev-parse --abbrev-ref HEAD)"}

#TODO this should be replaced with tagging
# Find out the version
if [ "$BRANCH" = "master" ]; then
    VERSION=""
    cp docker/incore-prod.properties docker/incore.properties
elif [ "${BRANCH}" = "develop" ]; then
    VERSION="-dev"
    cp docker/incore-dev.properties docker/incore.properties
else
    exit 0
fi

# Build individual images
$DEBUG docker build -t hub.ncsa.illinois.edu/incore/auth-jetty$VERSION:latest -f Dockerfile.auth .
$DEBUG docker build -t hub.ncsa.illinois.edu/incore/data-jetty$VERSION:latest -f Dockerfile.data .
$DEBUG docker build -t hub.ncsa.illinois.edu/incore/frag-jetty$VERSION:latest -f Dockerfile.fragility .
$DEBUG docker build -t hub.ncsa.illinois.edu/incore/hazard-jetty$VERSION:latest -f Dockerfile.hazard .
$DEBUG docker build -t hub.ncsa.illinois.edu/incore/maestro-jetty$VERSION:latest -f Dockerfile.maestro .
$DEBUG docker build -t hub.ncsa.illinois.edu/incore/space-jetty$VERSION:latest -f Dockerfile.space .
