Create token (POST)
===================
Takes username and password as input and returns a token that is valid until <x> minutes of inactivity. The time 
of inactivity (<x>) is configurable in WCT.


Each call to a WCT API (except for authentication for obvious reasons) must contain a valid token. The token must 
be added to the HTTP header of each call:

.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Request
-------
``https://--WCT_base--/auth/v1/token``

Header
^^^^^^
There are no specific header fields for this API.

Body
^^^^

======== ====== ========
**Parameters**
------------------------
username String Required
password String Required
======== ====== ========

| **username**
| Username of to be authenticated user in WCT.

| **password**
| Password of to be authenticated user in WCT.

Response
--------
200: OK

===== ====== ========
**Body**
---------------------
token String Required
===== ====== ========

Errors
------
If any error is raised no token is returned.

=== ==================================================================
400 Bad Request, username and/or password are missing.
403 Not authorized, given username and/or password are not valid.
405 Method not allowed, only POST, DELETE are allowed.
=== ==================================================================

Example
-------
.. code-block:: linux

  curl \
  --location 'http://localhost/wct/auth/v1/token' \
  --form 'username="<username>"' \
  --form 'password="<password>"'