package system.raw;

import java.io.RandomAccessFile;


/**
 *
 * Good explanation of this section can be found at:
 * http://msdn.microsoft.com/en-us/library/ms680341%28VS.85%29.aspx
 * http://win32assembly.online.fr/pe-tut5.html
 *
 * @author Nuno Brito, 26 of March 2011 in Germany
 */
public class image_section_header {

    private Boolean
     debug = false; // should we debug this class or not?
    
    private String
      Name;

     private long // DWORD
      startOffset,
      //PhysicalAddress,
      VirtualSize,
      VirtualAddress,
      SizeOfRawData,
      PointerToRawData,
      PointerToRelocations,
      PointerToLinenumbers;

     private int // WORD
      NumberOfRelocations,
      NumberOfLineNumbers;

     private long // DWORD
      Characteristics;

     private BinaryFile
             bin;

     private RandomAccessFile
             file;

 /**
  * Read the section
  */
public Boolean Read ( RandomAccessFile filename, long offset)
       {
        try {

      file = filename;
      bin = new BinaryFile(file);
            	bin.setSigned(false);
            	bin.setEndian(BinaryFile.LITTLE_ENDIAN);

            // record this value to know where we started reading
            startOffset = offset;

            file.seek(offset);

            Name = bin.readFixedString(8);
            VirtualSize = bin.readDWord();
            VirtualAddress = bin.readDWord();
            SizeOfRawData = bin.readDWord();
            PointerToRawData = bin.readDWord();
            PointerToRelocations = bin.readDWord();
            PointerToLinenumbers = bin.readDWord();
            NumberOfRelocations = bin.readWord();
            NumberOfLineNumbers = bin.readWord();
            Characteristics = bin.readDWord();


        } catch (Exception ex) {
            log("ERROR"," Read operation failed: "+ex.toString());
            return false;
        } 
        debug();
        return true;
       }


  /** Output all the values that we got from this structure */
  protected void debug(){
   if(!debug) return; // exit if we are not debugging
   log("DEBUG",""
      +utils.text.getHex("offset",startOffset)
      +utils.text.doFormat("Name",Name)
      +utils.text.getHex("VirtualSize",VirtualSize)
      +utils.text.getHex("VirtualAddress",VirtualAddress)
      +utils.text.getHex("SizeOfRawData",SizeOfRawData)
      +utils.text.getHex("PointerToRawData",PointerToRawData)
      +utils.text.getHex("PointerToRelocations",PointerToRelocations)
      +utils.text.getHex("PointerToLinenumbers",PointerToLinenumbers)
      +utils.text.getHex("NumberOfRelocations",NumberOfRelocations)
      +utils.text.getHex("NumberOfLineNumbers",NumberOfLineNumbers)
      +utils.text.getHex("Characteristics",Characteristics)
           );
  }

  /** Get the name corresponding to this section */
    public String getName() {
        return Name;
    }

    /** Get the pointer to where the real fun can be found */
    public long getPointerToRawData() {
        return PointerToRawData;
    }

    /** the symbolic address of this section inside the structure */
    public long getVirtualAddress() {
        return VirtualAddress;
    }



   /** output log messages in a standard manner */
    private void log(String gender, String message){
     System.out.println("[section_header]["+gender+"] "+message);
 }

  }
