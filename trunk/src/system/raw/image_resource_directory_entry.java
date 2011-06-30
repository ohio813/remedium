package system.raw;

import java.io.RandomAccessFile;

/**
 *
 * @author Nuno Brito, 27th of March 2011 in Germany.
 */
public class image_resource_directory_entry {

    private long         // DWORD
      ID;                // A 32-bit integer that identifies the Type, Name, or Language ID entry.

    private int
      OffsetToDirectory; // High bit 0. Address of a Resource Data entry (a leaf).

    private short        // High bit 1. The lower 31 bits are the address of another resource directory table (the next level down).
      Flag;              // Flag = 0 Points to data and Flag = 128 points to sub-level resource_dir


/*
    TYPES OF ID

1  	RT_CURSOR
2 	RT_BITMAP
3 	RT_ICON
4 	RT_MENU
5 	RT_DIALOG
6 	RT_STRING
7 	RT_FONTDIR
8 	RT_FONT
9 	RT_ACCELERATOR
10 	RT_RCDATA
11 	RT_MESSAGETABLE
12 	RT_GROUP_CURSOR
14 	RT_GROUP_ICON
16 	RT_VERSION
17 	RT_DLGINCLUDE
19 	RT_PLUGPLAY
20 	RT_VXD
21 	RT_ANICURSOR
22 	RT_ANIICON
23 	RT_HTML
24 	RT_MANIFEST
*/
      private BinaryFile 
              bin;

      private RandomAccessFile 
              file;

      private String
              title = ""; // custom title for each instace of this class

 /** Read the resource entry */
 public Boolean read ( RandomAccessFile filename, String title, long offset)
       {
     try {

      file = filename;
      bin  = new BinaryFile(file);
            	bin.setSigned(false);
            	bin.setEndian(BinaryFile.LITTLE_ENDIAN);

            file.seek(offset);

            ID = bin.readDWord();
            OffsetToDirectory = bin.readWord();
            //Hackish way of dealing with 0x80 000 000
                   bin.readByte();
            Flag = bin.readByte();
                           

        } catch (Exception ex) {
            log("ERROR","Read operation failed at file '"
                    +title
                    +"': "+ex.toString());
        } 

        return true;
       }

  public void debug ()
       {

      String gender = "";

if (ID==1) gender = "RT_CURSOR";
else if (ID==2) gender = "RT_BITMAP";
else if (ID==3) gender = "RT_ICON";
else if (ID==4) gender = "RT_MENU";
else if (ID==5) gender = "RT_DIALOG";
else if (ID==6) gender = "RT_STRING";
else if (ID==7) gender = "RT_FONTDIR";
else if (ID==8) gender = "RT_FONT";
else if (ID==9) gender = "RT_ACCELERATOR";
else if (ID==10) gender = "RT_RCDATA";
else if (ID==11) gender = "RT_MESSAGETABLE";
else if (ID==12) gender = "RT_GROUP_CURSOR";
else if (ID==14) gender = "RT_GROUP_ICON";
else if (ID==16) gender = "RT_VERSION";
else if (ID==17) gender = "RT_DLGINCLUDE";
else if (ID==19) gender = "RT_PLUGPLAY";
else if (ID==20) gender = "RT_VXD";
else if (ID==21) gender = "RT_ANICURSOR";
else if (ID==22) gender = "RT_ANIICON";
else if (ID==23) gender = "RT_HTML";
else if (ID==24) gender = "RT_MANIFEST";

      //String java.lang.Integer.toHexString( int )

      System.out.println("[RESOURCE_DIRECTORY_ENTRY] "+title);
      System.out.println("ID: 0x"+java.lang.Long.toHexString(ID)+" ("+gender+")");
      System.out.println("Flag: 0x"+java.lang.Long.toHexString(Flag));
      System.out.println("OffsetToDirectory: 0x"+java.lang.Long.toHexString(OffsetToDirectory));
      System.out.println();
        }

   /** output log messages in a standard manner */
    private void log(String gender, String message){
     System.out.println("[rsrc_dir_entry]["+gender+"] "+message);
 }

    /** get the offset to the file location where we can read this structure*/
    public int getOffsetToDirectory() {
        return OffsetToDirectory;
    }

    /** Get the ID regarding the type of entry that this is*/
    public long getID() {
        return ID;
    }

  }

