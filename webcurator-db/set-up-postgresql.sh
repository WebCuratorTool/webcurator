#!/usr/bin/env bash

export PGPASSWORD=password

cat latest/src/main/resources/setup/wct-create-postgres.sql | psql -U postgres
cat latest/src/main/resources/sql/wct-schema-postgres.sql | psql -U postgres --dbname=Dwct
cat latest/src/main/resources/sql/wct-schema-grants-postgres.sql | psql -U postgres --dbname=Dwct
cat latest/src/main/resources/sql/wct-indexes-postgres.sql | psql -U postgres --dbname=Dwct
cat latest/src/main/resources/sql/wct-bootstrap-postgres.sql | psql -U postgres --dbname=Dwct
cat latest/src/main/resources/sql/wct-qa-data-postgres.sql | psql -U postgres --dbname=Dwct
