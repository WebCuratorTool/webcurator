**part: general**
^^^^^^^^^^^^^^^^^
A list of general properties that belong to a target. This contains the following information:

=================== ======= ========
**general**
------------------------------------
id                  Number  Required
creationDate        Date    Required
name                String  Required
description         String  Optional
referenceNumber     String  Optional
RunOnApproval       Boolean Required
automatedQA         Boolean Required
owner               String  Required
state               Number  Required
autoPrune           Boolean Required
referencdCrawl      Boolean Required
requestToArchivists String  Optional
=================== ======= ========

| **id**
.. include:: /guides/apis/descriptions/desc-systemGenerated.rst

| **creationDate**
.. include:: /guides/apis/descriptions/desc-systemGenerated.rst

.. include:: /guides/apis/descriptions/desc-formatDate.rst

.. include:: /guides/apis/descriptions/desc-owner.rst

.. include:: /guides/apis/descriptions/desc-state_target.rst

| **RunOnApproval**
| The default is 'false'.

| **automatedQA**
| The default is 'false'.

.. include:: /guides/apis/descriptions/desc-state_target.rst

| **autoPrune**
| The default is 'false'.

| **refCrawl**
| The default is 'false'.