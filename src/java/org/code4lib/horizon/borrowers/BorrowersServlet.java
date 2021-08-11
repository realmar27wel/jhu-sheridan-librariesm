package org.code4lib.horizon.borrowers;

import edu.uchicago.lib.SearchKey;
import edu.uchicago.lib.Util;

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


/**
 *
 * @author jrochkind
 */
public class BorrowersServlet extends HttpServlet {
    
    private static Context ctx;
    private static String prefix;
    private static String realPath;
    private static DataSource ds;
    private static Connection conn;
    
    //will be loaded with app properties, default to system properties too.
    public static Properties appProperties = new Properties(System.getProperties());
    
        
    public void init(ServletConfig config) throws ServletException {
        System.out.println("=====Loading borrowers servlet=====");
        //Load app-specific properties
        try {
          String realPath = config.getServletContext().getRealPath(".");
          FileInputStream fis = new FileInputStream(realPath + "/config/general.properties");
          appProperties.load( fis );
          fis.close();
        } catch (IOException e) {
          System.err.println("Unable to load holdings.properties from path " + realPath + "/properties/");
        }
      
        String dsn = appProperties.getProperty("horizon-extras.datasource.name", "horizon-extras");
      
        // Stupid check to see whether we're actually in HIP
        prefix = System.getProperty("dynix.context.ds.lookup.prefix");

        
        // System.err.println("PREFIX: " + prefix);
        if (prefix != null) {
            //Running in HIP and JBoss
            System.out.println("Looks like we are running in HIP.");
            try {
                ctx = getInitialContext();
                prefix = System.getProperty("dynix.context.ds.lookup.prefix", "java:/");
                ds = (DataSource)PortableRemoteObject.narrow(ctx.lookup(prefix + dsn), javax.sql.DataSource.class);                                
                realPath = config.getServletContext().getRealPath(".");
                if (ds == null) 
                  throw new Exception("Data source not found! : " + dsn);
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
                
                String name = "java:/comp/env/" + dsn;
                System.out.println("Looking up " + name);
                System.out.flush();
                // ds = (DataSource) ctx.lookup( "java:/comp/env/catalog" );
                ds = (DataSource) ctx.lookup( name );
                System.out.println("Looking up " + name);
                System.out.flush();
                
                if ( ds == null ) {
                  throw new Exception("Data source not found! : " + name);
                }
            } catch (Exception e ) {
                e.printStackTrace();
            }
        }
                
        System.out.println("=====DONE Loading borrowers servlet=====");
    }
    
