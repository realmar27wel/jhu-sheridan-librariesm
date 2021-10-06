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

/*  An interface for classes that take some data from a Horizon query result
    set, and output some data string (usually XML fragment) to represent
    that. Kind of like a java Format, but doesn't parse/input, only outputs,
    and has some specialization for the way we wrote this stuff. 
    
    Needs to be initialized with a Connection for making further SQL querries
    if needed. */

public abstract class ItemFormat {
  protected ActionContext context;  
  
  public ItemFormat(ActionContext aContext) {
    context = aContext;
  }
  
  public Connection connection() {
    return context.connection();
  }
 
  public String format(Item item) {
     StringWriter sw = new StringWriter();
     PrintWriter pw = new PrintWriter(sw);
     format(item, pw);
     return sw.toString();
  }
  

  
  public abstract void format(Item item, PrintWriter out);
  
}
