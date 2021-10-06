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

public class ItemSearchKey extends SearchKey {
    // Parameter names
    protected static final String bibIdParmName =
      ItemsServlet.appProperties.getProperty("holdings.queryKey.bibId", "bib");
    protected static final String copyIdParmName =
      ItemsServlet.appProperties.getProperty("holdings.queryKey.copyId", "copy");
    protected static final String itemIdParmName = 
      ItemsServlet.appProperties.getProperty("holdings.queryKey.itemId", "item");
    protected static final String barcodeParmName = 
      ItemsServlet.appProperties.getProperty("holdings.queryKey.barcode", "barcode");

    private static Hashtable keyHash = new Hashtable();
    static {
                
        keyHash.put(bibIdParmName, new KeyInfo(bibIdParmName,"bib#",INT));
        keyHash.put(copyIdParmName, new KeyInfo(copyIdParmName,"copy#",INT));
        keyHash.put(itemIdParmName, new KeyInfo(itemIdParmName,"item#",INT));
        keyHash.put(barcodeParmName, new KeyInfo(barcodeParmName,"ibarcode",STRING));
    }

    
    
    public ItemSearchKey(String field, String value) {
      super(field, value, keyHash);
    }
      
    public static Collection validItemKeys() {                        
      return keyHash.keySet();                        
    }
       
        
}


