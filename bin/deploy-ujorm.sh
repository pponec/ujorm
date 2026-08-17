#!/bin/sh
# Publishes the release to Maven Central through the Central Portal.
# Tutorial: https://central.sonatype.org/publish/publish-portal-maven/
# Artefacts: https://central.sonatype.com/namespace/org.ujorm
#
# The central-publishing-maven-plugin runs with autoPublish=true, hence the
# artefacts go live at once and no version can be withdrawn afterwards.
# Check out the release branch first, so that no SNAPSHOT gets published.
#
# Credentials: the <server> of the id "central" in the ~/.m2/settings.xml.
# Signing: the maven-gpg-plugin, whose key is named by the "gpg" profile
# of the same file; GnuPG asks for the passphrase interactively.
###################################################################

set -e
readonly PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
mvn() { bash "$PROJECT_ROOT/mvnw" "$@"; }
mvn -version
cd "$PROJECT_ROOT"

# Required Release (example: RELEASE=1.30):
RELEASE=$( cd project-m2/ujo-tools; mvn help:evaluate -Dexpression=project.version | grep -v "\[" )
echo RELEASE=${RELEASE}

# Create the build:
mvn clean install
cd "project-m2"

ARTEFACTS="
  ujo-tools
  ujo-web
  ujo-core
  ujo-orm
  ujo-converter
  ujorm-meta-processor
"

# For all artefact SIGN and DEPLOY:
for ITEM in $ARTEFACTS
do
  (
    echo "Processing ARTEFACT: $ITEM"
    cd $ITEM
    mvn clean deploy -P gpg
  )
done

echo "Release $RELEASE is done"
