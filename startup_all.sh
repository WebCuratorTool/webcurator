#!/usr/bin/env sh

ps aux | grep 'java -jar build/libs' | grep -v grep | awk '{print $2}' | xargs -r kill -9
echo "--------------Killed All Running Apps--------"

./gradlew clean
echo "--------------Cleaned All--------------------"

cd webcurator-core/
./gradlew clean install -x test
echo "-------------Installed Core Component-------"

cd ..
cd webcurator-submit-to-rosetta/
./gradlew clean install -x test
echo "------------Installed Rosetta Component-----"

cd ..
cd webcurator-webapp/
./gradlew clean build -x test
rm ./logs/*
java -jar build/libs/webcurator-webapp-3.1.0-SNAPSHOT.war &
echo "------------Launched Webapp Component-------"

#sleep 15

cd ..
cd webcurator-store/
./gradlew clean build -x test
rm ./logs/*
java -jar build/libs/webcurator-store-3.1.0-SNAPSHOT.war &
echo "----------Launched Store Component----------"

#sleep 15

cd ..
cd webcurator-harvest-agent-h3/
./gradlew clean build -x test
rm ./logs/*
java -jar build/libs/harvest-agent-h3-3.1.0-SNAPSHOT.war &
echo "---------Launched Harvest Agent Component---"
