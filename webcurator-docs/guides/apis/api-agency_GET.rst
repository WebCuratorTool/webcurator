Retrieve Agency (GET)
====================
Returns all information for a specific agency.

Request
-------
``https://--WCT_base--/api/v1/agencies/{agency-id}``


Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-request-body-empty.rst

Response
--------
200: OK

====================== ======= ========
**Body**
---------------------------------------
id                     Number  Required
name                   String  Required
address                String  Required
phone                  String  Optional
agencyUrl              String  Optional
agencyLogoUrl          String  Optional
email                  String  Optional
fax                    String  Optional
showTasks              Boolean Required
defaultDescriptionType String  Optional
====================== ======= ========

| **showTasks** indicates whether users belonging to this agency get to see 
| tasks in the in-tray screen.

| **defaultDescriptionType** contains the default value for dc.type in the
| target description tab, when users of this agency create a new target.


Errors
------
If any error is raised no output is returned.

=== ==========================================================================
404 Not found, non-existing agency-id has been given.
403 Not authorized, user is no longer logged in.
405 Method not allowed, only GET is allowed.
=== ==========================================================================

Example
-------
.. code-block:: linux

  curl \
  --location --request GET 'http://localhost/wct/auth/v1/agencies/<agency-id>' \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <token>' \
  --data ''
  

