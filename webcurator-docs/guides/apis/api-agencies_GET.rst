Retrieve Agencies (GET)
============================

Returns a list of all the agencies.

Request
-------
``https://--WCT_base--/api/v1/agencies``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-request-body-empty.rst

Response
--------
200: OK

======== ==== ========
**Body**
----------------------
agencies List Optional
======== ==== ========

| **agencies**
| This is a list of found agencies. It could be that no agencies are present. In that case an empty list is returned.
 
The following information is returned per found agency:

============ ====== ========
**Body**
----------------------------
id           Number Required
name         String Required
address      String Required
============ ====== ========

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
  --location --request GET 'http://localhost/wct/auth/v1/agencies' \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <token>' \
  --data ''
 