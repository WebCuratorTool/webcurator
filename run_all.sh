#!/bin/bash

cd 'webcurator-webapp'
gradle clean bootRun &

cd ..
cd 'webcurator-harvest-agent-h3'
gradle clean bootRun &

