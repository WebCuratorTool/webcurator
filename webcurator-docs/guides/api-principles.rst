==========
Principles
==========

Maturity
========
The WCT APIs are fully RESTful webservices as defined by the REST principals (https://restfulapi.net/). They also adhere to the Richardson maturity model level two. This level of maturity makes use of URIs and HTTP Methods, but does not implement HATEOAS (https://restfulapi.net/richardson-maturity-model/).  

Versioning
==========
For the WCT APIs the version number of the API is part of the URI.

If an API is deprecated and no longer supported the HTTP response code '501: Not Implemented' will be returned. The body of the 
reposnse will be empty.

Authentication
==============
To use an API the user must be authenticated. This is done by getting a session token from WCT via the authentication API. This
token must be provided with each request to an API. This is done via the adding the authorization in the request header as follows:
Authorization: Bearer <token>

<token> is the string of characters (token) returned from the authentication API.

Request
=======
All API requests make use of HTTPS, HTTP is not supported.
 
The format of exchanged data is by default JSON. This applies to both requests and responses.

Response
========
All API responses return a valid HTTP response code (https://en.wikipedia.org/wiki/List_of_HTTP_status_codes). Only if an API requests
results in a correct and valid response '200 OK' is returned. In all other cases the appropriate HTTP error code is returned.

The format of exchanged data is by default JSON. This applies to both requests and responses.
 
Data format
===========
The format of exchanged data is by default JSON. This applies to both requests and responses. No other format is supported.
