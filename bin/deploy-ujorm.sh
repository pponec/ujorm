#!/bin/sh
# URL Tutorial: hhttps://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-7a.DeploySnapshotsandStageReleaseswithMaven
# URL Nexus: https://oss.sonatype.org/index.html#view-repositories;snapshots~browsestorage
# SNAPSHOT deploy: mvn clean deploy -Pproduction -Psign -DskipTests
###################################################################

set -e
readonly PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
mvn() { bash "$PROJECT_ROOT/mvnw" "$@"; }
mvn -version
cd "$PROJECT_ROOT"

# Required Release (example: RELEASE=1.30):
RELEASE=$( cd project-m2/ujo-tools; mvn help:evaluate -Dexpression=project.version | grep -v "\[" )
echo RELEASE=${RELEASE}

# Deploy URL:
URL=https://oss.sonatype.org/service/local/staging/deploy/maven2/

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
