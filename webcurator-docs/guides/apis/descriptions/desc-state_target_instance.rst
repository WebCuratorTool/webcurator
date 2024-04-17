| **state**
| The state of a target instance is an integer with the following values:

========= ===============
**State** **Description**
--------- ---------------
  1       Scheduled
  2       Queued
  3       Running
  4       Paused
  5       Harvested
  6       Aborted
  7       Endorsed
  8       Rejected
  9       Archived
 10       Archiving
========= ===============

The list of possible state values for targets can be retrieved with API :doc:`/guides/apis/api-target_instance_states_GET`.