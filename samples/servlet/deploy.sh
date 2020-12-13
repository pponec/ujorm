#!/bin/sh
#
# Usage: sh /fullpath/deploy.sh

targetUrl=https://servlet.ujorm.org/
pdir=$(dirname $0)
web=/home/tomcat/webapps/servlet
msg="ERROR"

cd $pdir \
&& /opt/maven/default/bin/mvn clean install \
&& rsync -v target/*.war ponec@ujorm.org:$web/ROOT.war \
&& msg=$targetUrl

echo Result: $msg

