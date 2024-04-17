| **offset**
| Specifies the number of rows in the result set to skip before limiting starts. 
| Default: 0
| Minimum: 0

| The results page shown is equal to:
| TRUNC(*offset* / *limit*) + 1
 
So the if the *offset* value is not a multiple of the *limit* value then the results page shown is the page upon which the offset row is present. E.g. If *limit* is 10 and *offset* is 19 then the page shown is TRUNC(19/10) + 1 = 2.

Issue: At this point in time *offset* **must** be a multiple of *limit*