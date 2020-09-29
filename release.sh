#!/bin/sh

# Exit on error
set -e

# use DEBUG=echo ./release-all.sh to print all commands
export DEBUG=${DEBUG:-""}

$DEBUG docker login hub.ncsa.illinois.edu

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

#TODO this should be replaced with tagging
# Push all images
docker push hub.ncsa.illinois.edu/incore/data-jetty:latest$VERSION
docker push hub.ncsa.illinois.edu/incore/dfr3-jetty:latest$VERSION
docker push hub.ncsa.illinois.edu/incore/hazard-jetty:latest$VERSION
docker push hub.ncsa.illinois.edu/incore/space-jetty:latest$VERSION
docker push hub.ncsa.illinois.edu/incore/semantics-jetty:latest$VERSION