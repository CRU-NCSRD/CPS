/*
 * Copyright (C) 2015 Computational Systems & Human Mind Research Unit
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package gr.iit.demokritos.cps.ai;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 *
 * @author Pythagoras Karampiperis
 */
public class HTMLUtilities {
    
    public static ArrayList linkExtractor(String location,String encoding,int engine){
       
        NodeClassFilter filter = new NodeClassFilter (LinkTag.class);
        Parser parser;
        NodeList list=null;
        
        try {
            parser = new Parser(location);
            parser.setEncoding(encoding);
            list = parser.extractAllNodesThatMatch (filter);
        } catch (ParserException ex) {
            Logger.getLogger(HTMLUtilities.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ArrayList linklist = new ArrayList();
        if(engine==0){
            for (int i = 0; i < list.size (); i++){               
                String link=list.elementAt(i).toPlainTextString();
                if(!link.startsWith("http://")) continue;
                linklist.add(link);                
            }
            
        }else if(engine==1){
            for (int i = 0; i < list.size (); i++){
                LinkTag extracted = (LinkTag)list.elementAt(i);
                if(!extracted.isHTTPLikeLink()) continue;
                String extractedLink = extracted.extractLink().replaceAll("&", "&");
                extractedLink = extractedLink.trim();
                if(extractedLink.length() == 0) continue; 
                if(extractedLink.startsWith("#")) continue;
                if(extractedLink.matches("(?i)^javascript:.*"))continue;
                if(!extractedLink.startsWith("http://")) continue;
                linklist.add(extractedLink);                
            } 
            
        }
               
        return linklist;     
    }
    
    
    
}
