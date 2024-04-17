Retrieve Harvest Agents (GET)
===============================

Returns a list of all the harvest agents.

Version
-------
1.0.0

Request
-------
``https://--WCT_base--/api/v1/harvest-agents``

Header
------
.. include:: /guides/apis/descriptions/desc-header-authentication.rst

Body
----
.. include:: /guides/apis/descriptions/desc-request-body-empty.rst

Response
--------
200: OK

============== ==== ========
**Body**
----------------------------
harvest-agents List Optional
============== ==== ========

| **harvest agents**
| This is a list of found harvest agents.
 
The following information is returned per found harvest agent:

================ ====== ========
**Body**
--------------------------------
harvestAgentName String Required
MaxNumHarvests   Number Required
NumHarvests      Number Required
================ ====== ========

Errors
------
If any error is raised no output is returned.

=== ========================================================================================
400 Bad Request, including reason why e.g. Unsupported or malformed sort spec <sortBy field>
403 Not authorized, user is no longer logged in.
405 Method not allowed, only GET is allowed.
=== ========================================================================================

Example
-------
.. code-block:: linux

  <TODO>
 