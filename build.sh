#!/bin/sh
# URL Tutorial: hhttps://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-7a.DeploySnapshotsandStageReleaseswithMaven
# URL Nexus: https://oss.sonatype.org/index.html#view-repositories;snapshots~browsestorage

# Required Release:
RELEASE=1.30.RC2

# Create the build:
mvn clean install javadoc:jar source:jar -DskipTests -Pproduction
cd "project-m2"

for ARTEFACT in ujo-core ujo-orm ujo-spring 
do
    (
	echo $ARTEFACT
	cd   $ARTEFACT/target

	mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$ARTEFACT-$RELEASE.pom -Dfile=$ARTEFACT-$RELEASE.jar
	mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$ARTEFACT-$RELEASE.pom -Dfile=$ARTEFACT-$RELEASE-sources.jar -Dclassifier=sources
	mvn gpg:sign-and-deploy-file -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$ARTEFACT-$RELEASE.pom -Dfile=$ARTEFACT-$RELEASE-javadoc.jar -Dclassifier=javadoc
    )
done

echo "Finished"


