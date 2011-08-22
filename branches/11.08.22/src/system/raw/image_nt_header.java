package system.raw;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 *
 * @author Nuno Brito, 26th of May 2011 in Germany
 */
public class image_nt_header {

   // settings
   private Boolean
      debug = false;

    

   // variables
   private Boolean
      hasVersion = false; // placeholder for text version

   // objects
   private String
      Signature;

   private image_file_header
      FileHeader;

   private image_optional_header
      OptionalHeader;

   private image_resource_directory_table
      RootResourceDirectory = new image_resource_directory_table(),
      VersionResourceDirectory = new image_resource_directory_table(),
      VersionDataResourceDirectory = new image_resource_directory_table();

   private ArrayList<image_resource_directory_table>
      TypeResourceDirectory = new ArrayList<image_resource_directory_table>();
      //ObjectResourceDirectory

   private ArrayList<image_section_header>
      SectionHeader = new ArrayList<image_section_header>();

   private version_node
      version = new version_node();

   private BinaryFile
              bin;
   
   private RandomAccessFile
              file;

   private Long
           e_lfanew_offset;

   private String
          fileVersion,
          fileLanguage,
          fileArchitecture;


   /**
    * Reads a given number of sections reported inside this header
    */
   private void readSectionHeaders(){

       long offset = 0; // our work offset

       try {
            // We need to jump the PE header pointer onto the relevant sections
            file.seek(e_lfanew_offset 
                    + FileHeader.getSizeOfOptionalHeader()+24);


            // set the offset to mark the beginning of the section read
            offset = file.getFilePointer();

             } catch (IOException ex) {
            log("ERROR","ReadSections operation failed: " + ex.toString());
        }
            // read the available sections
            // the number of sections is reported on FileHeader.NumberOfSections
            int n = FileHeader.getNumberOfSections();

             for (int i = 0; i < n; i++) {
                image_section_header temp = new image_section_header();
                Boolean result = temp.Read(file, offset+(40*i));

                if(result==false){
                    log("ERROR", "Failed to read section #"+i);
                    return;
                }
               SectionHeader.add(temp);
       }

   }



  /**
   * After we get a pointer about where each resource is located, it is
   * is time to explore each one of them.
   */
 private void exploreSectionContent(RandomAccessFile filename, String title){
            // Typically we can expect sections to be named like:
            // .text .data .rsrc or reloc
            // for our case, we need to find .rsrc since it is the place where
            // the file version is kept

            // iterate all sections
            for(image_section_header section : SectionHeader){
                // we are only interested in the one named '.rsrc'
              if(section.getName().contains(".rsrc")){
               // do our processing here
                   RootResourceDirectory.read(filename, title,
                                            section.getPointerToRawData());
                   // If we were successfull and entries exist, explore them
                  exploreResources(RootResourceDirectory, section, filename
                          ,title );
              }
              if(this.hasVersion){
                  break;}
            }


}


/** When given a resource, explore all the entries within */
 private void exploreResources(image_resource_directory_table resource,
                                image_section_header section,
                                RandomAccessFile filename, String title){
                    // explore all resources
                    int n = resource.getNumberOfIdEntries()
                            + resource.getNumberOfNamedEntries();

                    if(n==0){
                        log("INFO","ExploreResources, we found no entries to"
                                + " explore");
                        return;
                    }

                    
                    for (int x = 0; x < n; x++){

                        image_resource_directory_table temp
                            = new image_resource_directory_table();

                        temp.read(filename, title,
                                section.getPointerToRawData()
                                + resource.getEntry(x).getOffsetToDirectory()
                                );

                        // add this resource to our list
                        TypeResourceDirectory.add(temp);

                        // if this is a resource of type VERSION, read it
                        readIfVersionAvailable(resource.getEntry(x), 
                                                section,
                                                filename, title);
                        
                    }// for
}


/** 
 * When processing a given entry, if it is of type
 * VERSION, then extract the details that we want
 */
private void readIfVersionAvailable(image_resource_directory_entry entry,
                                    image_section_header section,
                                    RandomAccessFile filename, String title){
                    // on the first run, ID means the type of resource
                    // and we are looking specifically for 0x10 type that is version
                    if (entry.getID()!=16)
                        return;

                    if(debug)
                        log("EXTRA","Found a VERSION resource");

                    // Explore all the possible versions inside this data_directory
                    long r = entry.getOffsetToDirectory()
                                + section.getPointerToRawData();

                    if(!VersionResourceDirectory.read(filename, title, r)
                          ) return;

                        // explore the VERSION entries that might differ in languages
                        // we don't care about language, just grab the first one
                    int k = VersionResourceDirectory.getNumberOfIdEntries()
                          + VersionResourceDirectory.getNumberOfNamedEntries();
                   
                    if (k==0)
                        return;

                     // at this moment ID means the ID number
                     // Note that VersionDataResourceDirectory.entry[0].flag = 0;
                     // since the flag is 0, it means we are really pointing to data.
                     long t =
                        VersionResourceDirectory
                        .getEntry(0)
                        .getOffsetToDirectory()
                        + section.getPointerToRawData();

                     if(!VersionDataResourceDirectory.read(filename, title, t)
                              ) return;


        // at this moment, each ID entry on VersionDataResourceDirectory
        // represents the available language
        //VersionDataResourceDirectory.entry[0].debug();

        // next jump is to get RVA_DATA
        long y = VersionDataResourceDirectory
                        .getEntry(0)
                        .getOffsetToDirectory()
                        + section.getPointerToRawData();

         resource_data_entry versionData = new resource_data_entry();

         if(!versionData.read(filename, y))
                            return;

        //TODO Warning: Convert RVA to real offset - What happens VA < PointerToRawData?
        // Does this code really works for all DLL's?
        long versionDataOffset = versionData.getDataRVA()
                     - (section.getVirtualAddress()
                     -  section.getPointerToRawData());

        // RawOffset = VA – VirtualOffset da seção – ImageBase + RawOffset da seção

       //Now we read the data inside the version node
      if(!version.read(filename, versionDataOffset))
          return;
        
        
      // If we have a version, write and mark all the values as needed
      if (version.getVersionText().length()>0){
                        
          // mark as affirmative that we can export data
          hasVersion = true;
          
          // write the version details that will be sent outside
          fileVersion = version.getVersionText();

          // if there is a language for this file, report it here
          fileLanguage = "0x" +java.lang.Long.toHexString
                  (VersionDataResourceDirectory.getEntry(0).getID());

          // CPU architecture where the binary is supposed to run
          fileArchitecture = "0x"+java.lang.Long.toHexString(
                  OptionalHeader.getMagic()
                  ).toUpperCase();


          if(debug) // output some information
             log("INFO",version.getVersionText());
      }
    }



