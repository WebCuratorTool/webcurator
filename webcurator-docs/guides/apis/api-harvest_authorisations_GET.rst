Retrieve Harvest Authorisations (GET)
=====================================

Returns all harvest authorisations with a subset of the available information based upon given filter and sorting.

Request
-------
``https://--WCT_base--/api/v1/harvest-authorisations``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-query-method.rst

====== ====== ========
filter String Optional
sortBy String Optional
offset Number Optional
limit  Number Optional
====== ====== ========

.. include:: /guides/apis/descriptions/desc-request-filter.rst
* id [exact match only]
* name [contains text]
* authorisingAgent [contains text]
* orderNo [exact match]
* agency [exact match only]
* showDisabled [boolean]
* URLpattern [contains text]
* permissionsFilereference [contains text]
* permissionStates [List of integers, exact match only]

.. include:: /guides/apis/descriptions/desc-state_harvest_authoristion.rst

.. include:: /guides/apis/descriptions/desc-request-sortBy.rst
* name (default)
* creationDate

.. include:: /guides/apis/descriptions/desc-request-offset.rst

.. include:: /guides/apis/descriptions/desc-request-limit.rst

Response
--------
200: OK

===================== ====== ========
**Body**
-------------------------------------
filter                String Required
sortBy                String Optional
offset                Number Required
limit	              Number Required
amount 	              Number Required
harvestAuthorisations List   Optional
===================== ====== ========

| **amount**
| Number of total groups in the search result.  

| **harvestAuthorisations**
| This is a list of found harvest authorisations. It could be that no harvest authorisations are present. In that case an empty list is returned.
 
The following information is returned per found harvest authorisation:

======================= ====== ========
**Body**
---------------------------------------
id                      Number Required
creationDate            Date   Required
name                    String Required
authorisingAgents       List   Optional
orderNo                 String Optional
permissionStates        List   Optional
======================= ====== ========

| **authorisingAgents**
| List of of id's of authorising agents. The list of all agencies can be retrieved with API :doc: `api-agencies_GET.rst`.

| **permissionStates**
| List of permission states as given to the permissons in the harvest authorisation.

.. include:: /guides/apis/descriptions/desc-state_harvest_authoristion.rst

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
  --location --request GET 'http://localhost/wct/api/v1/harvest-authorisations' \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer <token>' \
  --data '{"filter": { "permissionStates": [2] }, "limit": 5, "sortBy": "creationDate,asc" }'
 