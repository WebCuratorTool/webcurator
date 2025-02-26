**part: general**
^^^^^^^^^^^^^^^^^
A list of general properties that belong to a group. This contains the following information:

=============== ====== ========
**general**
-------------------------------
id              Number Required
name            String Required
description     String Optional
referenceNumber String Optional
type            String Optional
owner           String Required
ownerInfo       String Optional  
dateFrom        Date   Required
dateTo          Date   Optional
sipType         Number Required
=============== ====== ========

| **id**
.. include:: /guides/apis/descriptions/desc-systemGenerated.rst

.. include:: /guides/apis/descriptions/desc-type_group.rst

.. include:: /guides/apis/descriptions/desc-owner.rst

| **dateFrom**
.. include:: /guides/apis/descriptions/desc-formatDate.rst

| **dateTo**
.. include:: /guides/apis/descriptions/desc-formatDate.rst

| **sipType**
Can have the following values:

+-------+-----------------------------------------------------------+
| Value |                                                           |
+=======+===========================================================+
|  1    | Generate a single Harvest Result for this group [default] |
+-------+-----------------------------------------------------------+
|  2    | Generate one Harvest Result per member                    |
+-------+-----------------------------------------------------------+