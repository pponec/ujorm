#!/bin/sh
# Building the "ujo-web" module with dependences
################################################

# Create the build:
cd "$(dirname $0)/project-m2" || exit 1
sh ./mvnw -DskipTests=true --also-make --projects ujo-web install

echo "Finished"


