Delete Target (DELETE)
======================
Deletes a specific target. This is only possible if there are no attached target instances. Also the status of the target must be 'Rejected' or 'Cancelled'. After a delete the target will have been completly removed from the WCT database.

If the target must be set to 'Rejected' or 'Cancelled' then the API :doc:`/guides/apis/api-target_PUT` must be used.

Request
-------
``https://--WCT_base--/api/v1/targets/{target-id}``

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

  curl \
  --location --request DELETE 'http://localhost/wct/api/v1/targets/<target-id>' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data ''