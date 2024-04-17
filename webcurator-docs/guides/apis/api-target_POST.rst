Create Target (POST)
====================
Create a target.

Version
-------
1.0.0

Request
-------
``https://--WCT_base--/api/v1/targets``

Header
------
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
----
.. include:: /guides/apis/descriptions/desc-target_request.rst

Response
--------
201: OK

.. include:: /guides/apis/descriptions/desc-response-body-empty.rst

The HTTP Header Location contains the URL to retrieve (GET) the inserted target
as described in :doc:`/guides/apis/api-target_GET`.

Errors
------
If any error is raised no output is returned. Nor is the target created.

=== =======================================================================================
400	Bad request, required value <element> has not been given.
403 Not authorized, user is no longer logged in.
405 Method not allowed, only POST, GET, PUT, DELETE are allowed.
=== =======================================================================================

Example
-------
.. code-block:: linux

  curl \
  --location 'http://localhost/wct/api/v1/targets/<target-id>' \
  --header 'Authorization: Bearer <token>' \ 
  --header 'Content-Type: application/json' \
  --data-raw '{"general": { \
    "owner": "demo", \
    "state": 2, \
    "name" : "TEST - TARGET 2023-07-06T09:43:09.816Z" \
    } \
    }'

.. code-block:: linux

  curl --location 'http://localhost/wct/api/v1/targets' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "general": {
        "owner": "demo",
        "state": 5,
        "name": "TEST - TARGET 2023-07-12T11:35:21.376Z - IJsselbiënale",
        "description": "info@ijsselbiennale.nl"
    },

    "profile": {
        "id": "1",
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

    "groups": [{"id": 70, "name": "testgroep"}],

    "description": {
        "description": "Test target",
        "type": "01 - Algemeen"
    },

    "access": {
        "displayTarget": true,
        "accessZone": "0",
        "accessZoneText": "Public"
    },

    "annotations": { 
        "selection": {
            "date": "2023-05-17T13:37:13.919+00:00",
            "note": null,
            "type": "Producer type"
        },
        "annotations": [
            {
            "date": "2023-05-17T13:37:13.919+00:00",
            "note": "Testannotatie no.1",
            "user": "demo",
            "alert": false
            },
            {
            "date": "2023-06-17T13:37:13.919+00:00",
            "note": "Testannotatie no.2",
            "user": "demo",
            "alert": true
            }
        ],
        "evalutionNote": null,
        "harvestType": "Subject"
    },

    "seeds": [
        {
            "seed": "https://dummyurl.org",
            "primary": true,
            "authorisations": [98304]
        }
    ],
    
    "schedule": {
        "harvestOptimization": false,
        "schedules": [
            {
                "cron": "00 23 25 7,8,9 ? *",
                "startDate": "2019-12-03T15:55:22.000+00:00",
                "type": -3,
                "nextExecutionDate": "2024-12-03T15:55:22.000+00:00",
                "owner": "demo"
            }
        ]
    }
  }'