package system.raw;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nuno
 */
public class image_resource_directory_string {
    int // WORD
      length; // The size of the string, not including length field itself.
    String
      UnicodeString; // The variable-length Unicode string data, word-aligned.


    // common variables
      private static  BinaryFile bin;
      private static  RandomAccessFile file;

  @SuppressWarnings("static-access")
       public Boolean read ( String filename, long offset)
       {
     try {

      file = new RandomAccessFile(filename,"r");
      bin  = new BinaryFile(file);
            	bin.setSigned(false);
            	bin.setEndian(BinaryFile.LITTLE_ENDIAN);

            file.seek(offset);

            length = bin.readWord();
            UnicodeString = bin.readFixedString(length);
          
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

