/*
 * items.java
 *
 * Created on July 9, 2007, 1:16 PM
 */

package edu.uchicago.lib;

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

import com.dynix.util.HtmlEncoder;

/**
 *
 * @author tod
 * @version
 */
public class ItemsServlet extends HttpServlet {
    
    private static Context ctx;
    private static String prefix;
    private static String realPath;
    private static DataSource ds;
    private static Connection conn;
    
    // Parameter names
    private static String formatParmName = "format";
    
    //will be loaded with app properties, default to system properties too.
    public static Properties appProperties = new Properties(System.getProperties());
    
    //hash that maps from internal Horizon item status to DLF availability status code
    public static ItemStatusToDlf itemStatusToDlfTranslator = null;
    
    // Hash mapping horizon status codes map to dlf:availabilty types
    //private static java.util.HashMap istatusToDlf = null;
        
    public void init(ServletConfig config) throws ServletException {
        System.out.println("=====Loading items servlet=====");
        //Load app-specific properties
        try {
          String realPath = config.getServletContext().getRealPath(".");
          FileInputStream fis = new FileInputStream(realPath + "/config/general.properties");
          appProperties.load( fis );
          fis.close();
        } catch (IOException e) {
          System.err.println("Unable to load holdings.properties from path " + realPath + "/properties/");
        }
      
        String dsn = appProperties.getProperty("holdings.datasource.name", "horizon-opac");
      
        // Stupid check to see whether we're actually in HIP
        prefix = System.getProperty("dynix.context.ds.lookup.prefix");

        
        // System.err.println("PREFIX: " + prefix);
        if (prefix != null) {
            //Running in HIP and JBoss
            // System.out.println("Looks like we are running in HIP.");
            try {
                ctx = getInitialContext();
                prefix = System.getProperty("dynix.context.ds.lookup.prefix", "java:/");
                ds = (DataSource)PortableRemoteObject.narrow(ctx.lookup(prefix + dsn), javax.sql.DataSource.class);
                realPath = config.getServletContext().getRealPath(".");
            } catch (Exception e ) {
                e.printStackTrace();
            }
        } else {
            //Maybe running in Tomcat and NetBeans?
            System.out.println("Looks like we are NOT running in HIP.");
            try {
                ctx = new InitialContext();
                if ( ctx == null ) {
                    throw new Exception("Uh oh -- no context!");
                }
                
                String name = "java:/comp/env/horizon-opac";
                System.out.println("Looking up " + name);
                System.out.flush();
                // ds = (DataSource) ctx.lookup( "java:/comp/env/catalog" );
                ds = (DataSource) ctx.lookup( name );
                System.out.println("Looking up " + name);
                System.out.flush();
                
                if ( ds == null ) {
                    throw new Exception("Data source not found!");
                }
            } catch (Exception e ) {
                e.printStackTrace();
            }
        }
        
        itemStatusToDlfTranslator = new ItemStatusToDlf(ds, appProperties);
        
        System.out.println("=====DONE Loading items servlet=====");
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        String ret;
        boolean getDefault = true;
        boolean getCopy = false;
        boolean getItem = false;
        boolean debug = false;

        
        

        // get params
        String keyName = null;
        String format = appProperties.getProperty("holdings.default_format", "uchicago");; // uchicago or dlf_di

        String debugStr = request.getParameter("debug");
        if (debugStr != null) {
            debug = Boolean.valueOf(debugStr).booleanValue();
        }
        //do we have item-mode or copy-mode specified explicitly?
        //usually not useful to do. 
        String getItemStr = request.getParameter("getItem");
        if (getItemStr != null) {
            getItem = Boolean.valueOf(getItemStr).booleanValue();
        }
        String getCopyStr = request.getParameter("getCopy");
        if (getCopyStr != null) {
            getCopy = Boolean.valueOf(getCopyStr).booleanValue();
        }
        
        SearchKey key = null;
        
        //Get paramters from path-style instead of query-param-style
        String pathInfo = request.getPathInfo();  
        if ( pathInfo != null ) {
          String[] pathComponents = pathInfo.split("/");
          //first component is null because the path starts with /
          if (pathComponents.length >= 2) {
            keyName = pathComponents[1];            
            String[] leafParts = pathComponents[2].split("\\.");
            try {
              key = new SearchKey(keyName, leafParts[0]);
            } catch (IllegalArgumentException e) {
              response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
              return;
            } 
            if ( leafParts.length > 1) {
              format = leafParts[1]; 
            }
          }
        }
        
        // Get parameters from query-param style only if we don't already
        // have them from path.         
        if ( key == null ) {
          if ( request.getParameter(formatParmName) != null && ! request.getParameter(formatParmName).equals("")) {
            format = request.getParameter(formatParmName);
          }
          
          Map parms = request.getParameterMap();
          for (Iterator i = SearchKey.validKeys().iterator(); i.hasNext(); ){
            String keyCandidate = (String) i.next();
            if (parms.containsKey(keyCandidate)) {
              keyName = keyCandidate;
              break;
            }
          }
          if (keyName==null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
              "Missing required parameter, must contain one of: " + SearchKey.validKeys());
            return;
          }
          try {
            key = new SearchKey(keyName, request.getParameter(keyName));
          } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return;
          }    
        }
        
        //System.out.println("SearchKey = " + keyName);
            
        
        
        // Decide what to fetch from Horizon
        if (key.field == SearchKey.itemIdParmName || key.field == SearchKey.barcodeParmName) {
          getDefault = false;
          getCopy = false;
          getItem = true;
        } else {
          if (getCopy == true) {
            getDefault = false;
            getCopy = true;
          }
          if (getItem) {
            getDefault = false;
            getItem = true;
          }
        }

        
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print("<?xml version='1.0' encoding='UTF-8'?>");
        
        // java.lang.System.getProperties().list(System.err);
        
        try {
            conn = ds.getConnection();
            
            ActionContext context = new ActionContext(conn, request, appProperties);
            
            
            if ( format.equals("uchicago")) {
              out.print("<results>");
            }
            else {
              out.print("<dlf:record xmlns:dlf=\"http://diglib.org/ilsdi/1.1\"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://diglib.org/ilsdi/1.1 http://www.diglib.org/architectures/ilsdi/schemas/1.1/dlfexpanded.xsd\">");
              // This is really only a valid DLF document if we have a bibID,
              // but its otherwise too hard to print out the 'bibliographic'
              // element.
              if (key.field.equals(SearchKey.bibIdParmName)) {
                out.print("  <dlf:bibliographic id=\"" + key.value + "\" />"); 
              }
            }
            
            int fetchedCount = 0;
            if (getDefault) {
                fetchedCount = getCopies(context, out, key, format);
                if (fetchedCount == 0) {
                    fetchedCount = getItems(context, out, key, format);
                }
            } else {
                if (getCopy) {
                    fetchedCount = getCopies(context, out, key, format);
                }
                if (getItem) {
                    fetchedCount = getItems(context, out, key, format);
                }
            }        
            if (fetchedCount == 0) {
               response.sendError(HttpServletResponse.SC_NOT_FOUND,
                 "Object not found: " + key.field + "=" + key.value);            
            }
        } catch( Exception e ) {
            System.err.println( e.getClass().getName() );
            out.print("<error class=\"" + e.getClass().getName() + "\">");
            out.print("  <message>" + e.getMessage() + "</message>");
            out.print("</error>");
            e.printStackTrace();
        } finally {
            //close xml on finally?
            if ( format.equals("uchicago")) {
              out.print("</results>");
            }
            else {
              out.print("</dlf:record>");
            }

            try {
              conn.close();
            } catch(Exception e) {
              System.err.println("Problem closing HIP connection?");
              e.printStackTrace();
            }
        }
        

        out.close();
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }
    
    /** Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
    
    

    
    private int getCopies(ActionContext context, PrintWriter out, SearchKey key, String format) throws SQLException {

      int rows = 0;              
      List copies = fetchCopies(context.connection(), key);
      
      if (format.equals("uchicago")) {  
          rows = printCopiesUchicago(context, out, copies);
      }
      else {
        rows = printCopiesDlf(context, out, copies);
      }
        
      return rows;
    }
    
    private int printCopiesDlf(ActionContext context, PrintWriter out, List copies) throws SQLException {
      int rows = 0;
      
      Writer completeItemsResult = new StringWriter(4000);
      PrintWriter completeItemsWriter = new PrintWriter(completeItemsResult);
      
      Iterator copiesI = copies.iterator();
      while(copiesI.hasNext()) {
        if ( rows == 0) {
          out.println("<dlf:holdings>");
        }

        Copy copy = (Copy) copiesI.next();

        out.println("  <dlf:holdingset>");
          
        out.println("     <dlf:holdingsrec>");  
           
          Iterator i = context.copyFormats().iterator();;
          while ( i.hasNext()) {
            CopyFormat f = (CopyFormat) i.next();
            f.format(copy, out); 
          }
        
          /*
        //Mfhd
        new CopyMfhd(context).format(copy, out);
        
        //custom ilsitem stuff for additional info
        //printIlsDetails(out, rs, false);
        new CopyIlsDetails(context).format(copy, out);*/
        
        out.println("     </dlf:holdingsrec>");
          
        if ( context.includeItems() ) {
        
          out.println("     <dlf:items>");
          
          // Okay, we need to print out a little blank item element with an id
          // here, AND all the items at the end.
                  //We're going to need items
          List items = fetchItems(context.connection(), copy.itemsSearchKey());
          //List items = new ArrayList();
  
          Iterator itemsIterator = items.iterator();
          while( itemsIterator.hasNext()) {
            Item item = (Item) itemsIterator.next();
            
            //blank node with id, right here
            out.println("       <dlf:item id=\"" + item.itemId + "\" />");
            
            //complete item, print to string for outputting at end.          
            printItemDlf(context, completeItemsWriter, item);          
          }
          
          out.println("     </dlf:items>");
        }
          
        out.println("  </dlf:holdingset>");
        
        rows++;
      }
      if ( rows > 0 ) {
        out.println("</dlf:holdings>");
      }
      
      if ( rows > 0 && context.includeItems() ) {
        // add the complete form of all items
        out.println("<dlf:items>");
        out.println( completeItemsResult.toString() );
        out.println("</dlf:items>");
      }
      
      return rows;
    }
    
    
    public static void printMfhd852(PrintWriter out, String location, String collection, CallNumber call, String note, Integer itemNumber) {
      
      out.println("  <marc:datafield tag=\"852\" ind1=\"" +  
         (call != null ? call.mfhd852TypeIndicator() : " ") +
         "\" ind2=\" \">");
      if (location != null) {
        writeElt(out, "marc:subfield", location, "code", "a");        
      }
      if ( collection != null ) {
        writeElt(out, "marc:subfield", collection, "code", "b");        
      }
      if ( call != null && call.prefix != null ) {
        writeElt(out, "marc:subfield", call.prefix, "code", "k");        
      }
      //We're kinda cheating by putting this random call number in it's
      // entirety in h, but gmcharlt says it's standardly done. 
      if ( call != null && call.callNumber != null ) {
        writeElt(out, "marc:subfield", call.callNumber, "code", "h");        
      }
      //subfield i is actually for cutter number on end of call number, but
      //we don't have a cutter number seperatable. We DO have this 'copy'
      //string that needs to go somewhere, we're going to put it there. 
      if (call != null && call.copyNumber != null) {
        writeElt(out, "marc:subfield", call.copyNumber, "code", "i");          
      }
      
      //Item number in $p
      if ( itemNumber != null) {
        writeElt(out, "marc:subfield", itemNumber.toString(), "code", "p"); 
      }
     

      out.println("  </marc:datafield>");
    } 
    
      
    // Resultset passed in, _current_ row will be output
    // in dlf style to PrintWriter. ResultSet is not next()ed by this
    // method, you do that. 
     private void printItemDlf(ActionContext context, PrintWriter out, Item item) throws SQLException {
          
          out.println("<dlf:item id=\"" + item.itemId + "\">");
          
          Iterator i = context.itemFormats().iterator();;
          while ( i.hasNext()) {
            ItemFormat f = (ItemFormat) i.next();
            f.format(item, out); 
          }
          out.println("</dlf:item>");
    }     

   // Some metadata schemas require a URI to identify a bib. We don't
   // really have a URI for a Horizon bib. We can make one that will be
   // guaranteed unique, although it won't actually resolve in a
   // standard HIP setup. (it does at JHU). 
    private String uriForBib(int bibId) {
      return "http://catalog.library.jhu.edu/bib/" + Integer.toString(bibId);
    }
    
    private String hipRequestUrl(int bibId, int itemId) {
      return "http://catalog.library.jhu.edu/ipac20/ipac.jsp?menu=request&aspect=none&bibkey=" + Integer.toString(bibId) + "&itemkey=" + Integer.toString(itemId);
    }
    
    //pass in a result set of items from fetchItems (TBD)
    private int printItemsDlf(ActionContext context, PrintWriter out, List items) throws SQLException {
       int rows = 0;
        out.println("<dlf:items>");
        Iterator itemsI = items.iterator();
        while (itemsI.hasNext()) {
           Item item = (Item) itemsI.next();
           printItemDlf(context, out, item);
           rows++;
        }
        out.println("</dlf:items>");
        
        return rows;
    }     
    

    
    private int printCopiesUchicago(ActionContext context, PrintWriter out, List copies)
    throws SQLException {
      Connection conn = context.connection();
        
      String holdingStmt = "select copy#, hs.run_code, rc.descr run_descr, rc.ord run_ord, hs.ord, "
          + "display_text_from, display_text_to, "
          + "enum_chron_text = hs.display_text_from + ' ' + hs.display_text_to, hs.note "
          + "from holding_summary hs, run_code rc "
          + "where hs.run_code = rc.run_code "
          + "and copy# = ? "
          + "order by rc.ord, hs.ord ";
              
        PreparedStatement holdingPstmt = conn.prepareStatement(holdingStmt);
        try {
          int rows = 0;
          Iterator copiesI = copies.iterator();
          while (copiesI.hasNext()) {
            if (rows == 0) {
              out.println("<copies>");
            }
            
            Copy copy = (Copy) copiesI.next();
            
            out.print("<copy>");
            writeElt(out, "copyId", copy.copyId);
            writeElt(out, "bibId", copy.bibId);
            out.print("<mediaType>");
            writeElt(out, "code", copy.mediaType);
            if (copy.mediaTypeDescr != null) {
              writeElt(out, "descr", copy.mediaTypeDescr);
            }
            out.print("</mediaType>");
            out.print("<location>");
            writeElt(out, "code", copy.location);
            if (copy.locationName != null) {
              writeElt(out, "descr", copy.locationName);
            }
            out.print("</location>");
            out.print("<collection>");
            writeElt(out, "code", copy.collection);
            if (copy.collectionDescr != null) {
              writeElt(out, "descr", copy.collectionDescr);
            }
            out.print("</collection>");
            
            copy.callNumber.write(out);
            
            out.print("<type>");
            writeElt(out, "code", copy.itemType);
            writeElt(out, "descr", copy.itemTypeDescr);
            out.print("</type>");
            if (copy.note != null) {
              writeElt(out, "note", copy.note);
            }
            if (copy.summaryOfHoldings) {
              holdingPstmt.setInt(1, copy.copyId);
              ResultSet holdRs = holdingPstmt.executeQuery();
              
              out.print("<holdings>");
              
              String runCode = null;
              String prevRunCode = null;
              String runDescr = null;
              String displayText, displayTextFrom, displayTextTo, hNote;                
              boolean inRun = false;
              boolean newRun = true;
              
              while (holdRs.next()) {
                prevRunCode = runCode;
                runCode = holdRs.getString("run_code");
                runDescr = holdRs.getString("run_descr");
                displayTextFrom = holdRs.getString("display_text_from");
                displayTextTo = holdRs.getString("display_text_to");
                hNote = holdRs.getString("note");
                if (!runCode.equals(prevRunCode)) {
                  newRun = true;
                } else {
                  newRun = false;
                }
                if (newRun) {
                  if (inRun) {
                    out.print("</run>");
                  }
                  out.print("<run>");
                  writeElt(out, "code", runCode);
                  writeElt(out, "descr", runDescr);
                  inRun = true;
                }
                if (displayTextTo == null) {
                  writeElt(out, "enumAndCron", displayTextFrom);
                } else{
                  writeElt(out, "enumAndCron", displayTextFrom + " " + displayTextTo);
                }
                if (hNote != null) {
                  writeElt(out, "note", hNote);
                }
              }
              if (inRun) {
                out.print("</run>");
              }
              out.print("</holdings>");
            }
            
            // Include subsidiary items if so configured
            if ( context.includeItems() ) {
              List items = fetchItems(conn, new SearchKey(SearchKey.copyIdParmName, new Integer(copy.copyId).toString()));
              printItemsUchicago(context, out,  items);
            }
            
            out.print("</copy>");
            rows++;
          }
          if (rows > 0) {
            out.println("</copies>");
          }        
          return rows;
        } finally {
          holdingPstmt.close();
        }
    }
    

    
    // Close the statement when you're done with it by taking the ResultSet
    // return value rs and calling rs.getStatement().close();
    private List fetchCopies(Connection conn, SearchKey key) throws SQLException  {
        String copyStmt = "select c.copy#, c.bib#, c.media_type, mt.descr media_descr, c.summary_of_holdings, c.itype, it.descr idescr, "
                + "c.location, loc.name location_name, c.collection, col.pac_descr collection_descr, "
                + "c.call call_number, c.call_type, ct.processor as call_type_hint, ct.descr as call_type_name, c.copy_number, c.pac_note "
                + "from copy c, location loc, collection col, media_type mt, itype it, call_type ct "
                + "where c.location *= loc.location "
                + "and c.collection *= col.collection "
                + "and c.media_type *= mt.media_type "
                + "and c.itype *= it.itype "
                + "and c.call_type *= ct.call_type "
                + "and staff_only != 1 "
                + "and c." + key.column + " = ?";
        

        PreparedStatement pstmt = conn.prepareStatement(copyStmt);
        try {
          pstmt.setInt(1, key.val);
          ResultSet rs = pstmt.executeQuery();
                  
          //turn the ResultSet into a List of Copy objs
          ArrayList result = new ArrayList();
          while ( rs.next()) {
            result.add(new Copy(rs));
          }
          return result;
        } finally {
          pstmt.close();
        }
    }

    // Close the statement when you're done with it by taking the ResultSet
    // return value rs and calling rs.getStatement().close();
    private List fetchItems(Connection conn, SearchKey key) throws SQLException {
      PreparedStatement pstmt = conn.prepareStatement("SELECT " + 
          " item#, copy#, bib#, i.location, l.name as location_name," +
          "  i.collection, c.pac_descr as collection_descr, " +
          "  i.call_reconstructed, i.call_type, ct.processor as call_type_hint, ct.descr as call_type_name, i.copy_reconstructed, "+
          "i.item_status, ist.descr as item_status_descr, i.due_date, " +
          "i.due_time, staff_only, delete_flag, i.itype," +
          "  it.descr AS item_type_descr, last_status_update_date, last_update_date, " +          
          " due_date, due_time " +
        "FROM dbo.item i, dbo.location l, dbo.collection c, dbo.item_status ist, dbo.itype it, dbo.call_type ct " +
        "WHERE " +
        "      i.location *= l.location " +
        "  AND i.collection *= c.collection " +
        "  AND i.item_status *= ist.item_status " +
        "  AND i.itype *= it.itype " +
        "  AND i.call_type *= ct.call_type " +
        "  AND i." + key.column + " = ?" + " " +
        "  AND i.staff_only=0");
      ArrayList result = new ArrayList();
      try {
        if ( key.colType == SearchKey.INT) {
          pstmt.setInt(1, key.val);
        } else if (key.colType == SearchKey.STRING) {
          pstmt.setString(1, key.value);
        }
        else {
          throw new RuntimeException("Unrecognized colType: " + key.colType);
        }
                  
        ResultSet rs = pstmt.executeQuery();
        
        // Go through them all and convert them to Item objects. 
        while ( rs.next() ) {
          result.add(  new Item(rs) ); 
        }
      } finally {
        pstmt.close();
      }
      return result;
    }

    private int getItems(ActionContext context, PrintWriter out, SearchKey key, String format)
    throws SQLException {
      List items = fetchItems(context.connection(), key);
      int rows;
      
      if ( format.equals("uchicago") ) {                
        rows = printItemsUchicago(context, out, items);                
      }
      else {
        rows = printItemsDlf(context, out, items);
      }
            
      return rows;
    }
    
    private int printItemsUchicago(ActionContext context, PrintWriter out, List items)
    throws SQLException {
                      
        out.println("<items>");
        int rows = 0;

        Iterator itemsI = items.iterator();
        while (itemsI.hasNext()) {
            Item item = (Item) itemsI.next();
          
            out.print("<item>");
            writeElt(out, "itemId", item.itemId);
            if (item.copyId != 0) {
                writeElt(out, "copyId", item.copyId);
            }
            writeElt(out, "bibId", item.bibId);
            if (item.locationCode != null || item.locationName != null) {
                out.print("<location>");
                writeElt(out, "code", item.locationCode);
                writeElt(out, "descr", item.locationName);
                out.print("</location>");
            }
            out.print("<collection>");
            writeElt(out, "code", item.collectionCode);
            writeElt(out, "descr", item.collectionDescr);
            out.print("</collection>");
            
            item.callNumber.write(out);
            
            out.print("<status>");
            writeElt(out, "code", item.itemStatusCode);
            writeElt(out, "descr", item.itemStatusDescr);
            out.print("</status>");

            if (item.dueDate != null) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                DateFormat tf = new SimpleDateFormat("HH:mm:ss");
              
                out.print("<dueDate>");
               // w3cdtf format, with or without time specified.
                writeElt(out, "dueDate", Util.formatW3cDtf(item.dueDate, (item.due_time != 0)));
                writeElt(out, "dateOnly", df.format(item.dueDate));
                if ( item.due_time != 0 ) {
                  writeElt(out, "timeOnly", tf.format(item.dueDate));
                }
                out.print("</dueDate>");
            }
           
            out.print("<type>");
            writeElt(out, "code", item.itemTypeCode);
            writeElt(out, "descr", item.itemTypeName);
            out.print("</type>");
            out.print("</item>");
            
            rows++;
        }
        out.println("</items>");
        // out.println("<debug>Exiting getItems</debug>");
        return rows;
    }
    
    // begin misc. utilities
    public static Context getInitialContext()
    throws NamingException{
        Properties p = new Properties();
        p.put( Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory" );
        p.put( Context.PROVIDER_URL, "localhost:1099" ); //TODO: make not hardcoded
        return new InitialContext( p );
    }
    
    private static void writeElt(PrintWriter out, String name, String str) {
      Util.writeElt(out, name, str); 
    }
    private static void writeElt(PrintWriter out, String name, int i) {
      Util.writeElt(out, name, i);
    }
    private static void writeElt(PrintWriter out, String name, String str, String attrName, String attrVal) {
      Util.writeElt(out, name, str, attrName, attrVal);
    }

    
    // Take a horizon item status code, and map it to one of the
    // four DLF availabilitystatusType values. 
    // http://www.diglib.org/architectures/ilsdi/schemas/1.1/dlfexpanded.xsd
    public String itemStatusToDlf(String istatus) {
      return itemStatusToDlfTranslator.itemStatusToDlf(istatus);         
    }
    
}

