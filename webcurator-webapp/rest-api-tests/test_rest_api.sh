#!/bin/bash


# Integration test for the ReST API against a local install of WCT
#
# Requirements:
#
# 1) jq version 1.6 or newer
# 2) GNU coreutils
# 3) make sure there's a file called "credentials" in your working directory, containing the 
#    following lines:
#    user=<your WCT username>
#    password=<your WCT password>


post_target_file_template=post-target-template.json
put_target_file=put-target.json
put_target_instance_file=put-target-instance.json
. ./credentials

post_target_file="/tmp/post-target-file-$RANDOM.json"
put_target_instance_revert_file="/tmp/put-target-instance-revert-file-$RANDOM.json"

echo "Getting token"
echo "curl http://localhost:8080/wct/auth/v1/token -d'username=$user&password=****'"
token=`curl http://localhost:8080/wct/auth/v1/token -d"username=$user&password=$password"`

echo "Getting targets"
echo "curl -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/targets/" 
first_target_id=`curl -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/targets/ | jq '.targets[0].id'`

echo "Getting first target from list"
echo "curl -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/targets/$first_target_id" 
curl -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/targets/$first_target_id | jq .

echo "Getting harvest authorisations"
echo "curl -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/harvest-authorisations" 
first_authorisation_id=`curl -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/harvest-authorisations | jq '.harvestAuthorisations[0].id'`

echo "Getting users"
echo "curl -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/users" 
first_user_name=`curl -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/users | jq -r '.users[0].name'`

echo "Getting profiles"
echo "curl -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/profiles" 
first_profile_id=`curl -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/profiles | jq '.profiles[0].id'`

echo "Getting groups"
echo "curl -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/groups" 
first_group_id=`curl -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/groups | jq '.groups[0].id'`

if [ "$first_group_id" == "null" ]
then
	echo "Error: no target groups found. Please create a target group and run this script again"
	exit 1
fi

# Replace placeholders with values we've just found
date=`date -Iseconds`
cat $post_target_file_template | sed s/\$username/$first_user_name/g \
					| sed s/\$groupId/$first_group_id/g \
					| sed s/\$authorisationId/$first_authorisation_id/g \
					| sed s/\$profileId/$first_profile_id/g \
					| sed s/\$date/$date/g \
				> $post_target_file


echo "Posting target example"
echo "curl -H\"Content-Type: application/json\" -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/targets/ -d @$post_target_file"
id=`curl -v -H"Content-Type: application/json" -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/targets/ -d @$post_target_file 2>&1 | grep "Location:" | tr -d '\r' | sed 's/.*\/\([0-9]\+\)$/\1/'`

if [ -z $id ]
then
	echo "POST of $post_target_file failed"
	exit 1
fi

echo "POST succeeded: there's a new target with id $id"
echo "Getting newly created target with id $id"
echo "curl -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/targets/$id" 
curl -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/targets/$id | jq .  

echo "Updating target with id $id"
echo "curl -XPUT -H\"Content-Type: application/json\" -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/targets/$id -d @$put_target_file"
curl -XPUT -H"Content-Type: application/json" -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/targets/$id -d @$put_target_file

echo "Getting updated target"
echo "curl -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/targets/$id" 
curl -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/targets/$id | jq .  

echo "Creating target instance for target $id"
data="{\"general\": {\"state\":5}, \"schedule\": {\"harvestNow\": true, \"schedules\": []}}"
echo "curl -XPUT -H\"Content-Type: application/json\" -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/targets/$id -d '$data'"
curl -XPUT -H"Content-Type: application/json" -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/targets/$id -d "$data"

echo "Getting target instance for target $id"
echo "curl -XGET -H\"Content-Type: application/json\" -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/target-instances/ -d'{\"filter\": {\"targetId\" : \"$id\"}}'" 
target_instance_id=`curl -XGET -H"Content-Type: application/json" -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/target-instances/ -d"{\"filter\": {\"targetId\" : \"$id\"}}" | jq '.targetInstances[0].id'`

echo "Getting target instance with id $target_instance_id"
echo "curl -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/target-instances/$target_instance_id" 
curl -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/target-instances/$target_instance_id > $put_target_instance_revert_file

echo "Updating target instance with id $target_instance_id"
echo "curl -XPUT -H\"Content-Type: application/json\" -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/target-instances/$target_instance_id -d @$put_target_instance_file"
curl -XPUT -H"Content-Type: application/json" -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/target-instances/$target_instance_id -d @$put_target_instance_file

echo "Getting updated target instance"
echo "curl -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/target-instances/$target_instance_id" 
curl -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/target-instances/$target_instance_id | jq .

# Delete it, once we have an API call for that
echo "Reverting changes to target instance $target_instance_id"
echo "curl -XPUT -H\"Content-Type: application/json\" -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/target-instances/$target_instance_id -d @$put_target_instance_revert_file"
curl -XPUT -H"Content-Type: application/json" -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/target-instances/$target_instance_id -d @$put_target_instance_revert_file

# This will fail until we're able to delete target instances
echo "Cleaning up target $id"
echo "curl -XDELETE -H\"Authorization: Bearer $token\" http://localhost:8080/wct/api/v1/targets/$id" 
#curl -XDELETE -H"Authorization: Bearer $token" http://localhost:8080/wct/api/v1/targets/$id

echo "Cleaning up temp files"
rm $post_target_file $put_target_instance_revert_file
echo "Done"
exit 0
