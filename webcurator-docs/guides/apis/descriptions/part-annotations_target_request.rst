---------------------
**part: annotations**
---------------------
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
note  String  Required
alert Boolean Required
===== ======= ========
			
---------------------
**part: selection**
---------------------
Contains the following information:

==== ====== ========
**selection**
--------------------
type String Optional
note String Optional
==== ====== ========

| **type**
| Contains one of the following: Area, Collection, Other collections, Producer type, Publication type.