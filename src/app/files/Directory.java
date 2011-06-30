 
package org.simpleframework.example;

import java.io.IOException;
import java.io.File;

class Directory {

   public byte[] getContents(File root, String target) throws Exception {
      String fixed = target;
      
      if(fixed.indexOf('?')>0){ /* remove query*/
         fixed = fixed.substring(0,
            fixed.indexOf('/')+1);
      }
      if(fixed.lastIndexOf(';')>0){ /* remove params*/
         fixed = fixed.substring(0,
            fixed.indexOf(';')+1);
      }
      return getContents(root, target, 
         !fixed.endsWith("/") && !fixed.endsWith("/."));
   }
   
   private byte[] getContents(File root, String target, boolean isRelative){
      File directory = new File(root, target.replace('/', File.separatorChar));
      String[] names = directory.list();
      
      String text = 
      "<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML 2.0//EN\">\n"+
      "<HTML><HEAD>"+
      "<TITLE>Index of "+target+"</TITLE>\n"+
      "</HEAD><BODY>" +
      "<H1>Index of "+target+"</H1>\n" +
      "<HR><TABLE>" +
      "<TR><TD><B>Name</B></TD>"+
      "<TD><B>Size</B></TD>";

      for(int i = 0; i < names.length; i++) {
         File file = new File(directory,names[i]);
         boolean isDirectory = file.isDirectory();
         String name = names[i] + (isDirectory ?"/":"");
         String size = isDirectory ? "-" : ""+file.length();

         text += "<TR><TD><TT><A HREF=\""+target+name+"\">"+name+"</A></TT></TD>"+
            "<TD><TT>"+size+"</TT></TD></TR>\n";       
      }       
      return  getBytes(text +"</TABLE><HR>" +      
       "</BODY></HTML>");
   }
   
   private byte[] getBytes(String text){
      try {
         return text.getBytes("utf-8");
      }catch(IOException never) {
         return null;
      }
   }

   public String getContentType(){      
      return "text/html; charset=utf-8";
   }
}


