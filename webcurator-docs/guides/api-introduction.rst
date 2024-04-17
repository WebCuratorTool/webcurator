============
Introduction
============
The seperation of client- and server-side has led to the development of a number of REST APIs for WCT. This documentation 
describes some generic features of the WCT REST services and gives a description of the available APIs.

Seperating the client- and server-side makes it possible to develop and change the frontend more easily and independently 
from the server-side. Also this allows for the development of batch workflows.

At the moment not all WCT information / functionality is covered by an API. The APIs are focussed on targets and target instances. In the 
future more REST APIs will be added as needed. The impact of this is that some screens will still be 
hardwired to WCT. Especially all configuration screens. The current assumption is that these will not change much and are also not needed
in a remote batch workflow.