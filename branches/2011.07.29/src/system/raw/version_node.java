package system.raw;

import java.io.RandomAccessFile;


/**
 *
 * @author Nuno Brito, 27th of March 2011 in Germany
 *
 * Structure based on info from:
 * http://blogs.msdn.com/b/oldnewthing/archive/2006/12/21/1340571.aspx
 *
 */
public class version_node {

   private int // WORD
    Node,
    Data,
    Type; // 0 (binary data);   1 (string data)

   private char []
    Name,
    Value;

   private int []
    rgbPadding1,
    rgbPadding2;

   private byte[]
    rgbData;
   version_node []
    children;
   ////////
   private long
    Signature,
    StructVersion;

   private int
    FileVersionMS1,
    FileVersionMS2,
    FileVersionLS1,
    FileVersionLS2;

   private long
    FileFlagsMasks,
    FileFlags,
    FileOS, // 00 04 00 04 = VOS_NT_WINDOWS32
    FileType, // VFT_DLL
    FileSubtype,
    FileDateMS,
    FileDateLS;


    private String
            out="",
            title = "";

    // common variables
      private BinaryFile bin;
      private RandomAccessFile file;


       public Boolean read ( RandomAccessFile filename, long offset)
       {
      
     try {

      file = filename;
      bin  = new BinaryFile(file);
            	bin.setSigned(false);
            	bin.setEndian(BinaryFile.LITTLE_ENDIAN);

          // check if we can indeed read this structure
          if(
            (offset>=file.length())
             || (offset < 0) // there is a defect on the calcs from nt_headers:180
                             // this is not working on MSORES.DLL from OFFICE12
            )
              return false;


            file.seek(offset);

            Node = bin.readWord();
            Data = bin.readWord();
            Type = bin.readWord(); // Type = 0 is binary data

            rgbData = new byte[Data];
           // Boolean keepReading = true;
            for (int i = 0; i < rgbData.length; i++){
              // if (keepReading)
                rgbData[i] = (byte) bin.readByte();
               // smart ass way to ensure we break the reading
               // after \00 \00 but continue to fill the empty
               // bytes to keep on reading the structure
                if ((rgbData[i]==0)&&(rgbData[i-1]==0))
                //    keepReading=false;
                    break;
            }
            //version_node way of jumping back to track
            file.seek(file.getFilePointer()+3);

            // convert text from unicode bytes to string
            title = new String(rgbData, "UTF-8");

   // long
    Signature = bin.readDWord();
    StructVersion = bin.readDWord();
   // int
    FileVersionMS1 = bin.readWord();
    FileVersionMS2 = bin.readWord();
    FileVersionLS1 = bin.readWord();
    FileVersionLS2 = bin.readWord();
   // long
    FileFlagsMasks = bin.readDWord();
    FileFlags = bin.readDWord();
    FileOS = bin.readDWord(); // 00 04 00 04 = VOS_NT_WINDOWS32
    FileType = bin.readDWord(); // VFT_DLL
    FileSubtype = bin.readDWord();
    FileDateMS = bin.readDWord();
    FileDateLS = bin.readDWord();

    out = FileVersionMS2+"."+FileVersionMS1+"."
         +FileVersionLS2+"."+FileVersionLS1;

        } catch (Exception ex) {
            //TODO There are exceptions that occur here, we are just ignoring them..
            log("ERROR","Read operation failed: "+ex.toString());
            out = "";
            return false;
        } 

        return true;
       }

  public void debug ()
       {
      System.out.println("[VERSION_NODE] "+title);
      System.out.println("Node: 0x"+java.lang.Long.toHexString(Node));
      System.out.println("Data: 0x"+java.lang.Long.toHexString(Data));
      System.out.println("Type: 0x"+java.lang.Long.toHexString(Type));

       // long
    System.out.println("Signature: 0x"+java.lang.Long.toHexString(Signature));
    System.out.println("StructVersion: 0x"+java.lang.Long.toHexString(StructVersion));
   // int
    System.out.println("FileVersion: "+FileVersionMS2+"."+FileVersionMS1);
     System.out.println("FileVersionLS = "+FileVersionLS1+"."+FileVersionLS2);
   // long
    System.out.println("FileFlagsMasks: 0x"+java.lang.Long.toHexString(Node));
    System.out.println("FileFlags: 0x"+java.lang.Long.toHexString(Node));
    System.out.println("FileOS: 0x"+java.lang.Long.toHexString(Node));
    System.out.println("FileType: 0x"+java.lang.Long.toHexString(Node));
    System.out.println("FileSubtype: 0x"+java.lang.Long.toHexString(Node));
    System.out.println("FileDateMS: 0x"+java.lang.Long.toHexString(Node));
    System.out.println("FileDateLS: 0x"+java.lang.Long.toHexString(Node));

      System.out.println();
        }

       /** output log messages in a standard manner */
    private void log(String gender, String message){
     System.out.println("[version]["+gender+"] "+message);
 }

    public String getVersionText() {
        return out;
    }


  }


