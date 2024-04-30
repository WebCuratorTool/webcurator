-----------------
**part: profile**
-----------------
A list of profile properties that belongs to a target or group. This contains the following information:

=============== ======= ========
**profile**
--------------------------------
id              Number  Required
imported        Boolean Required
harvesterType   Number  Required
name            String  Required
overrides       List    Optional
profile         String  Optional
=============== ======= ========

| **id**
| A list of all profiles, with their id and name, can be retrieved with the API :doc: `/guides/apis/api-profiles_GET`.

.. include:: /guides/apis/descriptions/desc-harvesterType.rst

.. include:: /guides/apis/descriptions/desc-overrides.rst

| **profile**
| Required when imported is 'true'. This then contains the imported profile as-is.
