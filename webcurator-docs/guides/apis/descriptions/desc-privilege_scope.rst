| **scope**
| The scope of a privilege is a number with the following values:

========= ===============
**Scope** **Description**
--------- ---------------
    0     ALL
  100     AGENCY
  200     OWNER
  500     NONE
========= ===============

Most privileges can be adjusted to three levels of scope: All, Agency, or Owner. If the scope of an active permission is set to All then the permission applies to all objects; if it is set to Agency then it applies only to those objects that belong to the same agency as the user; if it is set to Owner it applies only to those owned by that user.