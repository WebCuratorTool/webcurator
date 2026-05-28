Update Profile (PUT)
====================
Update a profile.

Request
-------
``https://--WCT_base--/api/v1/profiles/{id}``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
=============== ======= ========
**Body**
--------------------------------
description     String  Optional
profile         String  Optional
level           Number  Optional
state           Number  Optional
default         Boolean Optional
agency          String  Optional
harvesterType   String  Optional
dataLimitUnit   String  Optional
maxFileSizeUnit String  Optional
imported        Boolean Optional
=============== ======= ========

| **profile**
| The full profile as XML string.

| **dataLimitUnit**
| The unit used to specify the data limit in the profile. Possible values: B, KB, MB and GB.

| **maxFileSizeUnit**
| The unit used to specify the maximum file size limit in the profile. Possible values: B, KB, MB and GB.


Response
--------
200: OK

.. include:: /guides/apis/descriptions/desc-response-body-empty.rst

The HTTP Header Location contains the URL to retrieve (GET) the inserted profile 
as described in :doc:`/guides/apis/api-profile_GET`.

Errors
------
If any error is raised no output is returned. Nor is the profile created.

=== =======================================================================================
400	Bad request, required value <element> has not been given.
403 Not authorized, user is no longer logged in.
405 Method not allowed, only POST, GET, PUT, DELETE are allowed.
=== =======================================================================================

Example
-------
.. code-block:: linux

  curl -XPUT --location 'http://localhost/wct/api/v1/profiles/<id>' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data-raw {' \
        "dataLimitUnit": "GB",' \
  }'

