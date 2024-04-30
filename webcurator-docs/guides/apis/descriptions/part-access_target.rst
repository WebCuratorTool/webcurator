----------------
**part: access**
----------------
A list of access properties that belong to a target or group. This contains the following information:

=================== ======= ========
**access**
------------------------------------
displayTarget       Boolean Required
accessZone          Number  Required
accessZoneText      String  Required
displayChangeReason String  Optional
displayNote         String  Optional
=================== ======= ========

| **accessZone**
| The access zone of a target is an integer with the following values:

======== ===============
**Zone** **Description**
-------- ---------------
  0      Public
  1      Onsite
  2      Restricted
======== ===============

| **accessZoneText**
| Contains one of the following: Public, Onsite, Restricted, based upon the integer in accessZone.

.. include:: /guides/apis/descriptions/desc-systemGenerated.rst
