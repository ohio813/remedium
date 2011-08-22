/*
 * This class allows managing an INI text file. There exist implementations of
 * INI handling already available but this one is created with special attention
 * to our specific context.
 *
 * Available commands:
 *  - Write INI value
 *  - Read INI value
 *  - Write a line to INI section
 *  - Read line from INI section
 *  - Read all lines from a section
 */

package utils;

import java.io.File;
import java.io.IOException;
import system.log.LogMessage;
import system.mqueue.msg;

/**
 *
 * @author Nuno Brito, 1st of July 2011 in Darmstadt, Germany.
 */
public class INIfile {

    private File file;
    private String 
            data = "", // where the contents of our INI file are stored
            dataUpperCase = "", // upper case version,
            who = "INI";

    private LogMessage logger;


    /** public constructor */
    public INIfile(File assignedFile, LogMessage result){
        // preflight checks
        if(assignedFile.exists()==false){
            try {
                // create a new INI if it doesn't exist already
                assignedFile.createNewFile();
            } catch (IOException ex) {
                result.add(who, msg.ERROR, "File '%1' does not exist",
                        assignedFile.getAbsolutePath() );
                return;
            }
        }
        // do the assignments
        file = assignedFile;
        logger = new LogMessage();
        // read our file
        data = utils.files.readAsString(file);
        dataUpperCase = data.toUpperCase();
        result.add(who,msg.COMPLETED, "INI file '%1' is ready",
                file.getAbsolutePath());
    }

    public boolean sectionExists(final String section){
    // define how a section would look inside our file
        String sectionTitle = "[" + section.toUpperCase() + "]\n";
        // does this section exists?
        boolean sectionExists = dataUpperCase.contains(sectionTitle);
        return sectionExists;
    }

    /** Write a key to the INI file */
    public boolean write(final String section, final String key,
            final String value){
        // preflight check
        if(file.exists()==false){
            // file doesn't exist, quit here
            log(msg.ERROR, "File '%1' does not exist",
                    file.getAbsolutePath());
            return true;
        }

        // check if section exists
        createSection(section);

        // add the key
        doWriteKey(section, key, value);
        
        // write back to disk
        boolean result =
                utils.files.SaveStringToFile(file, data);

        if(result == false){
            log(msg.ERROR, "Failed to write INI key '%1' with value "
                    + "'%2' at section '%3' of file '%4'",
                key, value, section, file.getPath() );
        return false;
        }

        log(msg.COMPLETED, "Wrote INI key '%1' with value '%2' at"
                + " section '%3' of file '%4'",
                key, value, section, file.getPath()
                );
        return true;
    }

    /* Returns all the text inside a given section. This version includes
     the section header. */
    private String getSectionFullText(final String section){
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

            String result = "";
            try{
                result = data.substring(sectionPositionBegin, sectionPositionEnd);
            }catch (Exception e){
                log(msg.ERROR, "getSectionFullText: Exception: ",
                        e.toString());
            }

            if(logger.getResult() == msg.ERROR)
                System.out.println();

            // return the full text of this section
        return result;
    }

    /** Return all lines inside a given section. The section header is not
     included.*/
    public String readSectionLines(final String section){
        // get the text from the mentioned section
        String result = getSectionFullText(section);
        // remove the section header, example: [myTitle]
        result = result.substring(result.indexOf("]\n")+2);
        // return our lines
        return result;
    }

    /** Write a line to a given section of this INI file. If the section
     does not exist then it will be created */
    public boolean writeSectionLine(final String section, final String line){
        // create section even if it doesn't exist
        createSection(section);

        // get the section contens
        String sectionOriginal = getSectionFullText(section);
        // add our line to the bottom of the section
        String sectionModified = sectionOriginal + line + "\n";

            // do the replacement
            data = data.replace(sectionOriginal, sectionModified);
            dataUpperCase = data.toUpperCase();

        // write back to disk
        boolean result =
                utils.files.SaveStringToFile(file, data);

        if(result == false){
            log(msg.ERROR, "Failed to write INI section '%1' with line "
                    + "'%2' at file '%4'",
                section, line, file.getPath() );
        return false;
        }

            log(msg.COMPLETED, "Wrote in INI section '%1' the line "
                    + "'%2' at file '%4'",
                section, line, file.getPath() );
        return true;
    }


