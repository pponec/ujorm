#!/bin/sh
# URL Tutorial: hhttps://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-7a.DeploySnapshotsandStageReleaseswithMaven
# URL Nexus: https://oss.sonatype.org/index.html#view-repositories;snapshots~browsestorage
# SNAPSHOT deploy: mvn clean deploy -Pproduction -Psign -DskipTests
###################################################################

set -e
alias mvn="sh $PWD/mvnw"
mvn -version || exit

# Required Release (example: RELEASE=1.30):
RELEASE=$( cd project-m2/ujo-tools; mvn help:evaluate -Dexpression=project.version | grep -v "\[" )
echo RELEASE=${RELEASE}

# Deploy URL:
URL=https://oss.sonatype.org/service/local/staging/deploy/maven2/

# Create the build:
mvn clean install
cd "project-m2"

# For all artefact SIGN and DEPLOY:
for ARTEFACT in ujo-tools ujo-web
do
  (
	  echo ARTEFACT=$ARTEFACT
	  cd $ARTEFACT
    mvn clean deploy -P gpg
  )
done

echo "Release $RELEASE is done"


