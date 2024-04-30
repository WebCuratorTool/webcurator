Retrieve Users (GET)
====================

Returns all users with a subset of the available information based upon given filter and sorting.

Version
-------
1.0.0

Request
-------
``https://--WCT_base--/api/v1/users``

Header
------
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
----
.. include:: /guides/apis/descriptions/desc-query-method.rst

====== ====== ========
filter String Optional
====== ====== ========

| **filter**
| Name of field upon which the result set must be filtered. Only filterable fields maybe given, others are ignored. Filterable fields are:

* agency [exact match only]

| With each filter field (key) a value must be given which is used to filter. The filter only shows those results that match or contains the given value in the given field. All given characters are used, there are no wild cards.

Response
--------
200: OK

========== ====== ========
**Body**
--------------------------
filter     String Optional
users      List   Optional
========== ====== ========

| **users**
| This is a list of found users. It could be that no users are present. In that case an empty list is returned.
 
The following information is returned per found user:

============ ====== ========
**Body**
----------------------------
userId       Number Required
userName     String Required
firstName    String Required
lastName     String Required
email        String Required
agency       String Required
============ ====== ========

.. include:: /guides/apis/descriptions/desc-userId.rst

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
  --location --request GET 'http://localhost/wct/api/v1/users' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data '{"filter": {"agency": "KB" } }'
 
 
 