Update Group (PUT)
==================
Update a group.

Request
-------
``https://--WCT_base--/api/v1/groups/{group-id}``

Also the following parts can be updated separately by adding the part name to the request query, e.g.
``https://--WCT_base--/api/v1/groups/{group-id}/{part}``

Each part must contain at least one field that needs updating. Only the fields given are updated, fields not given, but present in the database, remain unchanged.

Fields of mutable lists (schedule.schedules, annotations.annotations) can not be updated individually: if a list is present in the input, the corresponding list attribute of the target will be overwritten with the new list. Fields of profile.overrides, which is a fixed list, can be updated individually.

Members can not be added to or removed from a group on update as this list might become too large. Individual members can only be added (link) and removed (link).

+-------------+
| **part**    |
+=============+
| general     | 
+-------------+
| profile     |
+-------------+
| schedule    |
+-------------+
| annotations |
+-------------+
| description |
+-------------+
| access      |
+-------------+

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

	curl --location --request PUT 'http://localhost/wct/api/v1/groups/<group-id>' \
	--header 'Content-Type: application/json' \
	--header 'Authorization: Bearer <token>' \
	--data '{"general": {
			 "description": "Changed description",
			 "type": "thematic",
			 "dateFrom": "1970-09-16T22:00:00.000+00:00",
			 "sipType": 2
		}
	}'