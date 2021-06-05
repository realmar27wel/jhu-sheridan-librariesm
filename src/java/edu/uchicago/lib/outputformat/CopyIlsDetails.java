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

public class CopyIlsDetails extends CopyFormat {
  public CopyIlsDetails(ActionContext context) {
    super(context);
  }
  
  public void format(Copy copy, PrintWriter out) {
     out.println("<ilsitem:description xmlns:ilsitem=\"http://purl.org/NET/ils-holdings-schema/1\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"  xsi:schemaLocation=\"http://purl.org/dc/elements/1.1 http://dublincore.org/schemas/xmls/qdc/2003/04/02/dc.xsd http://purl.org/NET/ils-holdings-schema/1 http://purl.org/NET/ils-holdings-schema/1/schema\">");
     
     ItemIlsDetails.commonBlock(out, copy.location, copy.locationName, copy.collection, copy.collectionDescr, copy.callType, copy.callTypeName);
     
     out.println("</ilsitem:description>");

  }
  
  
}
