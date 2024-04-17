Retrieve Profile states (GET)
===========================================

Returns a list of all the states that a profile can have, including the description of each specific state.

Version
-------
1.0.0

Request
-------
``https://--WCT_base--/api/v1/profiles/states``

Header
------
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
----
.. include:: /guides/apis/descriptions/desc-request-body-empty.rst

Response
--------
200: OK

====== ==== ========
**Body**
--------------------
states List Required
====== ==== ========

| **states**
| Is a list of all the states that a profile can have, including the description of each specific state.

.. include:: /guides/apis/descriptions/desc-state_profile.rst

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
  --location --request GET 'http://kb006561i.clients.wpakb.kb.nl:8080/wct/api/v1/profiles/states' \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <token>' \
  --data ''
 