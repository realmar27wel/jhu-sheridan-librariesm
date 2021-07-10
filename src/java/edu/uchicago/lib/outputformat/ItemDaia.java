package edu.uchicago.lib.outputformat;

import edu.uchicago.lib.*;

import java.io.*;
import java.net.*;

import java.text.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import javax.sql.DataSource;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import org.xml.sax.*;

public class ItemDaia extends ItemFormat {
  public ItemDaia(ActionContext context) {
    super(context);
  }
  
  public void format(Item item, PrintWriter out)  {
    out.println("<daia:daia xmlns:daia=\"http://ws.gbv.de/daia/\" version=\"0.51\" xsi:schemaLocation=\"http://ws.gbv.de/daia/ http://ws.gbv.de/daia/daia.xsd\"  timestamp=\"" + Util.formatW3cDtf(new java.util.Date(), true) +  "\" >");
    
    // DAIA needs a document element, which needs a URI id. We don't
    // really have a URI for a Horizon bib. We can make one that will be
    // guaranteed unique, although it won't actually resolve in a
    // standard HIP setup. (it does at JHU). 
    out.println("<daia:document id=\"" + Util.escapeXml(context.uriForBib(item.bibId)) + "\">");
    
    // And the individual item we're displaying now please. 
    out.println("<daia:item id=\"" + Util.escapeXml(context.uriForItem(item.itemId)) + 
      "\">");
    // We already have most of the info about this item elsewhere
    // in the package, but might as well repeat it as applicable.
    // stick 'location' in 'department'. Stick 'collection' in
    // 'storage'. Stick extended call number in 'label'. 

    Util.writeElt(out, "daia:label", item.callNumber.simpleCallLabel());    
    Util.writeElt(out, "daia:department", item.locationName);
    Util.writeElt(out, "daia:storage", item.collectionDescr);
    //now the actual services. Sadly at present it's too complicated
    // to figure out if they're REALLY available. We will provide
    // a service for 'request', which is always listed as available,
    // just like in HIP. 
    out.println("  <daia:available delay=\"unknown\" service=\"http://purl.org/NET/daia-ext/request\" href=\"" + Util.escapeXml(context.hipRequestUrl(item.bibId, item.itemId)) + "\"/>");
    out.println("</daia:item>");
    
    
    out.println("</daia:document>");
    
    out.println("</daia:daia>");
  }
  


    

}
