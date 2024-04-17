-----------------
**part: general**
-----------------
A list of general properties that belong to a group. This contains the following information:

=============== ====== ========
**general**
--------------------------------
id              Number Required
name            String Required
description     String Optional
referenceNumber String Optional
type            String Optional
owner           String Required
owner_info      String Optional  
dateFrom        Date   Required
dateTo          Date   Optional
harvestType     Number Required
=============== ====== ========

.. include:: /guides/apis/descriptions/desc-type_group.rst

.. include:: /guides/apis/descriptions/desc-owner.rst

| **dateFrom**
.. include:: /guides/apis/descriptions/desc-formatDate.rst

| **dateTo**
.. include:: /guides/apis/descriptions/desc-formatDate.rst

| **harvestType**
Can have the follwing values:
*1 => Generate a single Harvest Result for this group [default]
*2 => Generate one Harvest Result per member