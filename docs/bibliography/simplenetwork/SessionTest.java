package org.simpleframework.http.core;

import java.util.Map;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.StatusLine;
import org.simpleframework.http.session.Session;

public class SessionTest extends PerformanceTest {
   
   @Scenario(requests=10, concurrency=20, repeat=10, debug=true)
   public Analyser testSession() throws Exception {    
      return new Analyser() {       
         public void request(StringBuilder address, Message header, StringBuilder body) throws Exception {
            address.append("/index.html");
            header.add("Host", "localhost");
            header.add("User-Agent", "IE/5.0");  
         }
         public void handle(Request req, Response resp, Map map) throws Exception {
            Session session = req.getSession();
            
            if(req.getCookie("JSESSIONID") != null) {
               assertEquals(session.get("A"), "b");
               assertEquals(session.get("B"), "c");               
            } else {               
               assertTrue(session.isEmpty());
               session.put("A", "b");
               session.put("B", "c");
            }            
            resp.set("Server", "Apache/1.2");              
         }
         public void analyse(StatusLine line, Message resp, Body body, Map map) throws Exception {
            assertEquals(line.getMajor(), 1);
            assertEquals(line.getMinor(), 1);
            assertEquals(line.getCode(), 200);
            assertEquals(body.getContent(), "");
            assertEquals(resp.getValue("Server"), "Apache/1.2");
            assertEquals(resp.getValue("Connection"), "keep-alive");    
         }
      };
   }

}
