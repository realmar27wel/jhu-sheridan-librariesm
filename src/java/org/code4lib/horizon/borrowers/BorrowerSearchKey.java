package org.code4lib.horizon.borrowers;

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

public class BorrowerSearchKey extends edu.uchicago.lib.SearchKey {
    private static Hashtable keyHash = new Hashtable();
    static {                    
      keyHash.put("id", new KeyInfo("id","dbo.borrower.borrower#",INT));
      keyHash.put("second_id", new KeyInfo("second_id","dbo.borrower.second_id",STRING));
      keyHash.put("barcode", new KeyInfo("barcode","dbo.borrower_barcode.bbarcode",STRING, " INNER JOIN dbo.borrower_barcode ON dbo.borrower.borrower# = dbo.borrower_barcode.borrower# " ));  
      keyHash.put("pin", new KeyInfo("pin", "dbo.borrower.pin#", STRING));
      keyHash.put("other_id", new KeyInfo("other_id", "dbo.borrower_note.other_id", STRING, " INNER JOIN dbo.borrower_note ON dbo.borrower.borrower# = dbo.borrower_note.borrower# "));
    }
  
    public BorrowerSearchKey(String field, String value) {
      super(field, value, keyHash);
    }
        
    public static Collection validBorrowerKeys() {
      return keyHash.keySet();
    }
    
         
        
}


