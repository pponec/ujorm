#!/bin/sh
#
# Usage: sh /fullpath/deploy.sh

pdir=$(dirname $0)/..
cd $pdir

/opt/maven/default/bin/mvn clean install && scp target/*.war ponec@ponec.net:/home/tomcat/webapps/hotels/ROOT.war && msg=OK

echo Result OK

