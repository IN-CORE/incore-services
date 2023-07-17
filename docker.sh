#!/bin/sh

# Exit on error
set -e

# use DEBUG=echo ./docker.sh to print all commands
export DEBUG=${DEBUG:-""}

# TODO replace this later with tagging
# Find out what branch we are on
#BRANCH=${BRANCH:-"$(git rev-parse --abbrev-ref HEAD)"}

# Build individual images
$DEBUG docker build -t incore/data-jetty -f Dockerfile.data .
$DEBUG docker build -t incore/dfr3-jetty -f Dockerfile.dfr3 .
$DEBUG docker build -t incore/hazard-jetty -f Dockerfile.hazard .
$DEBUG docker build -t incore/space-jetty -f Dockerfile.space .
$DEBUG docker build -t incore/semantics-jetty -f Dockerfile.semantics .
