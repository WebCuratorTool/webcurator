Delete Profile (DELETE)
======================
Deletes a specific profile. This is only possible if the profile is not in use. After a delete the profile will have been completly removed from the WCT database.

Request
-------
``https://--WCT_base--/api/v1/profiles/{profile-id}``

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
If any error is raised the profile is not removed.

=== =======================================================================================
404 Not found, non-existing profile-id has been given.
400 Bad request, cannot delete as the profile is still in use.
403 Not authorized, user is no longer logged in.
405 Method not allowed, only POST, GET, PUT, DELETE are allowed.
=== =======================================================================================

Example
-------
.. code-block:: linux

  curl \
  --location --request DELETE 'http://localhost/wct/api/v1/profiles/<profile-id>' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data ''
