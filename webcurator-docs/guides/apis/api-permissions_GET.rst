Retrieve Permissions (GET)
=====================================

Returns all permissons with a subset of the available information based upon given filter.

Request
-------
``https://--WCT_base--/api/v1/permissions``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-query-method.rst

====== ====== ========
filter String Required
page   Number Optional
====== ====== ========

| **filter**
| Parameter which contains the filter criteria. Only filterable fields may be given, others are ignored. 

Filterable fields are:

========= ====== ========
targetId  Number Required
url       String Optional
========= ====== ========

| **page**
| Paging parameter, starts at 0 (also the default value). Pages always contain 10 results.


Response
--------
200: OK

===================== ====== ========
**Body**
-------------------------------------
filter                String Required
page                  Number Required
amount 	              Number Required
permissions           List   Required
===================== ====== ========

| **amount**
| Number of permissions in the search result.  

| **permissions**
| This is a list of matching permissions. If no permissions match the criteria, an empty list is returned.
 
The following information is returned per permission:

======================= ======= ========
**Body**
----------------------------------------
id                      Number  Required
startDate               Date    Required
endDate                 Date    Required
status                  Integer Required
urlPatterns             List    Required
harvestAuthorisationId  Number  Required
======================= ======= ========

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
  --location --request GET 'http://localhost/wct/api/v1/permissions' \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <token>' \
  --data '{"filter": { "targetId": 2, "url": "https://www.kb.nl/" }}'
 
