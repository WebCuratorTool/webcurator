The body of the request contains the parameters for the retrieval of the target instances. This has been done to 
ensure that the length of the GET URL does not exceed the maximum length. There is currently a proposal for a new 
HTTP method (QUERY) do support exactly this use case. However it is still in draft and is therefore not yet implemented. 
For more information on HTTP Query method see: `https://www.ietf.org/archive/id/draft-ietf-httpbis-safe-method-w-body-03.html <https://www.ietf.org/archive/id/draft-ietf-httpbis-safe-method-w-body-03.html>`_.

Not all libraries and platforms allow the GET method to have anything in the body. In these situations this API also
supports the HTTP method POST. The header must then contain the field ``X-HTTP-Method-Override`` with the value equal to ``GET``.
