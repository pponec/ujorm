#!/bin/sh
# Building the "ujo-web" module with dependences
################################################

alias mvn='/opt/maven/default/bin/mvn'

# Create the build:
cd "$(dirname $0)/project-m2" || exit 1
mvn -DskipTests=true --also-make --projects ujo-web install

echo "Finished"


