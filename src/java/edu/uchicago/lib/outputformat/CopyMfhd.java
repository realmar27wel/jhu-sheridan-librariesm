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

public class CopyMfhd extends CopyFormat {
  public CopyMfhd(ActionContext context) {
    super(context);
  }
  
  public void format(Copy copy, PrintWriter out) {
    Connection connection = connection();
    
    //Need to fetch subsidiary holdings statements
    String holdingStmt = "select copy#, hs.run_code, rc.run_type, rc.descr run_descr, rc.ord run_ord, hs.ord, "
      + "display_text_from, display_text_to, "
      + "enum_chron_text = hs.display_text_from + ' ' + hs.display_text_to, hs.note "
      + "from holding_summary hs, run_code rc "
      + "where hs.run_code = rc.run_code "
      + "and copy# = ? "
      + "order by rc.ord, hs.ord ";              
    PreparedStatement holdingPstmt = null;         
    try {
        holdingPstmt = connection.prepareStatement(holdingStmt);
        holdingPstmt.setInt(1, copy.copyId);
    
        ResultSet holdingsRs = holdingPstmt.executeQuery();
    
        //MFHD for run statements and notes as 'textual holdings'. 
        out.println("<marc:record xmlns:marc=\"http://www.loc.gov/MARC21/slim\" type=\"Holdings\" xsi:schemaLocation=\"http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd\">");
        //standard leader from gmcharlt, not sure what this means.
        out.println("<marc:leader>00000nu  a2200000un 4500</marc:leader>");
        
        //holdingID in 001, bibID in 004
        Util.writeElt(out, "marc:controlfield", new Integer(copy.copyId).toString(), "tag", "001");
        Util.writeElt(out, "marc:controlfield", new Integer(copy.bibId).toString(), "tag", "004");
        
        ItemsServlet.printMfhd852(out, copy.locationName, copy.collectionDescr, copy.callNumber, copy.note, null);
        
        
        while (holdingsRs.next()) {
          int run_type = holdingsRs.getInt("run_type");
          String displayTextFrom = holdingsRs.getString("display_text_from");
          String displayTextTo = holdingsRs.getString("display_text_to");
          String hNote = holdingsRs.getString("note");
           
           String datafield = null;
           if (run_type == 1) {
             datafield = "867"; //supplement
           }  
           else if (run_type == 2) {
             datafield = "868"; //indexes
           }
           else {
             datafield = "866"; //main run
           }
          
           out.println("<marc:datafield tag=\"" + datafield + "\" ind1=\" \" ind2=\" \">");
           
           out.println("  <marc:subfield code=\"a\">");
           if ( displayTextFrom != null ) {
             out.println(displayTextFrom);
           }
           if ( displayTextTo != null ) {
             out.println(" " + displayTextTo);
           }
           out.println("  </marc:subfield>");
           
           if ( hNote != null ) {
             out.println("  <marc:subfield code=\"z\">" + hNote + "</marc:subfield>");
           }
           
           out.println("</marc:datafield>");
           
  
        }
       out.println("</marc:record>");
    } catch(java.sql.SQLException e) {
      e.printStackTrace();
    } finally {   
      try {
        holdingPstmt.close();
      }
      catch( SQLException e) {
        e.printStackTrace(); 
      }
    }
  }
  
  
}