  /**
   * This method reads the NT fields of a given executable file.
   * @return True if it has suceeded in reading the file
   */
  public Boolean read ( RandomAccessFile filename, String title,
       image_dos_header DOS_header)
       {
       // pre flight check
         if(DOS_header.getLFAnew()<= 0){
             log("ERROR","Read operation failed, value for LFAnew is not valid");
             return false;
         }

      // initial statement
      hasVersion = false;

      e_lfanew_offset = DOS_header.getLFAnew(); // get the data offset
      file =  filename; // get our file object

      try {
      file.seek(e_lfanew_offset); // seek offset position for NT header
      // set the binary reader
      bin = new BinaryFile(file);
            	bin.setSigned(false);
            	bin.setEndian(BinaryFile.LITTLE_ENDIAN);

            Signature = bin.readFixedString(2); // read the signature

           }catch (Exception e){
                log("ERROR","Read operation failed, unable of "
                        + "reading signature");
            return false;
           }

            if (!Signature.equals("PE")){
                log("ERROR","Read operation failed, signature does not match "
                        + "'PE'");
                return false;
            }

            // read the file header
            FileHeader = new image_file_header(); 
            FileHeader.read(filename, e_lfanew_offset);

            // read the "optional" header (optional is a misleading name)
            OptionalHeader = new image_optional_header();
            OptionalHeader.read(filename, e_lfanew_offset);

            // read the headers of each section
            readSectionHeaders();
            // visit each section
            exploreSectionContent(filename, title);

        return true;
       }

  /** Returns the CPU Architecture of the Windows file */
    public String getFileArchitecture() {
        return fileArchitecture;
    }

    /** Returns the native language to which the Windows file is targeted */
    public String getFileLanguage() {
        return fileLanguage;
    }

    /** Returns the version of the Windows file */
    public String getFileVersion() {
        return fileVersion;
    }

    /** If we have found the version details of this file, report them here */
    public Boolean hasVersion() {
        return hasVersion;
    }






     /** output log messages in a standard manner */
    private void log(String gender, String message){
        if(debug)
            System.out.println("[NT_header]["+gender+"] "+message);
 }

}

