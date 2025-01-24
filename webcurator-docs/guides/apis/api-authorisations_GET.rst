Retrieve Authorisations (GET)
=============================

Returns a list of all the privileges that a user can have. Together with the highest scope of the privilege.

It is only possilbe to retrieve this list based upon a given and valid token. This is for security purposes.

Request
-------
``https://--WCT_base--/api/v1/authorisations``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-request-body-empty.rst

Response
--------
200: OK

========== ==== ========
**Body**
------------------------
privileges List Required
========== ==== ========

| **privileges**
| Is a list of all the privileges that a user can have. The follwing information is returned per privilige:

========= ====== ========
**Body**
-------------------------
privilege String Required
scope     Number Required
========= ====== ========

.. include:: /guides/apis/descriptions/desc-privilege.rst

.. include:: /guides/apis/descriptions/desc-privilege_scope.rst

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

  TODO
 