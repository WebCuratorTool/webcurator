Retrieve Target (GET)
=====================
Returns all information for a specific target.

Request
-------
``https://--WCT_base--/api/v1/targets/{target-id}``

Also the following parts can be retrieved separately by adding the part name to the request query, e.g.
``https://--WCT_base--/api/v1/target/{target-id}/{part}``

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
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-request-body-empty.rst

Response
--------
200: OK

.. include:: /guides/apis/descriptions/desc-target.rst

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

  curl \
  --location 'http://localhost/wct/api/v1/targets/<target-id>' \
  --header 'Authorization: Bearer <token>'
  
.. code-block:: linux

  curl \
  --location 'http://localhost/wct/api/v1/targets/<target-id>/general' \
  --header 'Authorization: Bearer <token>'