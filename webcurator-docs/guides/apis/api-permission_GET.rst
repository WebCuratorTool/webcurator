Retrieve Permission (GET)
=====================================

Return permisson by identifier.

Request
-------
``https://--WCT_base--/api/v1/permissions/{permission-id}``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst


Response
--------
200: OK

======================== ======= ========
**Body**
-----------------------------------------
id                       Number  Required ---> is this the siteId or the permissionId?
startDate                Date    Required
endDate                  Date    Required
status                   Integer Required ---> what's the relation between status and approved?
urlPatterns              List    Required
harvestAuthorisationId   Number  Required
accessStatus             String  Required
approved                 Boolean Required
copyrightStatement       String  Optional
copyrightUrl             String  Optional
authResponse             String  Optional
openAccessDate           Date    Optional
authorisingAgent         Object  Required
quickPick                Boolean Required
annotations              List    Required
displayName              String  Optional
exclusions               List    Required
======================== ======= ========

| **authorisingAgent** 
| Object containing the id and the name of the authorising agent. 

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
  --location --request GET 'http://localhost/wct/api/v1/permissions/<id>' \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <token>' 
 
