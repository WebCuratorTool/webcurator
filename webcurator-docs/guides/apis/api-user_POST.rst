Create User (POST)
====================

Creates a new user.

Request
-------
``https://--WCT_base--/api/v1/users``

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
userName                String  Required
firstName               String  Required
lastName                String  Required
email                   String  Required
agency                  String  Required
password                String  Required
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
| Every object in this list has attributes *id* and *name*. 

| **active**
| The default value for this key is *true*, which means a newly created user is able to login
| immediately by default.


Errors
------

=== ========================================================================================
400 Bad Request, including reason why e.g. Unsupported or malformed sort spec <sortBy field>
403 Not authorized, user is no longer logged in.
405 Method not allowed, only GET is allowed.
=== ========================================================================================

Example
-------
.. code-block:: linux

  curl \
  --location --request POST 'http://localhost/wct/api/v1/users' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data-raw '{ \
        "userName": "testuser", \
        "firstName": "foo", \
        "lastName": "bar", \
        "email": "testuser@foo.bar", \
        "password": <password>, \ 
        "agency": "fbi", \
        "active": false\
  }'

 
 
 
