**part: schedule**
^^^^^^^^^^^^^^^^^^
Consists of the following information:

=================== ======= ========
**schedule**
------------------------------------
harvestOptimization Boolean Optional
harvestNow          Boolean Optional
schedules           List    Optional
=================== ======= ========

| **schedules**
| A list of schedules that belong to a target. If, upon updating a target, an existing schedule is not supplied, it will be deleted. Supplied schedules that do not match an existing schedule will be added. Each schedule contains the following fields:

================= ======= ========
**schedules**
----------------------------------
id                Number  Required
cron              String  Required
startDate         Date    Required
endDate           Date    Optional
type              Number  Required
owner             String  Required
nextExecutionDate Date    Required
lastExecutionDate Date    Optional
================= ======= ========

| **id**
This field is system-generated and may not be given at creation time.

.. include:: /guides/apis/descriptions/desc-cron.rst

| **startDate** 
.. include:: /guides/apis/descriptions/desc-formatDate.rst

| **endDate** 
.. include:: /guides/apis/descriptions/desc-formatDate.rst

.. include:: /guides/apis/descriptions/desc-scheduleType.rst

.. include:: /guides/apis/descriptions/desc-owner.rst

| **nextExecutionDate** 
.. include:: /guides/apis/descriptions/desc-formatDate.rst

| **lastExecutionDate** 
.. include:: /guides/apis/descriptions/desc-formatDate.rst
