package system.raw;

import java.io.RandomAccessFile;


/**
 *
 * @author Nuno Brito, 27th of March 2011 in Germany
 */
public class resource_data_entry {

   private long // DWORD
       DataRVA, // The address of a unit of resource data in the Resource Data area.
       Size, // The size, in bytes, of the resource data that is pointed to by the Data RVA field.
       Codepage, // The code page that is used to decode code point values within the resource data. Typically, the code page would be the Unicode code page.
       Reserved; // Reserved, must be 0.

  private BinaryFile
       bin;
  
  private RandomAccessFile
       file;

  
  public Boolean read (RandomAccessFile filename, long offset)
       {
     try {

      file = filename;
      bin  = new BinaryFile(file);
            	bin.setSigned(false);
            	bin.setEndian(BinaryFile.LITTLE_ENDIAN);

            file.seek(offset);

            DataRVA = bin.readDWord();
            Size = bin.readDWord();
            Codepage = bin.readDWord();
            Reserved = bin.readDWord();


        } catch (Exception ex) {
            log("ERROR","Read operation failed: "+ex.toString());
            return false;
        }
        return true;
       }

  public void debug ()
       {
     // System.out.println("[RESOURCE_DATA_ENTRY] "+title);
      System.out.println("DataRVA: 0x"+java.lang.Long.toHexString(DataRVA));
      System.out.println("Size: 0x"+java.lang.Long.toHexString(Size));
      System.out.println("Codepage: 0x"+java.lang.Long.toHexString(Codepage));
      System.out.println("Reserved: 0x"+java.lang.Long.toHexString(Reserved));
      System.out.println();
        }

     /** output log messages in a standard manner */
    private void log(String gender, String message){
     System.out.println("[rsrc_data_entry]["+gender+"] "+message);
 }

    /** Relative Value Address */
    public long getDataRVA() {
        return DataRVA;
    }



  }


