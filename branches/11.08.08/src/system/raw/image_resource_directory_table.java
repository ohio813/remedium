package system.raw;

import java.io.RandomAccessFile;

/**
 *
 * @author Nuno Brito, 27th of March 2011 in Germany.
 */
public class image_resource_directory_table {

    // settings
    private Boolean
      debug = false;

    private long // DWORD
      Characteristics,
      TimeDateStamp;

    private int // WORD
      MajorVersion,
      MinorVersion,
      NumberOfNamedEntries,
      NumberOfIdEntries;

    private image_resource_directory_entry[]
      entry;

    private  BinaryFile
      bin;

    private RandomAccessFile
      file;

    public Boolean read (RandomAccessFile filename, String title, long offset)
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

            Characteristics = bin.readDWord();
            TimeDateStamp = bin.readDWord();
            MajorVersion = bin.readWord();
            MinorVersion = bin.readWord();
            NumberOfNamedEntries = bin.readWord();
            NumberOfIdEntries = bin.readWord();


            // create an array to read all entries
            int n = NumberOfIdEntries + NumberOfNamedEntries;
            entry = new image_resource_directory_entry[n];

            // TODO: This is not working with files such as "DivX Plus Player.exe"
            if (n>0 && n<100){
               for (int r = 0; r < n; r++)
                          {
                          entry[r] = new image_resource_directory_entry();
                          entry[r].read(filename, title, offset+16+(r*8) );
                       }
                    }


        } catch (Exception ex) {
            log("ERROR","Read operation failed: "+ex.toString());
            return false;
        }
        debug();
        return true;
       }

   /** output the results from the read operation */
  public void debug ()
       {
      if(debug==false)
          return;
      //System.out.println("[RESOURCE_DIRECTORY_TABLE] "+title);
      log("DEBUG",""
      +utils.text.getHex("Characteristics",Characteristics)
      +utils.text.getHex("TimeStamp",TimeDateStamp)
      +utils.text.getHex("Major",MajorVersion)
      +utils.text.getHex("Minor",MinorVersion)
      +utils.text.getHex("NumberOfNamedEntries",NumberOfNamedEntries)
      +utils.text.getHex("NumberOfIdEntries: ",NumberOfIdEntries)
      );
  }

  /** number of entries without a title, just a number */
    public int getNumberOfIdEntries() {
        return NumberOfIdEntries;
    }

  /** number of entries that hold a title */
    public int getNumberOfNamedEntries() {
        return NumberOfNamedEntries;
    }

    public image_resource_directory_entry getEntry(int i) {
        return entry[i];
    }



     /** output log messages in a standard manner */
    private void log(String gender, String message){
     System.out.println("[rsrc_dir_table]["+gender+"] "+message);
 }

  }

