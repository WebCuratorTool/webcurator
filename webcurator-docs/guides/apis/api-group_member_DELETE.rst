Remove Group Member (DELETE)
============================
Remove a target or group from a group.

Request
-------
| ``https://--WCT_base--/api/v1/groups/{group-id}/members/{target-id}`` OR 
| ``https://--WCT_base--/api/v1/groups/{group-id}/members/{group-id}``

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
If any error is raised no output is returned. Nor is the member removed from the group.

=== ==========================================================================
400 No group or target with id <id> exists.
403 Not authorized, user is no longer logged in.
405 Method not allowed, only POST, GET, PUT, DELETE are allowed.
=== ==========================================================================

Example
-------
.. code-block:: linux

  curl \
  --location --request DELETE 'http://localhost/wct/api/v1/groups/<group-id>/members/<target-id>' \
  --header 'Authorization: Bearer <token>' \
  --data ''