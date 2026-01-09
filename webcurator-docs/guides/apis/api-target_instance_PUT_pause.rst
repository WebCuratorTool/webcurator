Pause Target Instance (PUT)
===========================
Pauses a running target instance.

Request
-------
``https://--WCT_base--/api/v1/target-instances/{target-instance-id}/pause``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-request-body-empty.rst

Response
--------
200: OK

Errors
------
If any error is raised no output is returned.

=== ========================================================================================
400 Bad Request, including reason why e.g. Not a running target instance.
403 Not authorized, user is no longer logged in.
405 Method not allowed, only GET is allowed.
=== ========================================================================================

Example
-------
.. code-block:: linux

  curl \
  --location 'http://localhost/wct/api/v1/target-instances/<target-instance-id>/pause' \
  --header 'Authorization: Bearer <token>'
