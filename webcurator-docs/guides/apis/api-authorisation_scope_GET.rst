Retrieve Authorisation Scope (GET)
==================================

Returns the highest authorisation scope of a specific privilege of a user. The result is empty if the user does not have the privilege.

It is only possilbe to retrieve this list based upon a given and valid token. This is for security purposes.

Request
-------
``https://--WCT_base--/api/v1/authorisation/scope``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-query-method.rst

====== ====== ========
filter String Optional
====== ====== ========

| **filter**
| Name of field upon which the result set must be filtered. Only filterable fields maybe given, others are ignored. Filterable fields are:

* privilege [contains text]

.. include:: /guides/apis/descriptions/desc-privilege.rst		

Response
--------
200: OK

====== ====== ========
**Body**
----------------------
filter String Required
scope  Number Required
====== ====== ========

.. include:: /guides/apis/descriptions/desc-privilege_scope.rst

If the user does not have the privilege then the scope is: 500, 'NONE'.

Errors
------
If any error is raised no output is returned.

=== ========================================================================================
400 Bad Request, including reason why e.g. Unsupported or malformed sort spec <sortBy field>
403 Not authorized, user is no longer logged in.
404 Given privilige does not exist.
405 Method not allowed, only GET is allowed.
=== ========================================================================================

Example
-------
.. code-block:: linux

  TODO
 