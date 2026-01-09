**part: general**
^^^^^^^^^^^^^^^^^
A list of general properties that belong to a target instance. This contains the following information:

=================== ======= ========
**general**
------------------------------------
id                  Number  Required
name                String  Required
scheduleStartDate   Date    Required
actualStartDate     Date    Optional
priority            String  Required
owner               String  Required
agency              String  Required
state               Number  Required
automatedQA         Boolean Required
bandwidthPercentage String  Required
flagId              Number  Required
=================== ======= ========

| **scheduleStartDate**
.. include:: /guides/apis/descriptions/desc-formatDate.rst

| **actualStartDate**
.. include:: /guides/apis/descriptions/desc-formatDate.rst

.. include:: /guides/apis/descriptions/desc-owner.rst

.. include:: /guides/apis/descriptions/desc-state_target_instance.rst

| **automatedQA**
| The default is 'false'.

.. include:: /guides/apis/descriptions/desc-flagId.rst