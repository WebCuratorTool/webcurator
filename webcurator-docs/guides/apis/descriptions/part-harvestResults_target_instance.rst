**part: harvest results**
^^^^^^^^^^^^^^^^^^^^^^^^^
A list of harvest results that belong to a target instance. This contains the following information:

=============== ====== ========
**harvest results**
-------------------------------
id              Number Required
number          Number Required
creationDate    Date   Required
derivedFrom     Number Required
owner           String Required
note            String Optional
state           Number Required
=============== ====== ========

| **creationDate**
.. include:: /guides/apis/descriptions/desc-formatDate.rst

.. include:: /guides/apis/descriptions/desc-owner.rst

.. include:: /guides/apis/descriptions/desc-state_harvest_result.rst