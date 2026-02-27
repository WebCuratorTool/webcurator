Retrieve Profile (GET)
=======================

Returns the full profile corresponding to the supplied identifier.

Request
-------
``https://--WCT_base--/api/v1/profiles/{profile-id}``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-query-method.rst

Response
--------
200: OK

.. include:: /guides/apis/descriptions/desc-profile.rst

Errors
------
If any error is raised no output is returned.

=== ==========================================================================
404 Not found, non-existing profile-id has been given.
403 Not authorized, user is no longer logged in.
405 Method not allowed, only GET is allowed.
=== ==========================================================================

Example
-------
.. code-block:: linux

  curl \
  --location 'http://localhost/wct/api/v1/profiles/<profile-id>' \
  --header 'Authorization: Bearer <token>'



 
