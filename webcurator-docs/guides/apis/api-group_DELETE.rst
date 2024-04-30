Delete Group (DELETE)
======================
Deletes a specific group.

Version
-------
1.0.0

Request
-------
``https://--WCT_base--/api/v1/groups/{group-id}``

Header
------
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
----
.. include:: /guides/apis/descriptions/desc-request-body-empty.rst

Response
--------
200: OK

.. include:: /guides/apis/descriptions/desc-response-body-empty.rst

Errors
------
If any error is raised no output is returned. Nor is the target removed.

=== =======================================================================================
400 Bad request, non-existing target-id has been given.
400	Bad request, cannot delete as there are still target instances connected to the target.
400	Bad request, cannot delete as target is not rejected or cancelled. 
403 Not authorized, user is no longer logged in.
405 Method not allowed, only POST, GET, PUT, DELETE are allowed.
=== =======================================================================================

Example
-------
.. code-block:: linux

  TODO