#!/bin/sh
#
# Usage: sh /fullpath/deploy.sh

set -e
DOMAIN="hotels.ujorm.org"
PROTOCOL="https"

REMOTEDIR=$(echo $DOMAIN | tr "." "\n" | head -n 1)
PDIR=$(dirname $0)
MSG=FAILED
cd $PDIR

sh ../../mvnw clean install \
&& rsync -v target/*.war ponec@webfort:/home/tomcat/webapps/$REMOTEDIR/ROOT.war \
&& MSG="$PROTOCOL://$DOMAIN/"


echo Result $MSG

