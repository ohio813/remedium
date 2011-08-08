/*
 * File Reader is optimized for methods of ContainerFlatFile to read lines.
 * It will use techniques that avoid strings and other objects that are not
 * easily digested when processing thousands of lines within a short amount of
 * time.
 * 
 * Part of the code inside this class was retrieved from the Internet,
 * credits are due to Daniel Lord and Achut Reddy for their white paper 
 * entitled: "Java I/O Performance Tuning" that was used as reference.
 */
package system.container;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 *
 * @author Nuno Brito, 29th of July 2011 in Darmstadt, Germany
 */
public class FlatfileReader {

    
    private void test(String[] args){
    // This example uses character arrays instead of Strings.
// It doesn't use BufferedReader or BufferedFileReader, but does
// the buffering by itself so that it can avoid creating too many
// String objects.  For simplicity, it assumes that no line will be
// longer than 128 characters.

FileReader fr;
int nlines = 0;
char buffer[] = new char[8192 + 1];
int maxLineLength = 128;

//assumes no line is longer than this
char lineBuf[] = new char[maxLineLength];
        
int nargs = 0;
        
for (int i=0; i < nargs; i++) {
try {
    fr = new FileReader(args[i]);

    int nChars = 0;
    int nextChar = 0;
    int startChar = 0;
    boolean eol = false;
    int lineLength = 0;
    char c = 0;
    int n;
    int j;

    while (true) {
  	if (nextChar >= nChars) {
	n = fr.read(buffer, 0, 8192);
	if (n == -1) {  // EOF
	    break;
	}
    nChars = n;
    startChar = 0;
    nextChar = 0;
    }

    for (j=nextChar; j < nChars; j++) {
    	c = buffer[j];
	if ((c == '\n') || (c == '\r')) {
	    eol = true;
	    break;
	}
    }
    nextChar = j;

    int len = nextChar - startChar;
    if (eol) {
	nextChar++;
 	if ((lineLength + len) > maxLineLength) {
	    // error
    	} else {
	    System.arraycopy(buffer, startChar, lineBuf, lineLength, len);
    	}
        lineLength += len;

        //
        // Process line here
        //
        nlines++;
  
	if (c == '\r') {
    	    if (nextChar >= nChars) {
	        n = fr.read(buffer, 0, 8192);
	        if (n != -1) {
		    nextChar = 0;
		    nChars = n;
	        }
	   }

   	   if ((nextChar < nChars) && (buffer[nextChar] == '\n'))
	       nextChar++;
    	}
        startChar = nextChar;
        lineLength = 0;
        continue;
    }

    if ((lineLength + len) > maxLineLength) {
    	// error
    } else {
    	System.arraycopy(buffer, startChar, lineBuf, lineLength, len);
    }
    	lineLength += len;
    }
    fr.close();
} catch (Exception e) {
    System.out.println("exception: " + e);
    }

}
    
    
    }
    
    
    /** Look for a specific record inside the CSV file and write new values*/
    // this method has crappy performance
    public static boolean changeRecordFromFile(File inFile, final String[] fields,
            boolean hasChanged) {
        
        String 
                lineToRemove = fields[0], // identifier of line to remove
                lineToAdd = utils.text.convertRecordToString(fields); // new line
        // reset to default value as false
        hasChanged = false;

        try {
              //Construct the new file that will later be renamed to the original filename. 
              File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

              BufferedReader br = new BufferedReader(new FileReader(inFile));
              PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

              String line = null;

              //Read from the original file and write to the new 
              //unless content matches data to be removed.
              while ((line = br.readLine()) != null) {
                  // split each record into fields
                  String[] data = line.split(";");
                  // look for our specific record
                
                  if (data[0].equals(lineToRemove)){
                      // change our record with the new values
                      line = lineToAdd;
                      hasChanged = true; // flag that we have changed something
                  }
                  // write the results back to the new file
                  pw.println(line);
                  pw.flush();
              }
              // close our input/output files
              pw.close();
              br.close();
      
    // have we changed something?          
    if(hasChanged == true) {          
      //Delete the original file
      if (!inFile.delete()) {
        System.out.println("Could not delete file");
        return false;
      } 
     //Rename the new file to the filename the original file had.
     if (!tempFile.renameTo(inFile))
        System.out.println("Could not rename file");
     }
     else{ // no changes occurred, just delete the temp file
        if (!tempFile.delete()) {
            System.out.println("Could not delete file");
            return false;
       } 
    }
    
            }
            catch (Exception ex) {
                return false;
            }
        // all done
        return true;
  }
    
}
