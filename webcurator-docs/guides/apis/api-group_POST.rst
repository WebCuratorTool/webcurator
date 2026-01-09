Create Group (POST)
===================
Create a group.

Request
-------
``https://--WCT_base--/api/v1/groups``

Members can not be added to a group on creation as this list might become too large. Individual members can only be added (:doc:`/guides/apis/api-group_member_POST`) and removed (:doc:`/guides/apis/api-group_member_DELETE`).

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-group_request.rst

Response
--------
201: OK

.. include:: /guides/apis/descriptions/desc-response-body-empty.rst

The HTTP Header Location contains the URL to retrieve (GET) the inserted group
as described in :doc:`/guides/apis/api-group_GET`.

Errors
------
If any error is raised no output is returned. Nor is the group created.

=== ==========================================================================
400 Bad request, non-existing target-id has been given.
400 Bad request, non-existing part has been given.
403 Not authorized, user is no longer logged in.
405 Method not allowed, only POST, GET, PUT, DELETE are allowed.
=== ==========================================================================

Example
-------
.. code-block:: linux

	curl --location 'http://localhost/wct/api/v1/groups' \
	--header 'Content-Type: application/json' \
	--header 'Authorization: Bearer <token>' \
	--data '{"general": {
				"owner": "demo",
				"name" : "TEST - GROUP 2024-07-04T11:36:34.000+00:00"
		}
	}'

.. code-block:: linux
	
	curl --location 'http://localhost/wct/api/v1/groups' \
	--header 'Content-Type: application/json' \
	--header 'Authorization: Bearer <token>' \
	--data '{
    "general": {
        "owner": "demo",
        "name" : "TEST - GROUP 2024-07-04T12:24:45.000+00:00",
        "description": "A test groep",
        "type": "thematic",
        "dateFrom": "2023-08-21T22:00:00.000+00:00",
        "sipType": 1
    },

    "profile": {
        "id": 1,
        "imported": false,
        "harvesterType": "HERITIRX3",
        "name": "Default - KB",
        "overrides": [
            {
                "id": "ignoreRobots",
                "value": true,
                "enabled": true
            }
        ]
    },

    "description": {
        "description": "Test group",
        "type": "01 - Algemeen"
    },

    "schedules": [
        {
            "cron": "23 12 1 * ? *",
            "startDate": "2025-10-21T22:00:00.000+00:00",
            "endDate": null,
            "nextExecutionDate": "2025-10-21T22:00:00.000+00:00",
            "type": "-3",
            "owner": "demo"
        }
    ],

    "annotations": [
            {
            "user": "demo",
            "date": "2024-07-04T12:24:45.000+00:00",
            "note": "Testannotatie no.1"
            },
            {
            "user": "demo",
            "date": "2024-07-04T12:24:45.000+00:00",
            "note": "Testannotatie no.2"
            }
        ],

    "access": {
        "displayTarget": true,
        "accessZone": 0
    }

	}'