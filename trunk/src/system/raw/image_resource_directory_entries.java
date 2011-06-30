package system.raw;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nuno
 */
public class image_resource_directory_entries {
    long // DWORD
      NameRVA, // The address of a string that gives the Type, Name, or Language ID entry, depending on level of table.
      IntegerID, // A 32-bit integer that identifies the Type, Name, or Language ID entry.
      DataEntryRVA, // High bit 0. Address of a Resource Data entry (a leaf).
      SubDirectoryRVA; // High bit 1. The lower 31 bits are the address of another resource directory table (the next level down).

    public long getDataEntryRVA() {
        return DataEntryRVA;
    }

    public long getIntegerID() {
        return IntegerID;
    }

    public long getNameRVA() {
        return NameRVA;
    }

    public long getSubDirectoryRVA() {
        return SubDirectoryRVA;
    }

    // common variables
      private BinaryFile bin;
      private RandomAccessFile file;

       public Boolean read ( String filename, long offset)
       {
     try {

      file = new RandomAccessFile(filename,"r");
      bin  = new BinaryFile(file);
            	bin.setSigned(false);
            	bin.setEndian(BinaryFile.LITTLE_ENDIAN);

            file.seek(offset);

            NameRVA = bin.readDWord();
            IntegerID = bin.readDWord();
            DataEntryRVA = bin.readWord();
            SubDirectoryRVA = bin.readWord();
          
        } catch (Exception ex) {
            Logger.getLogger(image_file_header.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally{
            try {
                file.close();
            } catch (IOException ex) {
                Logger.getLogger(image_file_header.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return true;
       }
  }

