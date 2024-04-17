-----------------------
**part: harvest state**
-----------------------
A list of harvest state properties that belong to a target instanace. This contains the following information:

==================== ====== ========
**harvest state**
------------------------------------
wctVersion           String Required
captureSystem        String Required
harvestServer        String Required
job                  Number Required
status               String Required
averageKbsPerSecond  Number Required
averageUrisPerSecond Number Required
urlsDownloaded       Number Required
urlsFailed           Number Required
dataDownloaded       String Required
elapsedTime          Time   Required
==================== ====== ========

| **ElapsedTime**
.. include:: /guides/apis/descriptions/desc-formatTime.rst