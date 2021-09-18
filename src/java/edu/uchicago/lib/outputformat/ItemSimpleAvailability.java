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

public class ItemSimpleAvailability extends ItemFormat {
  public ItemSimpleAvailability(ActionContext context) {
    super(context);
  }
  
  public void format(Item item, PrintWriter out)  {      
    
      out.println("<dlf:simpleavailability>");
      out.println("  <dlf:identifier>" + item.itemId + "</dlf:identifier>");          
      
      
      out.println("  <dlf:location>" + Util.escapeXml(item.locationName) + " -- " + Util.escapeXml(item.collectionDescr) );      
      String callLabel = item.callNumber.simpleCallLabel();
      if ( callLabel != null ) {
          out.println(": " + Util.escapeXml(callLabel));
      }
      out.println("</dlf:location>");
      
      
      out.println("  <dlf:availabilitystatus>" + ItemsServlet.itemStatusToDlfTranslator.itemStatusToDlf(item.itemStatus) + "</dlf:availabilitystatus>"); 
      out.println("  <dlf:availabilitymsg>" + Util.escapeXml(item.itemStatusDescr) + "</dlf:availabilitymsg>");
      if ( item.dueDate != null) {
        out.println("  <dlf:dateavailable>" + Util.formatW3cDtf(item.dueDate, (item.due_time != 0)) +"</dlf:dateavailable>");
      }
      out.println("</dlf:simpleavailability>");
  }
  
}
