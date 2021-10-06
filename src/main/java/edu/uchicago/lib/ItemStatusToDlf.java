package edu.uchicago.lib;

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


public class ItemStatusToDlf extends Object {
  private java.util.Properties properties;
  private Hashtable itemStatusToDlf;
  
  ItemStatusToDlf(DataSource ds, Properties prop) {
    properties = prop;
    
    loadDlfStatusMap(ds);
    
  }

    // Take a horizon item status code, and map it to one of the
    // four DLF availabilitystatusType values. 
    // http://www.diglib.org/architectures/ilsdi/schemas/1.1/dlfexpanded.xsd
    public String itemStatusToDlf(String istatus) {
      String dlf = (String) itemStatusToDlf.get(istatus);
      return ( dlf == null ? "unknown" : dlf);   
    }
  
  // Loads map of Horizon item status to DLF four enumerated values.
    // from property file and/or database, as configured. 
    private void loadDlfStatusMap(DataSource ds) {
      itemStatusToDlf = new Hashtable();
      
      //Some basic horizon defaults
      itemStatusToDlf.put("i", "available");
      itemStatusToDlf.put("o", "not available");
      
      //Get from database?
      String dlf_column = properties.getProperty("holdings.item_status_dlf_column");
      if ( dlf_column != null ) {
        try {
          Connection conn = ds.getConnection();
          PreparedStatement pstmt = conn.prepareStatement("SELECT item_status, " + dlf_column + " AS dlf_status FROM dbo.item_status");
          try {           
             ResultSet rs = pstmt.executeQuery();
             while ( rs.next()) {
                String internal_status = rs.getString("item_status");
                String dlf_status = rs.getString("dlf_status");
                if ( dlf_status != null ) {
                  itemStatusToDlf.put( internal_status, dlf_status ); 
                }
             }
          }
          finally {
            pstmt.close();
            conn.close();           
          }
        }
        catch (SQLException e) {
          System.err.println("Could not load dlf status from Horizon item_status column " + dlf_column + " : " + e.getMessage());
        }
      }
      
      //comma-seperated in properties file
      String propString = properties.getProperty("holdings.dlf_status_available");
      if ( propString != null ) {
        String[] a = propString.split(",");
        for( int i=0 ; i < a.length; i++ ) {
          itemStatusToDlf.put(a[i], "available");
        }
      }
      propString = properties.getProperty("holdings.dlf_status_not_available");

      if ( propString != null ) {
        String[] a = propString.split(",");
        for( int i=0 ; i < a.length; i++ ) {
          itemStatusToDlf.put(a[i], "not available");
        }
      }
      propString = properties.getProperty("holdings.dlf_status_possibly_available");
      if ( propString != null ) {
        String[] a = propString.split(",");
        for( int i=0 ; i < a.length; i++ ) {
          itemStatusToDlf.put(a[i], "possibly available");
        }
      }
    }
  
}
