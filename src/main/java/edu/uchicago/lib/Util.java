package edu.uchicago.lib;

import java.io.*;
import java.net.*;

import java.text.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;


// Just some static util functions
public class Util {
   /**
   * @param s   source string
   * @param toMatch  target character(s)  
   * @param boolean  true=keep  false=strip
   **/
    // from http://www.rgagnon.com/javadetails/java-0018.html
   public static String cleanUp
        ( String s, String sToMatch, boolean isToKeep ) {
     final int size = s.length();
     StringBuffer buf = new StringBuffer( size );
     if ( ! isToKeep ) {
       for ( int i = 0; i < size; i++ ){
         if ( sToMatch.indexOf(s.charAt(i) ) == -1 ){
           buf.append( s.charAt(i) );
         }
       }
     }
     else {
       for ( int i = 0; i < size; i++ ){
         if ( sToMatch.indexOf(s.charAt(i) ) != -1 ){
           buf.append( s.charAt(i) );
         }
       }
     }
     return buf.toString();
   } 
   
    // Take Horizon's "days since 1/1/1970" and "minutes since midnight" and
    // combine into an ordinary java.util.Date object. 
    public static java.util.Date parseHorizonDate(int due_date, int due_time) {
      GregorianCalendar calendar = new GregorianCalendar(1970, Calendar.JANUARY, 1);
      calendar.add(Calendar.DAY_OF_MONTH , due_date);
      calendar.add(Calendar.MINUTE, due_time);
      
      return calendar.getTime();
    }
    
    // outputs a java.util.Date as a w3cdtf, with or without time included.
    // Should do this as a DateFormat sub-class, but we're lazy and ignorant.
    public static String formatW3cDtf(java.util.Date date, boolean shouldOutputTime) {
      if (shouldOutputTime) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String formatted = df.format(date);
        //but need to add in a colon that w3cdtf requires but java don't, argh. 
        return formatted.substring(0, formatted.length()-2) + ":" + formatted.substring(formatted.length()-2);
      }
      else {
         DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
         return df.format(date);
      }
    }
    
  public static void writeElt(PrintWriter out, String name, String str) {
        if (str != null) {
            str = escapeXml(str);
            out.print('<');
            out.print(name);
            out.print('>');
            out.print(str);
            out.print('<');
            out.print('/');
            out.print(name);
            out.print('>');
        }
    }
        
    public static void writeElt(PrintWriter out, String name, int i) {
        out.print('<');
        out.print(name);
        out.print('>');
        out.print(i);
        out.print("</");
        out.print(name);
        out.print('>');
    }
    
    public static void writeElt(PrintWriter out, String name, String str, String attrName, String attrVal) {
        if (str == null) {
            str = "";
        } else {
            str = escapeXml(str);
        }
        if (attrVal == null) {
            attrVal = "";
        }
        out.print('<');
        out.print(name);
        out.print(' ');
        out.print(attrName);
        out.print("='");
        out.print(attrVal);
        out.print("'>");
        out.print(str);
        out.print("</");
        out.print(name);
        out.print('>');
    }
    
   
    
    public static String removeLastPathComponent(String input) {
      return input.substring(0, input.lastIndexOf('/'));
    }
    
    public static String escapeXml(String str) {
      if (str == null) {
        return "";
      }
      str = str.replaceAll("&","&amp;");
      str = str.replaceAll("<","&lt;");
      str = str.replaceAll(">","&gt;");
      str = str.replaceAll("\"","&quot;");
      str = str.replaceAll("'","&apos;");
      return str;
    }

}
