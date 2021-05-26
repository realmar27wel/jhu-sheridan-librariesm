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


public class SearchKey {
        // Parameter names
        protected static final String bibIdParmName =
          ItemsServlet.appProperties.getProperty("holdings.queryKey.bibId", "bib");
        protected static final String copyIdParmName =
          ItemsServlet.appProperties.getProperty("holdings.queryKey.copyId", "copy");
        protected static final String itemIdParmName = 
          ItemsServlet.appProperties.getProperty("holdings.queryKey.itemId", "item");
        protected static final String barcodeParmName = 
          ItemsServlet.appProperties.getProperty("holdings.queryKey.barcode", "barcode");
        // No build-in Type interface in java 1.4.2. *sigh*
        public static final int INT = 0, STRING = 1;
        public static final String[] validKey = {bibIdParmName, copyIdParmName, itemIdParmName, barcodeParmName};
        private static Hashtable keyHash = new Hashtable();
        static {
            System.out.println("jrochkind searchkey initial bibIdParmName is " + bibIdParmName);
          
          
            keyHash.put(bibIdParmName, new KeyInfo(bibIdParmName,"bib#",INT));
            keyHash.put(copyIdParmName, new KeyInfo(copyIdParmName,"copy#",INT));
            keyHash.put(itemIdParmName, new KeyInfo(itemIdParmName,"item#",INT));
            keyHash.put(barcodeParmName, new KeyInfo(barcodeParmName,"barcode",STRING));
        }

        private static class KeyInfo {
            String field;
            String column;
            int colType;
            
            public KeyInfo (String field, String column, int colType) {
                this.field = field;
                this.column = column;
                this.colType = colType;
            }
            public String toString() {
                StringBuffer buf = new StringBuffer();
                buf.append("KeyInfo: field=").append(this.field);
                buf.append(", column=").append(this.column);
                buf.append(", colType=").append(this.colType);
                return buf.toString();
            }
        }
        
        
        String field = null;
        String column = null;
        int colType = -1;
        String value = null;
        int val = -1;
        
        public SearchKey (String field, String value) {
            if (!keyHash.containsKey(field)) {
                throw new IllegalArgumentException("Unknown search key: " + field);
            }
            KeyInfo info = (KeyInfo) keyHash.get(field);
            this.field = info.field;
            this.column = info.column;
            this.colType = info.colType;
            if (this.colType == INT) {
                this.value = value;
                try {
                    this.val = Integer.parseInt(this.value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(field + " requires an integer value");
                }
            } else if (this.colType == STRING) {
                // sanitize value string for use in SQL, allow for some variations in dummy barcodes
                // Any better ideas?
                // IDEA: move sanitizing up a level, to the calling environment.
                String saneChars = ".abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
                this.value = Util.cleanUp(value,saneChars, true);
            } else {
                throw new RuntimeException("Impossible colType: " + this.colType);
            }
            System.out.println("SearchKey = " + this);
        }
        
        public static final List validKeys() {
            List list = new ArrayList();
            for (int i=0; i < validKey.length; i++) {
                list.add(validKey[i]);
            }
            return list;
        }
        public static final String validKeysStr() {
            StringBuffer sb = new StringBuffer();
            for (int i=0; i < validKey.length; i++) {
                sb.append(validKey[i]);
                if (i < validKey.length -1)
                    sb.append(", ");
            }
            return sb.toString();
        }
                
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("SearchKey: field=").append(this.field);
            buf.append(", column=").append(this.column);
            buf.append(", colType=").append(this.colType);
            buf.append(", value=").append(this.value);
            buf.append(", int val=").append(this.val);
            return buf.toString();
        }
    }

