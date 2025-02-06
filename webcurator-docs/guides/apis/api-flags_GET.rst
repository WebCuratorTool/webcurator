Retrieve Flags (GET)
====================

Returns all flags with a subset of the available information based upon given filter and sorting.

Request
-------
``https://--WCT_base--/api/v1/flags``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-query-method.rst

====== ====== ========
**Parameters**
----------------------
filter String Optional
====== ====== ========

.. include:: /guides/apis/descriptions/desc-request-filter.rst
* agency [exact match only]

Response
--------
200: OK

========== ====== ========
**Body**
--------------------------
filter     String Optional
flags      List   Optional
========== ====== ========

| **flags**
| This is a list of found flags. It could be that no flags are present. In that case an empty list is returned.
 
The following information is returned per found flag:

============ ====== ========
**Body**
----------------------------
id           Number Required
name         String Required
rgb          String Required
agency       String Required
============ ====== ========

.. include:: /guides/apis/descriptions/desc-flagId.rst

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
  --location --request GET 'http://localhost/wct/api/v1/flags' \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <token>' \
  --data ''