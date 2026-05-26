#!/bin/sh



# copy db setup scripts to postgres build dir
cp -R ../webcurator-db/ postgres/


# build image and create/run the container (this can be done by compose later on)
podman build -t=postgres postgres
podman run --name=postgres -d localhost/postgres 