    /** Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {

        // get params
        ArrayList queryConstraints = new ArrayList();

        // In path?
        String pathInfo = request.getPathInfo();
        if ( pathInfo != null ) {
          String[] pathComponents = pathInfo.split("/");
          //first component is null becuase path starts with /
          if (pathComponents.length >= 1) {
            String borrowerId = pathComponents[1];
            queryConstraints.add( new BorrowerSearchKey("id" , borrowerId));    
          }
        }
        
        // Query param
        Iterator paramKeys = BorrowerSearchKey.validBorrowerKeys().iterator();
        while ( paramKeys.hasNext() ) {
          String key = (String) paramKeys.next();
          String value = request.getParameter(key);
          if ( value != null ) {
            queryConstraints.add(new BorrowerSearchKey(key, value)); 
          }          
        }
        
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        out.print("<?xml version='1.0' encoding='UTF-8'?>");

        
        
        try {
          //validate query
          if (queryConstraints.size() == 0) {
            throw new RuntimeException("No query parameters supplied: " + BorrowerSearchKey.validBorrowerKeys());
          }
          if (queryConstraints.size() == 1 && ("pin".equals( ((SearchKey)queryConstraints.get(0)).field()))) {
            throw new RuntimeException("Must supply an ID query parameter along with pin. " + BorrowerSearchKey.validBorrowerKeys()); 
          }
          
            conn = ds.getConnection();
              
            
            List borrowers= lookupBorrower(conn, queryConstraints);
            Iterator iterator = borrowers.iterator();

            out.print("<borrowers>\n");
            while ( iterator.hasNext() ) {
               Map b = (Map) iterator.next();
               out.print("  <borrower id=\""+  b.get("borrower#")  +"\">\n");
               Util.writeElt(out, "pin", (String) b.get("pin"));
               Util.writeElt(out, "name", (String) b.get("name_reconstructed"));
               Util.writeElt(out, "second_id", (String) b.get("second_id"));
               Util.writeElt(out, "btype", (String) b.get("btype"));
               Util.writeElt(out, "location", (String) b.get("location"));
               Util.writeElt(out, "last_cko_date",
                 toW3cDate(b.get("last_cko_date")));
               Util.writeElt(out, "registration_date",
                 toW3cDate(b.get("registration_date")));
               Util.writeElt(out, "expiration_date",
                 toW3cDate(b.get("expiration_date")));
               Util.writeElt(out, "creation_date",
                 toW3cDate(b.get("creation_date")));
               Util.writeElt(out, "last_update_date",
                 toW3cDate(b.get("last_update_date")));
               
               //"other_id"s from borrower_note
               List notes = fetchBorrowerNotes(conn, (Integer) b.get("borrower#"));
               Iterator notesIterator = notes.iterator();
               out.println("  <other_ids>\n");
               while ( notesIterator.hasNext()) {
                  Map note = (Map) notesIterator.next();
                  if (note.get("other_id") != null ) {
                    out.println("     <other_id location=\"" + Util.escapeXml((String) note.get("location")) + "\">" + 
                    Util.escapeXml((String)note.get("other_id")) + "</other_id>\n");
                  }
               }
               out.println("  </other_ids>\n");
               
               //barcodes
               List barcodes = fetchBorrowerBarcodes(conn, (Integer) b.get("borrower#"));
               Iterator listIterator = barcodes.iterator();
               out.println("   <barcodes>\n");
               while ( listIterator.hasNext()) {
                 Map barcode = (Map) listIterator.next();                
                 
                 out.print("       <barcode");
                 if ( barcode.get("proxy_borrower#") != null) {
                    out.print(" proxied_to=\"" + barcode.get("proxy_borrower#") + 
                      "\"");
                 }
                 
                 out.print(">" + Util.escapeXml((String)barcode.get("bbarcode")) + "</barcode>\n");
               }
               out.println("   </barcodes>\n");
               out.println("  </borrower>");
            }
            out.println("</borrowers>");
            
            
            

        } catch( Exception e ) {
            System.err.println( e.getClass().getName() );
            out.print("<error class=\"" + e.getClass().getName() + "\">");
            out.print("  <message>" + e.getMessage() + "</message>");
            out.print("</error>");
            e.printStackTrace();
        } finally {          
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

    
    //Lookup user/pin combo
    // Returns List of Map objects representing users. empty list means
    // none found. Normally there will not be more than one found,
    // unless there's weird data in Horizon, which is possible. 
    private List lookupBorrower(Connection conn, List queryConstraints) throws SQLException  {
        String lookupStmt = "SELECT DISTINCT dbo.borrower.* FROM dbo.borrower ";
        
        //Add joins from search keys
        for(int i=0; i < queryConstraints.size(); i++) {
           SearchKey key = (SearchKey) queryConstraints.get(i);
           if (key.join() != null) {
              lookupStmt += " " + key.join() + " "; 
           }
        }
        
        lookupStmt += " WHERE 1=1 ";
        
        for(int i = 0; i < queryConstraints.size(); i++) {
          SearchKey key = (SearchKey) queryConstraints.get(i);
          lookupStmt += " and " +  key.column() + " = ? ";
        }
        
        
        //System.out.println(" BorrowersServlet lookup stmt: " + lookupStmt);
        
        PreparedStatement pstmt = conn.prepareStatement(lookupStmt);
        for(int i = 0; i < queryConstraints.size(); i++) {
          SearchKey key = (SearchKey) queryConstraints.get(i);
          if (key.colType() == SearchKey.INT) {
            pstmt.setInt(i+1, key.val());
          } else { 
            pstmt.setString(i+1, key.value());
          }
        }
        return execStatementToList(conn, pstmt);          
          
         
    }
    

    
    //Returns List of Map objects representing borrower_notes objects
    //corresponding to borrower_id
    public List fetchBorrowerNotes(Connection conn, Integer borrower_id) throws java.sql.SQLException {
      String lookupStmt = "Select * from dbo.borrower_note WHERE borrower# = ?";
      
      PreparedStatement pstmt = conn.prepareStatement(lookupStmt);
      pstmt.setInt(1, borrower_id.intValue());
        
      return execStatementToList(conn, pstmt);
    }
    
    public List fetchBorrowerBarcodes(Connection conn, Integer borrower_id) throws java.sql.SQLException {
      //We don't want any lost barcodes, we consider them gone.
      String lookupStmt = "SELECT * from dbo.borrower_barcode WHERE borrower# = ? AND lost_date is null";
      PreparedStatement pstmt = null;
      
        pstmt = conn.prepareStatement(lookupStmt);
        pstmt.setInt(1, borrower_id.intValue());
        
        return execStatementToList(conn, pstmt);
    }

   // begin misc. utilities
    public static Context getInitialContext()
    throws NamingException{
        Properties p = new Properties();
        p.put( Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory" );
        p.put( Context.PROVIDER_URL, "localhost:1099" ); //TODO: make not hardcoded
        return new InitialContext( p );
    }
    
    public static String toW3cDate(Object horizonDate) {
      if ( horizonDate == null ) {
        return "";
      }
      Integer i = (Integer) horizonDate;
      return Util.formatW3cDtf(Util.parseHorizonDate(i.intValue(), 0), false);
    }
    
    //Executes a prepared statement, turns it's results into a List of Map
    //objects, closes the PreparedStatement. 
    public static List execStatementToList(Connection conn, PreparedStatement pstmt) throws java.sql.SQLException {
      try {
        
        ResultSet rs = pstmt.executeQuery();
                    
        ArrayList list = new ArrayList();
        
        while (rs.next() ) { 
          HashMap map = new HashMap();
          ResultSetMetaData resultSetDesc = rs.getMetaData();
          for( int i = 1; i <= resultSetDesc.getColumnCount(); i++) {
            map.put( resultSetDesc.getColumnLabel(i), rs.getObject(i)  ); 
          }
          list.add(map);
        }
        return list;
      }
      finally {
        pstmt.close();
      }         
    }
   
    
}

