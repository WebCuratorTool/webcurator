Delete Target instance (DELETE)
===============================
Deletes a specific target instance. This is only possible if the status of the target instance is 'Scheduled' or 'Queued'. After a delete the target instance will have been completly removed from the WCT database.

Version
-------
1.0.0

Request
-------
``https://--WCT_base--/api/v1/target_instances/{target_instance-id}``

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
400 Bad request, non-existing target_instnace-id has been given.
400	Bad request, cannot delete as target instance is not scheduled, aborted or rejected. 
403 Not authorized, user is no longer logged in.
405 Method not allowed, only GET, PUT, DELETE are allowed.
=== =======================================================================================

Example
-------
.. code-block:: linux

  <TODO>