Update User (PUT)
====================

Updates the new user identified by the supplied identifier.

Request
-------
``https://--WCT_base--/api/v1/users/{user-id}``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
======================= ======= ========
**Body**
----------------------------------------
userName                String  Optional
firstName               String  Optional
lastName                String  Optional
email                   String  Optional
agency                  String  Optional
password                String  Optional
notificationsByEmail    Boolean Optional
tasksByEmail            Boolean Optional
title                   String  Optional
active                  Boolean Optional
externalAuth            Boolean Optional
phone                   String  Optional
address                 String  Optional
roles                   List    Optional
deactivateDate          Date    Optional
notifyOnGeneral         Boolean Optional
notifyOnHarvestWarnings Boolean Optional
======================= ======= ========

| **roles**
| Every object in this list has attributes *id* and *name*, although for PUT and POST *name* is optional.


Response
--------
200: OK


Errors
------

=== ========================================================================================
400 Bad Request, including reason why. 
403 Not authorized, user is no longer logged in.
404 There is no user corresponding to the supplied identifier.
405 Method not allowed, only GET is allowed.
=== ========================================================================================

Example
-------
.. code-block:: linux

  curl \
  --location --request PUT 'http://localhost/wct/api/v1/users/<id>' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data-raw '{ \
        "firstName": "foo", \
  }'

 
 
 
