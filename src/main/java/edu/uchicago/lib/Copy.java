// Simple data class holding Copy row from Horizon db, doesn't do much. 

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

public class Copy {
  public int copyId;
  public int bibId;
  public String note;

  public String location;
  public String locationName;
  
  public String collection;
  public String collectionDescr;
  
  public CallNumber callNumber;
  public String callType;
  public String callTypeHint;
  public String callTypeName;

  public String mediaType;
  public String mediaTypeDescr;
  public boolean summaryOfHoldings;
  public String itemType;
  public String itemTypeDescr;

  
        
  // Needs a connection so it can fetch more stuff lazily
  Copy(ResultSet rs) throws SQLException {
   super(); 
   
   copyId = rs.getInt("copy#");
   bibId = rs.getInt("bib#");
   note = rs.getString("pac_note");

   location = rs.getString("location");
   locationName = rs.getString("location_name");
   collectionDescr = rs.getString("collection_descr");
   collection = rs.getString("collection");

   
   callNumber = new CallNumber(rs.getString("call_number"), rs.getString("call_type"), rs.getString("copy_number"), rs.getString("call_type_hint"));
   callType = rs.getString("call_type");
   callTypeHint = rs.getString("call_type_hint");
   callTypeName = rs.getString("call_type_name");

   
   mediaType = rs.getString("media_type");
   mediaTypeDescr = rs.getString("media_descr");
   summaryOfHoldings = rs.getBoolean("summary_of_holdings");
   itemType = rs.getString("itype");
   itemTypeDescr = rs.getString("idescr");
      
  }
  
  // Return a SearchKey suitable for fetching items belonging to this copy.
  public SearchKey itemsSearchKey() {
    return new ItemSearchKey(ItemSearchKey.copyIdParmName, new Integer(copyId).toString());
  }
  
}
