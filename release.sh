#!/bin/sh

# Exit on error
set -e

# use DEBUG=echo ./release-all.sh to print all commands
export DEBUG=${DEBUG:-""}

export SERVER=${SERVER:-"hub.ncsa.illinois.edu"}
$DEBUG docker login ${SERVER}

# Find out what branch we are on
BRANCH=${BRANCH:-"$(git rev-parse --abbrev-ref HEAD)"}

PKG_VERSION=$(cat server/build.gradle | grep "archiveVersion" | head -1 | awk -F= "{ print $2 }" | sed "s/[archiveVersion =,',]//g")

# Find out the version
if [ "$BRANCH" = "master" ]; then
    echo "Detected version ${PKG_VERSION}"
    VERSIONS="latest"
    OLDVERSION=""
    TMPVERSION=$PKG_VERSION
    while [ "$OLDVERSION" != "$TMPVERSION" ]; do
        VERSIONS="${VERSIONS} ${TMPVERSION}"
        OLDVERSION="${TMPVERSION}"
        TMPVERSION=$(echo ${OLDVERSION} | sed 's/\.[0-9]*$//')
    done

    TAG=$VERSIONS
elif [ "${BRANCH}" = "develop" ]; then
    TAG="develop"
else
    # Get the issue number for tagging
    TAG=$(echo $BRANCH | sed -e 's/^.*INCORE1-\([0-9]*\).*/INCORE-\1/' -e 's/^\(.\{15\}\).*/\1/' -e 's|/|-|g')
fi

for v in ${TAG}; do
    ${DEBUG} docker tag incore/data-jetty hub.ncsa.illinois.edu/incore/data-jetty:${v}
    ${DEBUG} docker tag incore/dfr3-jetty hub.ncsa.illinois.edu/incore/dfr3-jetty:${v}
    ${DEBUG} docker tag incore/hazard-jetty hub.ncsa.illinois.edu/incore/hazard-jetty:${v}
    ${DEBUG} docker tag incore/space-jetty hub.ncsa.illinois.edu/incore/space-jetty:${v}
    ${DEBUG} docker tag incore/semantics-jetty hub.ncsa.illinois.edu/incore/semantics-jetty:${v}

    ${DEBUG} docker push ${SERVER}/incore/data-jetty:${v}
    ${DEBUG} docker push ${SERVER}/incore/dfr3-jetty:${v}
    ${DEBUG} docker push ${SERVER}/incore/hazard-jetty:${v}
    ${DEBUG} docker push ${SERVER}/incore/space-jetty:${v}
    ${DEBUG} docker push ${SERVER}/incore/semantics-jetty:${v}
done
