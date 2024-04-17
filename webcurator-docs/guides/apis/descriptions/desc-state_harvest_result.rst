| **state**
| The state of a harvest result is an integer with the following values:

========= ===============
**State** **Description**
--------- ---------------
  0       Unassessed
  1       Endorsed
  2       Rejected
  3       Indexing
  4       Aborted
  5       Crawling
  6       Modifying
========= ===============

The list of possible state values for harvest results can be retrieved with API :doc:`/guides/apis/api-harvest_result_states_GET`.