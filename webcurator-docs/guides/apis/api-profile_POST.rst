Create Profile (POST)
====================
Create a profile.

Request
-------
``https://--WCT_base--/api/v1/profiles``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
=============== ======= ========
**Body**
--------------------------------
description     String  Optional
profile         String  Required
level           Number  Optional
state           Number  Optional
default         Boolean Optional
agency          String  Required
harvesterType   String  Optional
dataLimitUnit   String  Required
maxFileSizeUnit String  Required
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
201: OK

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

  curl --location 'http://localhost/wct/api/v1/profiles' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data-raw {' \
        "name": "Test profile",' \
        "harvesterType": "HERITRIX3",' \
        "agency": "<some agency>",' \
        "dataLimitUnit": "MB",' \
        "maxFileSizeUnit": "MB",' \
        "profile": "<full json-escaped XML of the profile>"' \
  }'

