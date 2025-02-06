Update Target Instance (PUT)
===============================
Update a specific target instance or part therof.

Note that not all fields are updatable. If non-updatable fields are given in the request this will lead to an error.

Request
-------
``https://--WCT_base--/api/v1/target_instances/{target_instance-id}``

Also the following parts can be updated separately by adding the part name to the request query, e.g.
``https://--WCT_base--/api/v1/target_instances/{target_instance-id}/{part}``

Each part must contain at least one field that needs updating. Only the fields given are updated, fields not given, but present in the database, remain unchanged.

Fields of mutable lists (annotations.annotations) can not be updated individually: if a list is present in the input, the corresponding list attribute of the target will be overwritten with the new list. Fields of profile.overrides, which is a fixed list, can be updated individually.

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-target_instance_request.rst

Response
--------
200: OK

.. include:: /guides/apis/descriptions/desc-response-body-empty.rst

Errors
------
If any error is raised no output is returned.

=== ========================================================================================
400 Bad Request, including reason why e.g. Unsupported or malformed sort spec <sortBy field>
403 Not authorized, user is no longer logged in.
405 Method not allowed, only GET, PUT, DELETE is allowed.
=== ========================================================================================

Example
-------
.. code-block:: linux

  curl \
  --location --request PUT 'http://localhost/wct/api/v1/target-instances/<target-instance-id>' \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <token>' \
  --data '{"general": {"owner": "demo"}}'