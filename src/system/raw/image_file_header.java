
package system.raw;

import java.io.RandomAccessFile;

/**
 *
 * @author Nuno Brito, 26th of March, 2011 in Germany.
 */
public class image_file_header {

   // settings
    private Boolean
            debug = false;

   // variables
   private int
      Machine,
      NumberOfSections;

   private long
      TimeDateStamp,
      PointerToSymbolTable,
      NumberOfSymbols;

   private int
      SizeOfOptionalHeader,
      Characteristics;

  private BinaryFile
           bin;

  private RandomAccessFile
           file;

 /**
  * This method reads the header field of a given NT executable.
  */
   public Boolean read (RandomAccessFile filename, long e_lfanew_offset)
       {
        try {

      file= filename;
      bin = new BinaryFile(file);
            	bin.setSigned(false);
            	bin.setEndian(BinaryFile.LITTLE_ENDIAN);

            file.seek(e_lfanew_offset+4);

            Machine = bin.readWord();
            NumberOfSections = bin.readWord();
            TimeDateStamp = bin.readDWord();
            PointerToSymbolTable = bin.readDWord();
            NumberOfSymbols = bin.readDWord();
            SizeOfOptionalHeader = bin.readWord();
            Characteristics = bin.readWord();


        } catch (Exception ex) {
            log("ERROR","Read operation failed, error while reading fields");
            return false;
        }

            debug();

        // operation was successful
        return true;
       }

   /** output log messages in a standard manner */
    private void log(String gender, String message){
     System.out.println("[file_header]["+gender+"] "+message);
 }

  /** Output all the values that we got from this structure */
  protected void debug(){
   if(!debug) return; // exit if we are not debugging
   log("DEBUG","Results: "
      +utils.text.getHex("Machine",Machine)
      +utils.text.getHex("NumberOfSections",NumberOfSections)
      +utils.text.getHex("TimeDateStamp",TimeDateStamp)
      +utils.text.getHex("PointerToSymbolTable",PointerToSymbolTable)
      +utils.text.getHex("NumberOfSymbols",NumberOfSymbols)
      +utils.text.getHex("SizeOfOptionalHeader",SizeOfOptionalHeader)
      +utils.text.getHex("Characteristics",Characteristics)
           );
  }

  /* Get the number of sections that were reported inside this image */
  public int getNumberOfSections() {
        return NumberOfSections;
    }

  /** We need this value to know where the header ends and resources begin */
    public int getSizeOfOptionalHeader() {
        return SizeOfOptionalHeader;
    }


  }


