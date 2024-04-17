Retrieve Group (GET)
=====================
Returns all information for a specific group.

Version
-------
1.0.0

Request
-------
``https://--WCT_base--/api/v1/groups/{group-id}``

Also the following parts can be retrieved separately by adding the part name to the request query, e.g.
``https://--WCT_base--/api/v1/groups/{group-id}/{part}``

+-------------+
| **part**    |
+=============+
| general     | 
+-------------+
| members     |
+-------------+
| memberOf    |
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
------
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
----
.. include:: /guides/apis/descriptions/desc-request-body-empty.rst

Response
--------
200: OK

.. include:: /guides/apis/descriptions/desc-group_response.rst

Errors
------
If any error is raised no output is returned.

=== ==========================================================================
400 Bad request, non-existing target-id has been given.
400 Bad request, non-existing part has been given.
403 Not authorized, user is no longer logged in.
405 Method not allowed, only POST, GET, PUT, DELETE are allowed.
=== ==========================================================================

Example
-------
.. code-block:: linux

  TODO