    /** do the actual writing of the key on the data file */
    private void doWriteKey(final String section, final String key,
            final String value){
            // get the full text of this section
            String sectionOriginalText =
                    getSectionFullText(section);

            // find the key
            String keyTitle = "\n" + key + "=";
            String sectionModifiedText = sectionOriginalText;

            // has this key been written before?
            if(sectionOriginalText.contains(keyTitle)){
                // where does our value begin?
                int keyBegin = sectionOriginalText.indexOf(keyTitle);
                // get text since the key until the end of string
                String  keyAndValue = sectionOriginalText
                        .substring(keyBegin +1);
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
            dataUpperCase = data.toUpperCase();
        return;
    }

    /** Creates an INI section if it was not found before*/
    public void createSection(final String section){
        if(sectionExists(section))
            return; // we found the section, no need to continue
        // create a new section
        data = data.concat("[" + section + "]\n");
        dataUpperCase = data.toUpperCase();
        // output the success message
        log(msg.COMPLETED, "Created section '%1'", section);
    }


    /** Read a INI key from a given file.*/
    public String read(final String section, final String key){
        return read(section, key, "");
    }

     /** Read a key from the INI file, provide a fall back value in case
      the mentioned key does not exist.*/
    public String read(final String section, final String key,
            final String fallbackValue){
        // preflight checks
        if(sectionExists(section) == false){ // does this section exists?
            log(msg.COMPLETED, "INI read: Section '%1' does not exist,"
                    + "providing the fall back value.", section);
            return fallbackValue;
        }

        // read the whole section
        String sectionText = getSectionFullText(section);
        // we need to convert text to uppercase to compare regardless of case
        String sectionTextUpperCase = sectionText.toUpperCase();
        // define the key
        String keyTitle = "\n" + key.toUpperCase() + "=";
        // is this key available?
        if(sectionTextUpperCase.contains(keyTitle)==false){
            // not available, use the fall back value and exit
            return fallbackValue;
        }

        // get the position where our key begins inside this section
        int keyBegin = sectionTextUpperCase.indexOf(keyTitle);
        // get text since the key until the end of this section
        String  keyAndValue = sectionText.substring(keyBegin +1);
        // get the text since the key until the end of our value
        keyAndValue = keyAndValue.substring(0, keyAndValue.indexOf("\n"));
        // grab the value
        String value = keyAndValue.substring(keyAndValue.indexOf("=")+1);
        // return this value
        log(msg.COMPLETED, "Read INI key '%1' with value '%2'", key, value);
        return value;
    }


    /** Delete a section and respective contents */
    public boolean deleteSection(final String section){
        // preflight checks
        if(this.sectionExists(section)== false){
            log(msg.COMPLETED, "deleteSection: Section %1 does not exist"
                    + " inside file '%2'", section, file.getPath());
            return true;
        }
        // get the whole text of this section
        String sectionToDelete = this.getSectionFullText(section);

        // do the cleansing
            data = data.replace(sectionToDelete, "");
            dataUpperCase = data.toUpperCase();

        // write back to disk
        boolean result =
                utils.files.SaveStringToFile(file, data);
        // output the result message
        log(msg.COMPLETED, "deleteSection: Section %1 was deleted from "
                    + "file '%2'", section, file.getPath());
        // all done
        return result;
    }

    /** Get the logger message */
    public LogMessage getLog() {
        return logger;
    }

/** central logger for this class */
    private void log(final int gender, final String message,
            final String... args){
        // log a new message
        logger.add(who, gender, message, args);
    }
}

