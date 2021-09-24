# Introduction #

The Servlet also includes a service for borrower information.

The borrower service is only available if you install the full version of the servlet, not the item-information-only version.

Be sure to see [#Security\_Issues](#Security_Issues.md) below.




# Request formats #

You can look up borrowers by borrower#, barcode, second\_id (field in Horizon borrower), or other\_id (field in BorrowerNotes).

  * /ws/borrowers/(borrower#)
  * /ws/borrowers?second\_id=(second\_id)
  * /ws/borrowers?barcode=(barcode)
  * /ws/borrowers?other\_id=(other\_id)

You can also check a pin in one lookup by including pin, eg:

  * /ws/borrowers/(borrower#)?pin=(pin)
  * /ws/borrowers?barcode=(barcode)&pin=(pin)

No borrowers will be returned unless both identifier and pin match in a record.

# Response format #

Any lookup except by borrower# can potentially return more than one borrower, depending on the nature of data in the Horizon db.

The response is just an ad-hoc XML format without a schema, for the moment. The top-level element is `<borrowers>`, which includes zero or more `<borrower>` elements, with certain limited information about each borrower. Right now included are borrower#, btype, location, relevant dates (in W3C Date Format), barcode(s), second\_id, other\_id(s). This will likely be expanded and perhaps formalized in a schema after we figure out what we need through 'cowpaths'.

# Security Issues #

The borrower service includes access confidential information, including pin. It's important that it only be available to authorized clients, and that all communications take place in https so confidential information is not sent in the clear accross the network.

At present, there is no access control built into the servlet. Here at JHU we deploy the servlet with an apache front-end, and use apache directives to apply access restrictions.  Other methods of access control may be built into the servlet at a later date.

If you don't understand how to apply access control or have no way to, please don't install the version of the servlet with borrower service, install the items-only version instead.

Example apache access control:

```
<Location /ws/borrowers>
  # Restrict access to borrowers info to certain IPs,
  # and to http auth login, and require https. HTTPS should
  # be required as sensitive info will be transmitted over the
  # network. 
  
  # require SSL for /ws/borrowers, redirect to SSL  
  RewriteEngine On
  RewriteCond %{HTTPS} !=on
  RewriteRule .* https://%{SERVER_NAME}%{REQUEST_URI} [L,R]
  
  Order deny,allow
  Deny from all
  Allow from x.x.x.x
  Allow from x.x.x.x

  # Apache basic or digest auth features could, and probably
  # should also be used to require a password in addition to
  # IP address auth.  IP addr is spoofable by a client. 
  # basic auth is secure ONLY if used over https. 
  
  
</Location>
```