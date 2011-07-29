/*
 * This test will verify if we are able of reading a few Win32 files.
 */

package system.raw;

import java.io.File;
import org.junit.Test;

/**
 *
 * @author Nuno Brito, 31st of March 2011 in Germany.
 */
public class ReadWin32FileTest {


//     String file_to_test = "d:\\test\\C2J.exe"; // change this value at your desire
//     String file_to_test = "d:\\test\\kernel32.dll"; // change this value at your desire
//     String file_to_test = "d:\\test\\brood-c.exe"; // change this value at your desire
    String file_to_test = "d:\\test\\DivX.exe"; // change this value at your desire

    public ReadWin32FileTest() {
    }


     @Test
     public void testWin32() {

      File file = new File(file_to_test);

      // get the correct canonic path of this file
      String target = file.getAbsolutePath();
      file =null;


      image_win_executable
                win_exe = new image_win_executable();
      
               if(!win_exe.read(target)){
                   System.out.println("Unable to read version from " + target);
                                 return;}


      System.out.println("----->"+target
                +" v="+win_exe.getVersion()
                +" lang="+win_exe.getLanguage()
                +" arch="+win_exe.getArchitecture()
                );


     }

}