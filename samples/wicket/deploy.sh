#!/bin/sh
#
# Usage: sh /fullpath/deploy.sh

DOMAIN="hotels.ujorm.org"
PROTOCOL="https"

REMOTEDIR=$(echo $DOMAIN | tr "." "\n" | head -n 1)
PDIR=$(dirname $0)
MSG=FAILED
cd $PDIR

/opt/maven/default/bin/mvn clean install \
&& rsync -v target/*.war ponec@ponec.net:/home/tomcat/webapps/$REMOTEDIR/ROOT.war \
&& MSG="$PROTOCOL://$DOMAIN/"


echo Result $MSG

