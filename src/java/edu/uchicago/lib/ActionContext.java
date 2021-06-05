package edu.uchicago.lib;

import edu.uchicago.lib.*;

import edu.uchicago.lib.*;
import edu.uchicago.lib.outputformat.*;

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


public class ActionContext {
  protected Connection conn;
  protected HttpServletRequest request;
  protected Properties appProperties;
  protected HashMap registeredItemFormats;
  protected Collection itemFormats;
  protected HashMap registeredCopyFormats;
  protected Collection copyFormats;
  protected Boolean includeItems;
  protected URI hipUrl;
  
  public ActionContext(Connection aConn, HttpServletRequest aReq, Properties aProp) {
    super();
    conn = aConn;
    request = aReq;
    appProperties = aProp;
    
    buildFormatMap();
  }
  
  public Connection connection() {
    return conn;
  }
  
  public boolean includeItems() {
    if (includeItems == null) {      
      
      String str = request.getParameter("includeItems");
      if ( str != null && str.equals("true")) {
        includeItems = new Boolean(true);
      }
      else if (str != null && str.equals("false")) {
        includeItems = new Boolean(false);
      }
      //don't have it from query, use default
      if ( includeItems == null) {
          str = appProperties.getProperty("holdings.default_include_items");
          if ( str != null )
            includeItems = new Boolean(str);
          else
            includeItems = new Boolean(true);      
      }
    }
    return includeItems.booleanValue();
  }
  
  public URI hipUrl() {
    if (hipUrl == null) {
      try {
      String hipUrlStr = ItemsServlet.appProperties.getProperty("holdings.hip_url");
      if ( hipUrlStr != null ) {
        return new URI(hipUrlStr);
      } else {
        //guess it from the servlet url, assuming the servlet url is installed
        //in HIP Jboss
        URI servletUri = new URI(request.getRequestURL().toString());
        
          //remove last component from context path, and then add on standard
          //hip        
          String newPath =  Util.removeLastPathComponent( request.getContextPath()) + "/ipac20/ipac.jsp";
                        
         
          hipUrl = new URI(  servletUri.getScheme(),
                           servletUri.getAuthority(),
                           newPath, null, null);
                           
        }
      }
      catch ( URISyntaxException e) {
        e.printStackTrace(); 
      }
    }
    return hipUrl;
  }
  
  // Some metadata schemas require a URI to identify a bib. We don't
  // really have a URI for a Horizon bib. We can make one that will be
  // guaranteed unique, although it won't actually resolve in a
  // standard HIP setup. (it does at JHU). 
  public String uriForBib(int bibId) {
    //Use a property if we've got it. 
    String template = appProperties.getProperty("holdings.bib_uri_template");
    if ( template != null ) {
      return template.replaceAll("\\%b", Integer.toString(bibId));
    }
    else {
      //Default thingy
      return Util.removeLastPathComponent(Util.removeLastPathComponent( hipUrl().toString() )) + "/bib/" + Integer.toString(bibId);
    }
  }
  public String uriForItem(int itemId) {
        //Use a property if we've got it. 
    String template = appProperties.getProperty("holdings.item_uri_template");
    if ( template != null ) {
      return template.replaceAll("\\%i", Integer.toString(itemId));
    }
    else {
      //Default thingy
      return Util.removeLastPathComponent(Util.removeLastPathComponent( hipUrl().toString() )) + "/item/" + Integer.toString(itemId);
    }
  }
  public String uriForCopy(int copyId) {
        //Use a property if we've got it. 
    String template = appProperties.getProperty("holdings.copy_uri_template");
    if ( template != null ) {
      return template.replaceAll("\\%c", Integer.toString(copyId));
    }
    else {
      //Default thingy
      return Util.removeLastPathComponent(Util.removeLastPathComponent( hipUrl().toString() )) + "/item/" + Integer.toString(copyId);
    }
  }
  
  public String hipRequestUrl(int bibId, int itemId) {
    String template = appProperties.getProperty("holdings.request_uri_template"); 
    if ( template != null ) {
      return template.replaceAll("\\%i", Integer.toString(itemId)).replaceAll("\\%b", Integer.toString(bibId)); 
    }
    else {
      return hipUrl().toString() +       
        "?menu=request&aspect=none&bibkey=" + Integer.toString(bibId) + "&itemkey=" + Integer.toString(itemId);
    }
  }
  
  
  public Collection itemFormats() {
    if ( itemFormats == null ) {
      //specifed in url?
      String queryParam = request.getParameter("itemFormat");
      if ( queryParam != null ) {
        String[] formats = queryParam.split(",");
        itemFormats = new ArrayList();
        for ( int i = 0; i < formats.length; i++) {
          ItemFormat f = (ItemFormat) registeredItemFormats.get(formats[i]);          
          if ( f != null ) {
            itemFormats.add(f);
          }
        }
      }
      else {
        itemFormats = registeredItemFormats.values();
      }
    }  
    return itemFormats;
  }
  public Collection copyFormats() {
    if ( copyFormats == null ) {
      //specifed in url?
      String queryParam = request.getParameter("holdingFormat");
      if ( queryParam != null ) {
        String[] formats = queryParam.split(",");
        copyFormats = new ArrayList();
        for ( int i = 0; i < formats.length; i++) {
          CopyFormat f = (CopyFormat) registeredCopyFormats.get(formats[i]);          
          if ( f != null ) {
            copyFormats.add(f);
          }
        }
      }
      else {
        copyFormats = registeredCopyFormats.values();
      }
    }  
    return copyFormats;
  }
  
  protected void buildFormatMap() {
     registeredItemFormats = new HashMap();     
     registeredItemFormats.put("simpleAvailability", new ItemSimpleAvailability(this));
     registeredItemFormats.put("mfhd", new ItemMfhd(this));
     registeredItemFormats.put("ilsDetails", new ItemIlsDetails(this));
     registeredItemFormats.put("daia", new ItemDaia(this));
     
     registeredCopyFormats = new HashMap();
     registeredCopyFormats.put("mfhd", new CopyMfhd(this));
     registeredCopyFormats.put("ilsDetails", new CopyIlsDetails(this));     
  }

}
