Retrieve Targets (GET)
======================
Returns all targets with a subset of the available information based upon given filter and sorting.

Request
-------
``https://--WCT_base--/api/v1/targets``_

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

| **filter**
| Name of field upon which the result set must be filtered. Only filterable fields maybe given, others are ignored. Filterable fields are:

* targetId [exact match only]
* name [contains text]
* seed [contains text]
* agency [exact match only]
* userId [exact match only]
* description [contains text]
* groupName [contains text]
* nonDisplayOnly [Boolean]
* state [List of integers, exact match only]

| Multiple filter fields may be given, but each field only once. If a filter field is given multiple times this will result in an error.
|
| With each field (key) a value must be given which is used to filter. The filter only shows those results that match or contains the given value in the given field. All given characters are used, there are no wild cards.

.. include:: /guides/apis/descriptions/desc-userId.rst

.. include:: /guides/apis/descriptions/desc-state_target.rst

| **sortBy**
| Name of field upon which the result set must be sorted. The field name must be followed by an indication if the sorting must be ascending (asc) or descending (desc).

| Only sortable fields maybe given, others are ignored. Sortable fields are:
| * name (default)
| * creationDate

Only one sort field may be given as input. If multiple sort fields are given then this will result in an error. Example: sortBy=Name,asc

.. include:: /guides/apis/descriptions/desc-request-offset.rst

.. include:: /guides/apis/descriptions/desc-request-limit.rst

Response
--------
200: OK

========== ====== ========
**Body**
--------------------------
filter     String Optional
sortBy     String Optional
offset     Number Optional
limit	   Number Optional
amount 	   Number Required
targets    List   Optional
========== ====== ========

| **amount**
| Number of total targets in the search result.  

| **targets**
| This is a list of found targets. It could be that no targets are returned.
 
The following information is returned per found target:

============ ====== ========
**Body**
----------------------------
targetId     Number Required
creationDate Date   Required 
name         String Required
agency       String Required
owner        String Required
state        Number Required
seeds        List   Required
============ ====== ========

| **creationDate**
.. include:: /guides/apis/descriptions/desc-formatDate.rst

.. include:: /guides/apis/descriptions/desc-state_target.rst

| **seeds**
| A list of seeds containing the following information:

============== ======= ========
**seeds**
-------------------------------
seed           URL     Required
primary        Boolean Required
============== ======= ========

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
  --location --request GET 'http://localhost/wct/api/v1/targets' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data '{"filter": {"states": [5], "agency": "KB" }, "limit": 5, "sortBy": "creationDate,asc" }'

.. code-block:: linux

  curl \
  --location 'http://localhost/wct/api/v1/targets' \
  --header 'X-HTTP-Method-Override: GET' \
  --header 'Authorization: Bearer <token>' \
  --header 'Content-Type: application/json' \
  --data '{"filter": {"states": [5], "agency": "KB" }, "limit": 5, "sortBy": "creationDate,asc" }'