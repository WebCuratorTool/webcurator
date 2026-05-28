Delete User (DELETE)
======================
Deletes a specific user. This is only possible if there are no other objects, such as targets, related to the user. After a delete the user will have been completly removed from the WCT database.


Request
-------
``https://--WCT_base--/api/v1/users/{user-id}``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-request-body-empty.rst

Response
--------
200: OK

.. include:: /guides/apis/descriptions/desc-response-body-empty.rst

Errors
------
If any error is raised no output is returned. Nor is the user removed.

=== =======================================================================================
400 Bad request, cannot delete as there are still objects connected to the user.
404 Not found, non-existing user-id has been given.
403 Not authorized, user is no longer logged in.
405 Method not allowed, only POST, GET, PUT, DELETE are allowed.
=== =======================================================================================

Example
-------
.. code-block:: linux

  curl \
  --location --request DELETE 'http://localhost/wct/api/v1/users/<user-id>' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data ''
