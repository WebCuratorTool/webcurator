Retrieve Profiles (GET)
=======================

Returns all profiles with a subset of the available information based upon given filter.

Request
-------
``https://--WCT_base--/api/v1/profiles``

Header
^^^^^^
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
^^^^
.. include:: /guides/apis/descriptions/desc-query-method.rst

====== ====== ========
filter String Optional
====== ====== ========

| **filter**
| Name of field upon which the result set must be filtered. Only filterable fields maybe given, others are ignored. Filterable fields are:

* showOnlyActive [Boolean] (default: true)
* agency [contains text]
* type [contains text]
		
| Multiple filter fields may be given, but each field only once. If a filter field is given multiple times this will result in an error.
|
| With each field (key) a value must be given which is used to filter. The filter only shows those results that match or contains the given value in the given field. All given characters are used, there are no wild cards.

| **showOnlyActive**
| When this field has the value 'true' only profiles with a state of 1 [active] are shown. When the value of this field is 'false' all profiles are shown irrespective of the state value. 

| **type**
| There are only two types:

* HERITRIX1
* HERITRIX3 (default)

Response
--------
200: OK

========== ====== ========
**Body**
--------------------------
filter     String Required
profiles   List   Optional
========== ====== ========

| **profiles**
| This is a list of found profiles. It could be that no profiles are present. In that case an empty list is returned.
 
The following information is returned per found profile:

============ ======= ========
**Body**
-----------------------------
id           Number  Required
name         String  Required
default      Boolean Required
description  String  Optional
type         String  Required
state        Number  Required
agency       String  Required
============ ======= ========

| **default**
| Only one active profile per type can be the default profile. In other words where the field 'default' has the value 'true'. For all other states this can occur multiple times.

| **type**
| There are only two types:

* HERITRIX1
* HERITRIX3 (default)

.. include:: /guides/apis/descriptions/desc-state_profile.rst

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
  --location 'http://localhost/wct/api/v1/profiles' \
  --header 'Authorization: Bearer <token>' \
  --data '{"filter": { "showOnlyActive": "false" } }'
 
 
 