Retrieve Target Instances (GET)
===============================
Returns all target instances with a subset of the available information based upon given filter and sorting.

Request
-------
``https://--WCT_base--/api/v1/target-instances``

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

* targetId (exact match only)
* targetInstanceId [exact match only]
* from [harvestDate is larger or equal to this date]
* to [harvestDate is smaller or equal to this date]
* agency [exact match only]
* userId [exact match only]
* name [contains text]
* flagId [exact match only]
* nonDisplayOnly [Boolean]
* state [List of integers, exact match only]
* qaRecommendation [List of strings, exact match only]

| Multiple filter fields may be given, but each field only once. If a filter field is given multiple times this will result in an error.
|
| With each field (key) a value must be given which is used to filter. The filter only shows those results that match or contains the given value in the given field. All given characters are used, there are no wild cards.

.. include:: /guides/apis/descriptions/desc-userId.rst

.. include:: /guides/apis/descriptions/desc-flagId.rst

.. include:: /guides/apis/descriptions/desc-state_target_instance.rst

.. include:: /guides/apis/descriptions/desc-QARecommendation_target_instance.rst

| **sortBy**
| Name of field upon which the result set must be sorted. The field name must be followed by an indication if the sorting must be ascending (asc) or descending (desc).

| Only sortable fields maybe given, others are ignored. Sortable fields are:
| * name (default)
| * harvestDate
| * runtime
| * dataDownloaded
| * AmountUrls
| * percentageFailed 
| * AmountCrawls

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
instances  List   Optional
========== ====== ========

| **amount**
| Number of total targets in the search result.  

| **instances**
| This is a list of found target instances. It could be that no target instances are returned.
 
The following information is returned per found target instance:

================ ====== ========
**Body**
--------------------------------
id               Number Required
thumbnail        Image  Optional
harvestDate      Date   Required 
name             String Required
owner            String Required
agency           String Optional
state            Number Required
runtime          Time   Optional
dataDownloaded   Number Optional
AmountUrls       Number Optional
percentageFailed Number Optional
AmountCrawls     Number Optional
qaRecommendation String Optional
flagId           Number Optional
================ ====== ========

| **harvestDate**
.. include:: /guides/apis/descriptions/desc-formatDate.rst

.. include:: /guides/apis/descriptions/desc-owner.rst

.. include:: /guides/apis/descriptions/desc-state_target_instance.rst

.. include:: /guides/apis/descriptions/desc-flagId.rst

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
  --location --request GET 'http://kb006561i.clients.wpakb.kb.nl:8080/wct/api/v1/target-instances' \
  --header 'Content-Type: application/json' \
  --header 'Authorization: Bearer FRnUvdZxlJcmvAoLXSqyzUbTW3qQPGhKN7M0nepKH9ZBcRkka1meaA5yxVMMK1T4' \
  --data '{ "filter": { "nonDisplayOnly": false, "states": [5] }, "limit": 5 }'