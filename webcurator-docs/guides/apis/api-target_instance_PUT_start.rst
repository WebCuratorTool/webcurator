Start Target Instance (PUT)
===========================
Start harvest of a target instance immediately.

Request
-------
``https://--WCT_base--/api/v1/target-instances/{target-instance-id}/start``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^

================ ====== ========
**Parameters**
--------------------------------
harvestAgentName String Required
================ ====== ========

| **harvestAgentName**
| Name of valid harvest agent as defined in application.properties.

The list of possible harvest agent name values can be retrieved with API :doc:`/guides/apis/api-harvest-agents_GET`.

Response
--------
200: OK

Errors
------
If any error is raised no output is returned.

=== ========================================================================================
400 Bad Request, including reason why e.g. Not a stopped target instance.
403 Not authorized, user is no longer logged in.
405 Method not allowed, only GET is allowed.
=== ========================================================================================

Example
-------
.. code-block:: linux

  <TODO>
  curl \
  --location 'http://localhost/wct/api/v1/target-instances/<target-instance-id>/harvest_now' \
  --header 'Authorization: Bearer <token>'
