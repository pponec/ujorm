#!/bin/sh

# Terminate script on first error
set -e
cd "$(dirname "$0")/.."
mvn() { bash "$PWD/mvnw" "$@"; }
mvn -version || exit

# Target JavaDoc dir
targetDir="_javadoc"

# Define project modules (MUST include '.' for the root aggregator project)
modules=".
  project-m2
  project-m2/ujo-converter
  project-m2/ujo-core
  project-m2/ujo-orm
  project-m2/ujo-tools
  project-m2/ujo-web
"

# Convert multiline string into a comma-separated list
moduleList=$(echo "$modules" | sed 's/^[[:space:]]*//' | sed '/^$/d' | paste -sd "," -)

echo "Building aggregated Javadoc for specific modules: $moduleList"
echo "Output directory set to: $targetDir"

if [ -d "$targetDir" ]; then
  echo "Cleaning up old target directory: $targetDir"
  rm -rf "$targetDir"
fi

# Run Maven wrapper for aggregated Javadoc
mvn clean compile javadoc:aggregate -pl "$moduleList" -Dshow=protected -Ddoclint=none

sourceDir="target/reports/apidocs"

if [ -d "$sourceDir" ]; then
  mkdir -p "$targetDir"
  cp -r "$sourceDir/"* "$targetDir/"
  echo "Javadoc successfully published to: $targetDir"
else
  echo "Error: Directory $sourceDir does not exist."
  exit 1
fi