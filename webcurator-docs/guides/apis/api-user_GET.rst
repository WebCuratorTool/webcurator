Retrieve User (GET)
====================

Returns all information about the user identified by the supplied identifier (the last component of the path).

Request
-------
``https://--WCT_base--/api/v1/users/{user-id}``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-query-method.rst


Response
--------
200: OK

======================= ======= ========
**Body**
----------------------------------------
id                      Number  Required
userName                String  Required
firstName               String  Required
lastName                String  Required
email                   String  Required
agency                  String  Required
notificationsByEmail    Boolean Required
tasksByEmail            Boolean Required
title                   String  Required
active                  Boolean Required
forcePasswordChange     Boolean Required
externalAuth            Boolean Required
phone                   String  Required
address                 String  Required
roles                   List    Required
deactivateDate          Date    Required
notifyOnGeneral         Boolean Required
notifyOnHarvestWarnings Boolean Required
======================= ======= ========

| **roles**
| Every object in this list has attributes *id* and *name*. 


Errors
------
If any error is raised no output is returned.

=== ========================================================================================
400 Bad Request, including reason why e.g. Unsupported or malformed sort spec <sortBy field>
404 Not found, if the user with the supplied id does not exist.
403 Not authorized, user is no longer logged in.
405 Method not allowed, only GET is allowed.
=== ========================================================================================

Example
-------
.. code-block:: linux

  curl \
  --location --request GET 'http://localhost/wct/api/v1/users/<id>' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' 
 
 
 
