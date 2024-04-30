| **overrides**
| Required when imported is 'false'.

A list of profile overwrites containing the following information:

======= ========= ========
**overwrites**
--------------------------
id      String    Required
value   Dependent Optional
unit    String    Optional
enabled Boolean   Required
======= ========= ========

| **id**
| The identifier of the overwrite in the WCT database. E.g. po_h3_time_limit.

| **value**
| The value type can be boolean, number, string or list. It is dependent on the specific override. E.g. the override for 'maxPathDepth' is a number, the override for 'includedUrls' is a List of Strings.

| **unit**
| Some overrides have specific units, E.g. 'SECONDS'.
