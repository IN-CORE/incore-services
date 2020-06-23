#!/bin/sh

# Exit on error
set -e

# use DEBUG=echo ./docker.sh to print all commands
export DEBUG=${DEBUG:-""}

# TODO replace this later with tagging
# Find out what branch we are on
BRANCH=${BRANCH:-"$(git rev-parse --abbrev-ref HEAD)"}

# Find out the version
if [ "$BRANCH" = "master" ]; then
    VERSION=""
elif [ "${BRANCH}" = "develop" ]; then
    VERSION="-dev"
else
    exit 0
fi

# Build individual images
$DEBUG docker build -t hub.ncsa.illinois.edu/incore/data-jetty:latest$VERSION -f Dockerfile.data .
$DEBUG docker build -t hub.ncsa.illinois.edu/incore/dfr3-jetty:latest$VERSION -f Dockerfile.dfr3 .
$DEBUG docker build -t hub.ncsa.illinois.edu/incore/hazard-jetty:latest$VERSION -f Dockerfile.hazard .
$DEBUG docker build -t hub.ncsa.illinois.edu/incore/space-jetty:latest$VERSION -f Dockerfile.space .
$DEBUG docker build -t hub.ncsa.illinois.edu/incore/semantics-jetty:latest$VERSION -f Dockerfile.semantics .
