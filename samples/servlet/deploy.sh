#!/bin/sh
#
# Usage: sh /fullpath/deploy.sh

pdir=$(dirname $0)
web=/home/tomcat/webapps/servlet
msg="ERROR"

cd $pdir \
&& /opt/maven/default/bin/mvn clean install \
&& rsync -v target/*.war ponec@ujorm.org:$web/ROOT.war \
&& msg=OK

echo Result: $msg

