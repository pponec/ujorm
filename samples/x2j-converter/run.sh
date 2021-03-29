#!/bin/sh

set -e
cd "$(dirname $0)"

sh mvnw spring-boot:run

