A Java servlet which provides web services for accessing information from the Horizon ILS.
 - Borrower Info
 - Holdings Info

The main service provided is item information for Horizon ILS items in DLF ils-di (dlfexpanded) 
format, among other formats.

A borrower information service is now included as well.

Based on code by Tod Olson, significantly expanded by Jonathan Rochkind. Refactored by Dazhi Jiao to
use maven to build the project. 

See the ./docs directory for more information. 

If you are interested in collaborating on this code, feel free to let me know.

## Build

First, create a context.xml file at `src/main/etc/context.xml`. Use `src/main/etc/context.example.xml` 
as an example. 

Run the following to create a war file in the directory `target`

```
mvn package 
```

War file will be stored in `target`

## Test

To test it on jetty. 

```
mvn jetty:run
```

You may also set it up in IntelliJ to run the command in debug mode to use it with a debugger. 
