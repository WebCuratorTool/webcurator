================ ======= ========
**Body**
---------------------------------
general          List    Optional
harvest results  List    Optional
annotations      List    Optional
display          List    Optional
================ ======= ========

-----------------
**part: general**
-----------------
A list of general properties that belong to a target instance. This contains the following information:

=================== ======= ========
**general**
------------------------------------
owner               String  Optional
flagId              Number  Optional
=================== ======= ========

.. include:: /guides/apis/descriptions/desc-owner.rst

.. include:: /guides/apis/descriptions/desc-flagId.rst

-------------------------
**part: harvest results**
-------------------------
A list of harvest results that belong to a target instance. This contains the following information:

=============== ====== ========
**harvest results**
-------------------------------
HarvestResultId Number Required
state           Number Required
=============== ====== ========

.. include:: /guides/apis/descriptions/desc-state_harvest_result.rst

When updating the field 'state' may only contain the values '1' or '2'. All other values are system set.

.. include:: /guides/apis/descriptions/part-annotations_target_instance.rst

.. include:: /guides/apis/descriptions/part-display_target_instance.rst
