// very simple data object with values from a row from Item

package edu.uchicago.lib;
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
public class Item {
  public int itemId;
  public int copyId;
  public int bibId;
  public String locationCode;
  public String locationName;
  public String collectionCode;
  public String collectionDescr;
  public String itemStatus;
  public String itemStatusDescr;
  public int due_date;
  public int due_time;
  public java.util.Date dueDate = null;
  public String callNumberTypeCode;
  public String callNumberTypeName;
  public CallNumber callNumber = null;
  public String itemTypeCode;
  public String itemTypeName;
  public String itemStatusCode;
  public String itemStatusName;
  
  public Item(ResultSet rs) throws SQLException {
    
      itemId = rs.getInt("item#");
      copyId = rs.getInt("copy#");
      bibId = rs.getInt("bib#");
      locationCode = rs.getString("location");
      locationName = rs.getString("location_name");
      collectionCode = rs.getString("collection");
      collectionDescr = rs.getString("collection_descr");
      itemStatus = rs.getString("item_status");
      itemStatusDescr = rs.getString("item_status_descr");
      
      due_date = rs.getInt("due_date");
      due_time = rs.getInt("due_time");      
      if ( due_date != 0 ) {
          dueDate = Util.parseDueDate( rs.getInt("due_date"),rs.getInt("due_time")); 
      }
      
      callNumberTypeCode = rs.getString("call_type");
      callNumberTypeName = rs.getString("call_type_name");

      callNumber = new CallNumber(rs.getString("call_reconstructed"), callNumberTypeCode, rs.getString("copy_reconstructed"), rs.getString("call_type_hint"));
      
      itemTypeCode = rs.getString("itype");
      itemTypeName = rs.getString("item_type_descr");
      itemStatusCode = rs.getString("item_status");
      itemStatusName = rs.getString("item_status_descr");
  }
}
