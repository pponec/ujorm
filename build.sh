#!/bin/sh
# URL Tutorial: hhttps://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-7a.DeploySnapshotsandStageReleaseswithMaven
# URL Nexus: https://oss.sonatype.org/index.html#view-repositories;snapshots~browsestorage
# SNAPSHOT deploy: mvn clean deploy -Pproduction -Psign -DskipTests
###################################################################

# Required Release (example: RELEASE=1.30):
RELEASE=$( cd project-m2/ujo-core; mvn help:evaluate -Dexpression=project.version | grep -v "\[" )
echo RELEASE=${RELEASE}

# Deploy URL:
URL=https://oss.sonatype.org/service/local/staging/deploy/maven2/

# Create the build:
mvn clean install javadoc:jar source:jar -DskipTests -Pproduction
cd "project-m2"

# For all artefact SIGN and DEPLOY:
for ARTEFACT in ujo-core ujo-orm ujo-spring 
do
    (
	echo ARTEFACT=$ARTEFACT
	cd $ARTEFACT/target

	mvn gpg:sign-and-deploy-file -Durl=${URL} -DrepositoryId=sonatype-nexus-staging -DpomFile=$ARTEFACT-$RELEASE.pom -Dfile=$ARTEFACT-$RELEASE.jar
	mvn gpg:sign-and-deploy-file -Durl=${URL} -DrepositoryId=sonatype-nexus-staging -DpomFile=$ARTEFACT-$RELEASE.pom -Dfile=$ARTEFACT-$RELEASE-sources.jar -Dclassifier=sources
	mvn gpg:sign-and-deploy-file -Durl=${URL} -DrepositoryId=sonatype-nexus-staging -DpomFile=$ARTEFACT-$RELEASE.pom -Dfile=$ARTEFACT-$RELEASE-javadoc.jar -Dclassifier=javadoc
    )
done

echo "Finished"


