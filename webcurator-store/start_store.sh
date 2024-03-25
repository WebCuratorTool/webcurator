#!/bin/sh
# Tomcat server start script.

nohup /opt/u01/local/jdk1.8.0_181/jre/bin/java -Xmx256m -jar webcurator-store.war --spring.profiles.active=store+nlnzuat >store_log.out 2>&1 &
#$cmd

