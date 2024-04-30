Update Target (PUT)
===================
Update a specific target or part therof.

Version
-------
1.0.0

Request
-------
``https://--WCT_base--/api/v1/targets/{target-id}``

Also the following parts can be updated separately by adding the part name to the request query, e.g.
``https://--WCT_base--/api/v1/target/{target-id}/{part}``

Each part must contain at least one field that needs updating. Only the fields given are updated, fields not given, but present in the database, remain unchanged.

Fields of mutable lists (seeds, schedule.schedules, annotations.annotations, groups) can not be updated individually: if a list is present in the input, the corresponding list attribute of the target will be overwritten with the new list. Fields of profile.overrides, which is a fixed list, can be updated individually.

+-------------+
| **part**    |
+=============+
| general     | 
+-------------+
| seeds       |
+-------------+
| profile     |
+-------------+
| schedule    |
+-------------+
| annotations |
+-------------+
| description |
+-------------+
| groups      |
+-------------+
| access      |
+-------------+

Header
------
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
----
.. include:: /guides/apis/descriptions/desc-target_request.rst

Response
--------
200: OK

.. include:: /guides/apis/descriptions/desc-response-body-empty.rst

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
  --location 'http://localhost/wct/api/v1/targets/<target-id>' 
  --header 'Authorization: Bearer <token>'  
  --header 'Content-Type: application/json' 
  --data-raw '{ 
    "general": { 
      "owner": "demo", 
      "state": 2, 
      "name" : "TEST - TARGET 2023-07-06T09:43:09.816Z" 
    } 
  }'