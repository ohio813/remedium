package system.raw;

import java.io.RandomAccessFile;


/**
 *
 * @author Nuno Brito, 27th of March 2011 in Germany
 */
public class image_data_directory {
    private long // DWORD
            VirtualAddress,
            Size;

    private BinaryFile
            bin;
    private RandomAccessFile
            file;


 public Boolean read ( RandomAccessFile filename, long offset)
       {
        try {

      file = filename;
      bin = new BinaryFile(file);
            	bin.setSigned(false);
            	bin.setEndian(BinaryFile.LITTLE_ENDIAN);

            file.seek(offset);

            VirtualAddress = bin.readDWord();
            Size = bin.readDWord();


        } catch (Exception ex) {
            log("ERROR","Read operation failed: "+ex.toString());
            return false;
        }

        return true;
       }

     /** output log messages in a standard manner */
    private void log(String gender, String message){
     System.out.println("[data_dir]["+gender+"] "+message);
     }

    public void setSize(long Size) {
        this.Size = Size;
    }

    public void setVirtualAddress(long VirtualAddress) {
        this.VirtualAddress = VirtualAddress;
    }

  
    

}
