**part: annotations**
^^^^^^^^^^^^^^^^^^^^^
A list of annotations properties that belong to a target. This contains the following information:

============== ====== ========
**annotations**
------------------------------ 
evaluationNote String Optional
harvestType    String Optional
annotations    List   Optional
selection      List   Required
============== ====== ========

| **harvestType**
| Contains one of the following: Event, Subject, Team

| **annotations**
| A list of annotations containing the following information:

===== ======= ========
**annotations**
---------------------- 
date  Date    Required
note  String  Required
user  String  Required
alert Boolean Required
===== ======= ========

| **date**
.. include:: /guides/apis/descriptions/desc-systemGenerated.rst

.. include:: /guides/apis/descriptions/desc-formatDate.rst

.. include:: /guides/apis/descriptions/desc-user.rst

| **user**
.. include:: /guides/apis/descriptions/desc-systemGenerated.rst
			
---------------------
**Part: selection**
---------------------
Contains the following information:

==== ====== ========
**selection**
--------------------
date Date   Required
type String Optional
note String Optional
==== ====== ========

| **date**
.. include:: /guides/apis/descriptions/desc-formatDate.rst

| **type**
| Contains one of the following: Area, Collection, Other collections, Producer type, Publication type.