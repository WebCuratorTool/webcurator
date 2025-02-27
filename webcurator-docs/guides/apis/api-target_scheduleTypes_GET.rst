Retrieve Target schedule types (GET)
====================================

Returns a list of all the schedule types that a target can have, including the description of each specific schedule type.

Request
-------
``https://--WCT_base--/api/v1/targets/schedule-types``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-request-body-empty.rst

Response
--------
200: OK

============= ==== ========
**Body**
---------------------------
scheduleTypes List Required
============= ==== ========

| **scheduleTypes**
| Is a list of all the scheduleTypes that a target can have, including the description of each specific schedule type.

.. include:: /guides/apis/descriptions/desc-scheduleType.rst

Errors
------
If any error is raised no output is returned.

=== ========================================================================================
400 Bad Request, including reason why e.g. Unsupported or malformed sort spec <sortBy field>
403 Not authorized, user is no longer logged in.
405 Method not allowed, only GET is allowed.
=== ========================================================================================

Example
-------
.. code-block:: linux

  curl \
  --location --request GET 'http://localhost/wct/api/v1/targets/schedule-types' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data ''
 