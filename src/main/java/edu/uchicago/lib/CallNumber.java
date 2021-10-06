  package edu.uchicago.lib;
  
  import edu.uchicago.lib.ItemsServlet;

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
  
  public class CallNumber {
        String prefix = null;
        String callNumber = null;
        String callType = null;
        String callTypeHint = null;
        String copyNumber = null;
        
        static char NSB = '\u0018';
        static char NSE = '\u0019';
        
        public CallNumber (String call, String type, String copy, String type_hint) {
            if (call == null) {
                return;
            } else {
                int p1 = call.indexOf(NSB);
                if (p1 >= 0) {
                    int p2 = call.indexOf(NSE);
                    prefix = call.substring(p1+1,p2);
                    // TODO: trim by hand to cut down garbage collection
                    callNumber = call.substring(p2+1).trim();
                } else {
                    callNumber = call;
                }
            }
            callType = type;
            copyNumber = copy;
            callTypeHint = type_hint;
        }
        
        public void write(PrintWriter out) {
            if (callNumber == null && copyNumber == null) {
                return;
            }
            out.print("<call>");
            try {
                Util.writeElt(out, "prefix", prefix);
                Util.writeElt(out, "callNumber", callNumber, "type", callType);
                if (copyNumber != null) {
                    Util.writeElt(out, "copyNumber", copyNumber);
                }
            } finally {
                out.print("</call>");
            }            
        }
        
        // Bare human-displayable call number
        public String simpleCallLabel() {
          String returnVal = "";
          if ( prefix != null ) {
            returnVal += prefix + " ";
          }
          if ( callNumber != null ) {
            returnVal += callNumber + " ";
          }
          if ( copyNumber != null) {
            returnVal += copyNumber;
          }
          
          //return null, not empty string. 
          if ( returnVal.equals("")) {
            returnVal = null;
          }
          
          return returnVal;
        }
        
        // Turn the 'hint' from the Horizon call 'processor' into
        // a 1st indicator code for MFHD 852
        public String mfhd852TypeIndicator() {
          if ( callTypeHint == null ) {
            return " "; //blank == unknown 
          }
          else if ( callTypeHint.equals("lc") ) {
            return "0"; 
          }
          else if ( callTypeHint.equals("dewey") ) {
            return "1";
          }
          else if ( callTypeHint.equals("nlm")) {
            return "2";
          }
          else if ( callTypeHint.equals("sudoc")) {
            return "3";
          }
          else {
            return " "; // blank is 'unknown'. 
          }
        }
    }

