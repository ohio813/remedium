package system.raw;

import java.io.RandomAccessFile;

/**
 *
 * @author Nuno Brito, 26th of March 2011 in Germany.
 */
public class image_optional_header {

   // settings
   private boolean debug = false;


   // variables
   private int
      Magic;

   private short
      MajorLinkerVersion,
      MinorLinkerVersion;

   private long
      startOffset,
      SizeOfCode,
      SizeOfInitializedData,
      SizeOfUninitializedData,
      AddressOfEntryPoint,
      BaseOfCode,
      BaseOfData,
      ImageBase,
      SectionAlignment,
      FileAlignment;

   private int // WORD
      MajorOperatingSystemVersion,
      MinorOperatingSystemVersion,
      MajorImageVersion,
      MinorImageVersion,
      MajorSubSystemVersion,
      MinorSubSystemVersion;

   private long // DWORD
      Win32VersionValue,
      SizeOfImage,
      SizeOfHeaders,
      Checksum;

   private int
      Subsystem,
      DllCharacteristics;

   private long
      SizeOfStackReserve,
      SizeOfStackCommit,
      SizeOfHeapReserve,
      SizeOfHeapCommit,
      LoaderFlags,
      NumberOfRvaAndSizes;

    private image_data_directory[]
      DataDirectory = new image_data_directory[15];
 
    private BinaryFile
      bin;

    private RandomAccessFile
      file;


    /**
     * This method will read the optional header, albeit called "optional",
     * it is the most important structure on a modern PE header since it
     * contains the most relevant data.
     */
  public Boolean read ( RandomAccessFile filename, long offset)
       {
        try {

      file = filename; // get our file pointer

      bin = new BinaryFile(file); // fire up the binary object
            	bin.setSigned(false);
            	bin.setEndian(BinaryFile.LITTLE_ENDIAN);

            startOffset = offset+24;
            file.seek(offset+24); // jump + 0x18 or 24(10)// get to the location

      Magic = bin.readWord(); // 0x10b = x86 exe, 0x20b = x64 exe
      MajorLinkerVersion = bin.readByte();
      MinorLinkerVersion = bin.readByte();
      SizeOfCode = bin.readDWord();
      SizeOfInitializedData = bin.readDWord();
      SizeOfUninitializedData = bin.readDWord();
      AddressOfEntryPoint = bin.readDWord();
      BaseOfCode = bin.readDWord();
      BaseOfData = bin.readDWord();
      ImageBase = bin.readDWord();
      SectionAlignment = bin.readDWord();
      FileAlignment = bin.readDWord();
      MajorOperatingSystemVersion = bin.readWord();
      MinorOperatingSystemVersion = bin.readWord();
      MajorImageVersion = bin.readWord();
      MinorImageVersion = bin.readWord();
      MajorSubSystemVersion = bin.readWord();
      MinorSubSystemVersion = bin.readWord();
      Win32VersionValue = bin.readDWord();
      SizeOfImage = bin.readDWord();
      SizeOfHeaders = bin.readDWord();
      Checksum = bin.readDWord();
      Subsystem = bin.readWord();
      DllCharacteristics = bin.readWord();
      SizeOfStackReserve = bin.readDWord();
      SizeOfStackCommit = bin.readDWord();
      SizeOfHeapReserve = bin.readDWord();
      SizeOfHeapCommit = bin.readDWord();
      LoaderFlags = bin.readDWord();
      NumberOfRvaAndSizes = bin.readDWord();


       for (int i = 0; i < DataDirectory.length; i++)
                {
                DataDirectory[i] = new image_data_directory();
                DataDirectory[i].setVirtualAddress(bin.readDWord());
                DataDirectory[i].setSize(bin.readDWord());
                }

      
        } catch (Exception ex) {
            log("ERROR","Read operation, failed to read values from structure"
                    + ": "+ex.toString());
            return false;
        }
            // output our results if desired
            debug();
        return true;
       }

    /** output log messages in a standard manner */
    private void log(String gender, String message){
     System.out.println("[Optional_header]["+gender+"] "+message);
 }

 


    /** Output all the values that we got from this structure */
  protected void debug(){
   if(!debug) return; // exit if we are not debugging
   log("DEBUG",""
      +utils.text.getHex("offset",startOffset)
      +utils.text.getHex("Magic",Magic)
      +utils.text.getHex("MajorLinkerVersion",MajorLinkerVersion)
      +utils.text.getHex("MinorLinkerVersion",MinorLinkerVersion)
      +utils.text.getHex("SizeOfCode",SizeOfCode)
      +utils.text.getHex("SizeOfInitializedData",SizeOfInitializedData)
      +utils.text.getHex("SizeOfUninitializedData",SizeOfUninitializedData)
      +utils.text.getHex("AddressOfEntryPoint",AddressOfEntryPoint)
      +utils.text.getHex("BaseOfCode",BaseOfCode)
      +utils.text.getHex("BaseOfData",BaseOfData)
      +utils.text.getHex("ImageBase",ImageBase)
      +utils.text.getHex("SectionAlignment",SectionAlignment)
      +utils.text.getHex("FileAlignment",FileAlignment)
      +utils.text.getHex("MajorOperatingSystemVersion",MajorOperatingSystemVersion)
      +utils.text.getHex("MinorOperatingSystemVersion",MinorOperatingSystemVersion)
      +utils.text.getHex("MajorImageVersion",MajorImageVersion)
      +utils.text.getHex("MinorImageVersion",MinorImageVersion)
      +utils.text.getHex("MajorSubSystemVersion",MajorSubSystemVersion)
      +utils.text.getHex("MinorSubSystemVersion",MinorSubSystemVersion)
      +utils.text.getHex(" Win32VersionValue", Win32VersionValue)
      +utils.text.getHex("SizeOfImage",SizeOfImage)
      +utils.text.getHex("SizeOfHeaders",SizeOfHeaders)
      +utils.text.getHex("Checksum",Checksum)
      +utils.text.getHex("Subsystem",Subsystem)
      +utils.text.getHex("DllCharacteristics",DllCharacteristics)
      +utils.text.getHex("SizeOfStackReserve",SizeOfStackReserve)
      +utils.text.getHex("SizeOfStackCommit",SizeOfStackCommit)
      +utils.text.getHex("SizeOfHeapReserve",SizeOfHeapReserve)
      +utils.text.getHex("SizeOfHeapCommit",SizeOfHeapCommit)
      +utils.text.getHex("LoaderFlags",LoaderFlags)
      +utils.text.getHex("NumberOfRvaAndSizes",NumberOfRvaAndSizes)
           );
  }


  // get the CPU architecture of this executable (x86, x64)
    public int getMagic() {
        return Magic;
    }




}
