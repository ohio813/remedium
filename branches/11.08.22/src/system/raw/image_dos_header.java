package system.raw;

import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nuno Brito, 26th of March 2011 in Germany.
 */
public class image_dos_header {

   // are we debugging this class? (output debug messages)
      protected boolean
        debug = false;

   // IMAGE_DOS_HEADER
      private String
        e_magic;

      private int
        e_cblp,
        e_cp,
        e_crlc,
        e_cparhdr,
        e_minalloc,
        e_maxalloc,
        e_ss,
        e_sp,
        e_csum,
        e_ip,
        e_cs,
        e_lfarlc,
        e_ovno;

     private int[]
        e_res = new int[4]; //4

     private int
        e_oemid,
        e_oeminfo;

      final int[]
        e_res2 = new int[10];//10

      private long
        e_lfanew = 0;



    // objects
      private BinaryFile bin;

  /**
   *  Read a given DOS header and populate the data fields.
   * @param filename
   * @return True if we successfuly read the DOS header
   */
 public Boolean read (RandomAccessFile file )
        {
    try {
      bin = new BinaryFile(file); // open the file for binary access
            	bin.setSigned(false);
            	bin.setEndian(BinaryFile.LITTLE_ENDIAN);


      // get the magic signature
      e_magic = bin.readFixedString(2);

      // we want to see a magic signature here
      if(e_magic.equalsIgnoreCase("MZ")==false){
          log("ERROR","Read operation failed, no magic signature was found");
          return false;
      }

      e_cblp =  bin.readWord();
      e_cp =  bin.readWord();
      e_crlc =  bin.readWord();
      e_cparhdr =  bin.readWord();
      e_minalloc =  bin.readWord();
      e_maxalloc =  bin.readWord();
      e_ss =  bin.readWord();
      e_sp =  bin.readWord();
      e_csum =  bin.readWord();
      e_ip =  bin.readWord();
      e_cs =  bin.readWord();
      e_lfarlc =  bin.readWord();
      e_ovno =  bin.readWord();

      for (int i = 0; i < this.e_res.length; i++) {
        e_res[i] = bin.readWord();}//4x times

      e_oemid = bin.readWord();
      e_oeminfo = bin.readWord();

      for (int i = 0; i < e_res2.length; i++) {
        e_res2[i] = bin.readWord();}//10x times

      e_lfanew = bin.readDWord();


      debug();
      //file.close();
  
            } catch (Exception ex) {
             log("error","IO exception = " + ex );
            return false;
       }
   return true;
    }

 /**
  * Returns the address of the LFA structure
  */
     public long getLFAnew() {
        return e_lfanew;
    }

   /** output log messages in a standard manner */
 private void log(String gender, String message){
     //TODO We should default this to our standard log system
     if(debug)
        System.out.println("[DOS_header]["+gender+"] "+message);
 }

 /** Output all the values that we got from this structure */
  protected void debug(){
   if(!debug) return; // exit if we are not debugging
   log("DEBUG","Results: "
      +utils.text.doFormat("e_magic",e_magic)
      +utils.text.getHex("e_cblp",e_cblp)
      +utils.text.getHex("e_cp",e_cp)
      +utils.text.getHex("e_crlc",e_crlc)
      +utils.text.getHex("e_cparhdr",e_cparhdr)
      +utils.text.getHex("e_minalloc",e_minalloc)
      +utils.text.getHex("e_maxalloc",e_maxalloc)
      +utils.text.getHex("e_ss",e_ss)
      +utils.text.getHex("e_sp",e_sp)
      +utils.text.getHex("e_csum",e_csum)
      +utils.text.getHex("e_ip",e_ip)
      +utils.text.getHex("e_cs",e_cs)
      +utils.text.getHex("e_lfarlc",e_lfarlc)
      +utils.text.getHex("e_ovno",e_ovno)
      +utils.text.getHex("e_oemid",e_oemid)
      +utils.text.getHex("e_oeminfo",e_oeminfo)
      +utils.text.getHex("e_lfanew",e_lfanew)
           );
  }

  }


