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


public class ItemMfhd extends ItemFormat {
  public ItemMfhd(ActionContext context) {
    super(context);
  }
  
  public void format(Item item, PrintWriter out)  {
    out.println("<marc:record xmlns:marc=\"http://www.loc.gov/MARC21/slim\" type=\"Holdings\" xsi:schemaLocation=\"http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\">");
            
    //standard leader from gmcharlt, not sure what this means.
    // second n is 'n' = no item information in record; set to 'i' if you will have item record in the MFHD in 876 fields 
    out.println("<marc:leader>00000nu  a2200000un 4500</marc:leader>");
    
    //holdingID in 001, bibID in 004
    Util.writeElt(out, "marc:controlfield", new Integer(item.copyId).toString(), "tag", "001");
    Util.writeElt(out, "marc:controlfield", new Integer(item.bibId).toString(), "tag", "004");
    
    //location, collection, call no, copy in 852
    ItemsServlet.printMfhd852(out, item.locationName, item.collectionDescr, item.callNumber, null, new Integer(item.itemId));
    out.println("</marc:record>");

  }
}
