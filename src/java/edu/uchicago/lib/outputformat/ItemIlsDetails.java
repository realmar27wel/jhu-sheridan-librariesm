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

import com.dynix.util.HtmlEncoder;

public class ItemIlsDetails extends ItemFormat {
  public ItemIlsDetails(ActionContext context) {
    super(context);
  }
  
  public void format(Item item, PrintWriter out)  {
    out.println("<ilsitem:description xmlns:ilsitem=\"http://purl.org/NET/ils-holdings-schema/1\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"  xsi:schemaLocation=\"http://purl.org/dc/elements/1.1 http://dublincore.org/schemas/xmls/qdc/2003/04/02/dc.xsd http://purl.org/NET/ils-holdings-schema/1 http://purl.org/NET/ils-holdings-schema/1/schema\">");
       
      commonBlock(out, item.locationCode, item.locationName, item.collectionCode, item.collectionDescr, item.callNumberTypeCode, item.callNumberTypeName);
       
         out.println("<ilsitem:itemType>");
         if ( item.itemTypeCode != null ) {
           Util.writeElt(out, "dc:identifier", item.itemTypeCode);
         }
         if ( item.itemTypeName != null ) {
             Util.writeElt(out, "dc:title", item.itemTypeName);
           }
         out.println("</ilsitem:itemType>");
  
         out.println("<ilsitem:itemStatus>");
           if ( item.itemStatusCode != null ) {
             Util.writeElt(out, "dc:identifier", item.itemStatusCode);
           }
           if ( item.itemStatusName != null ) {
             Util.writeElt(out, "dc:title", item.itemStatusName);
           }
         out.println("</ilsitem:itemStatus>");               
       
       out.println("</ilsitem:description>");
  }
  
  public static void commonBlock(PrintWriter out, String locationCode, String locationName, String collectionCode, String collectionDescr, String callNumberTypeCode, String callNumberTypeName) {
    out.println("<ilsitem:location>");
    if ( locationCode != null) {
     Util.writeElt(out, "dc:identifier", locationCode);
    }
    if ( locationName != null) {
     Util.writeElt(out, "dc:title", locationName);
    }
    out.println("</ilsitem:location>");

    out.println("<ilsitem:collection>");
    if ( collectionCode != null ) {
     Util.writeElt(out, "dc:identifier", collectionCode);
    }
    if ( collectionDescr != null) {
     Util.writeElt(out, "dc:title", collectionDescr);
    }         
    out.println("</ilsitem:collection>"); 
    
     if ( callNumberTypeCode != null || callNumberTypeName != null) {
         out.println("<ilsitem:shelfMarkType>");
         if ( callNumberTypeCode != null ) {
           Util.writeElt(out, "dc:identifier", callNumberTypeCode);
         }
         if ( callNumberTypeName != null ) {
           Util.writeElt(out, "dc:title", callNumberTypeName);
         }
       out.println("</ilsitem:shelfMarkType>");
     }
  }
}
