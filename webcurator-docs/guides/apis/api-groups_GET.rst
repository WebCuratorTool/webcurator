Retrieve Groups (GET)
=====================

Returns all groups with a subset of the available information based upon given filter.

Version
-------
1.0.0

Request
-------
``https://--WCT_base--/api/v1/groups``

Header
------
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
----
.. include:: /guides/apis/descriptions/desc-query-method.rst

====== ====== ========
filter String Optional
offset Number Optional
limit  Number Optional
====== ====== ========

| **filter**
| Name of field upon which the result set must be filtered. Only filterable fields maybe given, others are ignored. Filterable fields are:

* groupId [exact match only]
* name [contains text]
* agency [exact match only]
* userId [exact match only]
* memberOf [contains text]
* type [contains text]
* nonDisplayOnly [Boolean]
* state [List of integers, exact match only]

| Multiple filter fields may be given, but each field only once. If a filter field is given multiple times this will result in an error.
|
| With each field (key) a value must be given which is used to filter. The filter only shows those results that match or contains the given value in the given field. All given characters are used, there are no wild cards.

.. include:: /guides/apis/descriptions/desc-userId.rst

.. include:: /guides/apis/descriptions/desc-groupType.rst

.. include:: /guides/apis/descriptions/desc-request-offset.rst

.. include:: /guides/apis/descriptions/desc-request-limit.rst

.. include:: /guides/apis/descriptions/desc-type_group.rst

.. include:: /guides/apis/descriptions/desc-state_group.rst

Response
--------
200: OK

========== ====== ========
**Body**
--------------------------
filter     String Required
offset     Number Required
limit	   Number Required
amount 	   Number Required
groups     List   Optional
========== ====== ========

| **amount**
| Number of total groups in the search result.  

| **groups**
| This is a list of found groups. It could be that no groups are present. In that case an empty list is returned.
 
The following information is returned per found group:

============ ====== ========
**Body**
----------------------------
groupId      Number Required
name         String Required
type         String Required
agency       String Required
owner        String Required
state        Number Required
============ ====== ========

.. include:: /guides/apis/descriptions/desc-type_group.rst

.. include:: /guides/apis/descriptions/desc-owner.rst

.. include:: /guides/apis/descriptions/desc-state_group.rst

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

  TODO
 
 