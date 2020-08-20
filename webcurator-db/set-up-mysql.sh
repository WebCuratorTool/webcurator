#!/usr/bin/env bash


export MYSQL_PWD=password


cat latest/src/main/resources/setup/wct-create-mysql.sql | mysql -u root
cat latest/src/main/resources/sql/wct-schema-mysql.sql | mysql -u root -D DB_WCT
cat latest/src/main/resources/sql/wct-schema-grants-mysql.sql | mysql -u root -D DB_WCT
cat latest/src/main/resources/sql/wct-indexes-mysql.sql | mysql -u root -D DB_WCT
cat latest/src/main/resources/sql/wct-bootstrap-mysql.sql | mysql -u root -D DB_WCT
cat latest/src/main/resources/sql/wct-qa-data-mysql.sql | mysql -u root -D DB_WCT
