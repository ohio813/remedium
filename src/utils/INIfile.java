/*
 * This class allows managing an INI text file. There exist implementations of
 * INI handling already available but this one is created with special attention
 * to our specific context.
 */

package utils;

import java.io.File;
import java.io.IOException;
import system.log.LogMessage;
import system.msg;

/**
 *
 * @author Nuno Brito, 1st of July 2011 in Darmstadt, Germany.
 */
public class INIfile {

    private File file;
    private String data = "";
    private LogMessage log;


    /** public constructor */
    public INIfile(File assignedFile, LogMessage assignedMessage){
        // preflight checks
        if(assignedFile.exists()==false){
            try {
                // create a new INI if it doesn't exist already
                assignedFile.createNewFile();
            } catch (IOException ex) {
                assignedMessage.add(msg.ERROR, "File '%1' does not exist",
                        assignedFile.getAbsolutePath() );
                return;
            }
        }
        // do the assignments
        file = assignedFile;
        log = assignedMessage;
        // read our file
        data = utils.files.readAsString(file.getAbsolutePath());
        log.add(msg.COMPLETED, "INI file '%1' is ready",
                file.getAbsolutePath());
    }

    /** Write a key to the INI file */
    public boolean write(final String section, final String key,
            final String value){
        // preflight check
        if(file.exists()==false){
            // file doesn't exist, quit here
            log.add(msg.ERROR, "File '%1' does not exist",
                    file.getAbsolutePath());
            return true;
        }

        // check if section exists
        doCreateSectionIfNotExists(section);

        // add the key
        doWriteKey(section, key, value);
        
        // write back to disk
        boolean result =
                utils.files.SaveStringToFile(file, data);

        if(result == false){
            log.add(msg.ERROR, "Failed to write INI key '%1' with value "
                    + "'%2' at section '%3' of file '%4'",
                key,
                value,
                section,
                file.getPath()
                );
        return false;
        }

        log.add(msg.COMPLETED, "Wrote INI key '%1' with value '%2' at"
                + " section '%3' of file '%4'",
                key, value, section, file.getPath()
                );
        return true;
    }


    /** do the actual writing of the key on the data file */
    private void doWriteKey(final String section, final String key,
            final String value){
          // find the section
            String sectionTitle = "[" + section + "]\n";
            int sectionTitleSize = sectionTitle.length();
            // where is this positioned?
            int sectionPositionBegin = data.indexOf(sectionTitle);
            String temp = data.substring(sectionPositionBegin + sectionTitleSize);
            // get the end of section
            int sectionPositionEnd;
            if(temp.contains("\n["))
                 // until the next section is found
                sectionPositionEnd = temp.indexOf("\n[") + sectionTitleSize + 1;
            else // or until the end of file if no more sections exist
                sectionPositionEnd = data.length();

            // get the full text of this section
            String sectionOriginalText =
                    data.substring(sectionPositionBegin, sectionPositionEnd);
          // find the key
            String keyTitle = "\n" + key + "=";
            String sectionModifiedText = sectionOriginalText;

            // has this key been written before?
            if(sectionOriginalText.contains(keyTitle)){
                // do the replacement here
                int keyBegin = // where does our value begin?
                        temp.indexOf(keyTitle //+ keyTitle.length()
                        );
                // get text since the key until the end of string
                String 
                        keyAndValue = temp.substring(keyBegin +1);
                // get the text until the end of the value
                        keyAndValue = keyAndValue.substring
                                (0, keyAndValue.indexOf("\n"));

                // modify the section to use our modified key
                sectionModifiedText= sectionModifiedText.replace(keyAndValue,
                        key + "=" + value);
            }else{
                // add the key to the bottom of our text
                sectionModifiedText = sectionModifiedText
                        .concat(key + "=" + value + "\n");
            }
            // do the replacement
            data = data.replace(sectionOriginalText, sectionModifiedText);
        return;
    }

    /** Creates an INI section if it was not found before*/
    private void doCreateSectionIfNotExists(final String section){
        if(data.contains("[" + section + "]\n"))
            return; // we found the section, no need to continue
        // create a new section
        data = data.concat("[" + section + "]\n");
        log.add(msg.COMPLETED, "Created section '%1'", section);
    }


}

