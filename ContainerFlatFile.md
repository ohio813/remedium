# Introduction #

A flat file container is a remedium class that is used for storing and retrieving data using flat files. It is necessary since the current HSQL implementation of the container is using an excessive amount of RAM.


There exist trade-offs about this approach:
  * When the file name becomes long then it is unreadable by humans
  * File contents might changes and differ from what is reported on the file name
  * This structure is limited to a single level of key-value pairs. Typical INI methods allow using INI sections
  * Some Operative Systems such as Windows will limit the max size of the file name to 256 characters
  * Malicious actors can tamper the file name or file contents
  * Additional time required to process records
  * Not a mature technique, will be prune to errors

And advantages:
  * Third-party providers can supply additional knowledge bases
  * End users can manage available knowledge by their own means
  * Reduced RAM usage
  * Expand supported storage to large scale management on normal workstations

The main driver to adopt flat files is the need to find a solution that avoids an excessive RAM consumption.


---


# Desired properties #

  * Allows to set the max number of records per data file
  * Least amount possible of data files
  * Quick to retrieve data
    * Should read a record from a DB with 100 000 records under an average of 0,05 seconds after 100 random record reads
  * Quick to store data
    * Should write onto a DB 100 000 records under 10 minutes
  * Scale up to millions of records
    * Should write a DB with 100 000 000 records and manage them
  * Memory efficient
    * Overall memory usage shouldn't surpass 30Mb regardless of DB size


---


# Nice to haves #

We can attempt to store a max of a million of records for each file, however, it would be particularly nice to allow other providers adding more data on a folder without our direct intervention.

For example, we create a knowledge container with information gathered by someone about Microsoft Windows executables and we include this knowledge inside our own container in a decentralized manner.

The big advantage is allowing providers to make available specialized knowledge about a given subject.

People interested in updating the knowledge of their containers can do so manually. Despite primitive, it does bring the advantage of letting end-users manage updates and get them from other providers of their preference.


For this model of data exchange be possible, we can't enforce a specific folder structure but rather scan all files and subfolders to discover the type of information that each container is providing.




---


# Development approach #

I have recently implemented the Container Dump class that is basically dumping all records from an HSQL database onto flat files. The remarkable speed noted when importing and exporting these records are the main reason why the present class is being implemented.

Some of the code and lessons learned from the implementation of Container dump can now be applied to this new class. The page section "Desired properties" is a guideline of performance requirements for my development machine (Toshiba R-630 laptop, i5 CPU x64 with 4Gb of RAM under Windows 7).

A test case is being developed along with this class implementation to ensure that each method and step of this process is tested using a repetitive and measurable approach.

### Identifying files with knowledge ###

Since under this model all files will be mixed and use a unknown folder structure, we are left with the challenge of identifying the contents of a given knowledge file. We need to do this task without reading the file contents, otherwise this method becomes prohibitive to use when these knowledge files are numerous and large sized.

To identify each file, we apply the concept of file name properties that was introduced with the Container Dump class. We add all our properties (key and value) to the file name. When scanning all files inside a given folder and subfolder, we also get information about the file contents and save resources (time and CPU effort).

File name information uses Key=Value approach as seen for INI files with no INI sections. There are two reserved characters:
  * `-` is equivalent to "="
  * `_` separates key-value pairs from each others

An example of this usage:
`db-crc32_since-1309513994973_until-1309514033984_count-2307_checksum-6970388375f06a91f9b8f133697f94f2a1e0ed54ec1d64cebfb47f43595712bd_v-1.txt`

Note how we see different key-value pairs that allow extracting information from the file without needing to read the contents.


### Types of data files ###

On this section we detail the types of data files available and how they are distinguished from each other.

There exist the following types of data files:
  * **Index files** - File name starts with the text `index` and the extension ends in `.txt`, an example is `index-crc32.txt`. The identifier on the example is `crc32` and only exists one index file per identifier on the root folder where containers are stored
  * **Knowledge files** - File name starts with `db` and extension ends in `.txt`. The file name will describe the contents of the file along with other specific properties, such as defining if the file is read-only or read-write and so forth.


### File name parameters ###

This section describes the parameters that can be included on each file. Some parameters take precendence over others due to the limitation of 256 characters per file name.

List of supported parameters:

  * **v** - Version number of file format. This property is used to provide retro-compatibility as multiple formats surface over time
  * **rank** - Defines the importance rank of the knowledge inside this file. This is used for defining the reputation of information. When reading a record of information, it will first look on the knowledge files with higher rank. The accepted values range from 9 down to 1
  * **count** - Provides the number of records inside this knowledge file
  * **since** - Informs about the date of the most recent record inside the knowledge file (value is defined on the second column of each record)
  * **until** - Same as `since` with the difference that informs about the most recent record available
  * **checksum** - A check sum computation of the file contents using SHA2


---


# How it works #

We created an Interface class to the Container and this way it is possible to create implementations of the container using technologies in the future other than just HSQL or Flat files.

Over the next sections we detail the relevant execution steps for this class, intending to serve as a guideline for implementation and later serve as documentation for the class itself to be understood by other developers.

### Glossary ###

This section provides a quick overlook of the meaning and context of terms used over this chapter.

  * **target folder** - the folder where the containers are stored
  * **container ID** - unique ID text that identifies the type of knowledge (example: crc32, sha1, ...)
  * **knowledge file** - flat file where data records are stored
  * **index file** - a file placed at the root of the target folder that contains the location and resources provided by each of knowledge file that was found
  * **index key** - The column on the data record that is considered as most relevant. For example, the unique check-sum signature of each file

### Steps of initialization ###

Details the logical execution steps that occur when a container is initialized. We define initialized as the initial kickstart that occurs both when you run the container for the first time or recurring times in the future.

In parenthesis are the methods employed for each step at the ContainerFlatFile class.

Method initialization()
  * Verify that target folder is valid and available for operations (checkFolder())
  * Find knowledge files inside the target folder, crawl subfolders (findKnowledgeFiles())
  * Get the knowledge files that match our container ID (findKnowledgeFiles())
  * Sort these files according to their importance level. Higher number = higher importance = first to be processed (sortKnowledgeFiles())
    * If no importance level is provided, use 1 as default (lowest score) (sortKnowledgeFiles())
  * Create an index file for our container ID if one does not exist (createIndexFile())
  * We save time if there have been no content changes since the last start, first we check if there have been changes on a superficial level
    * Check if the knowledge file is mentioned on the index file
      * In case the knowledge file is mentioned, verify details such as check-sum of the file and date of last modification to check if any changes occurred
      * If nothing changed, no need to read the knowledge file
      * Otherwise, proceed with reading of knowledge
  * For each knowledge file, evaluate the following properties:
    * File name describes this file as read-only or read-write?
    * Compute current check-sum of knowledge file (check())
    * Get absolute file path and date of last modification
    * Get the number of records present in file
    * Verify if the number of columns on each record matches our expected size (check())
  * Write the properties about each knowledge file on the index file, according to their order of importance
  * From the processed knowledge files, create a list of files that will accept new records to be added
    * If no knowledge files are available to add new records, create a new knowledge file and mark it as available
  * Done!


### Steps for writing a record ###

  * Verify if the write request is valid
    * Number of fields should match the size our container fields
    * The index key should not be a blank value
  * Get a list of the available read-write knowledge files
  * Check records in available knowledge files to find any record that uses the mentioned index key
    * If no record uses the same key
      * Ask for a knowledge file where the new record can be written
    * If a record inside a read/write available knowledge file uses the same key
      * Overwrite the old record with the new record

### Steps for reading a record ###

  * Assume that the first column always holds the index key
    * Two methods are provided to read records
      * Get all records matching a given key
      * Get the first record matching a given key
  * Iterate through the knowledge files of our container for the record(s) that matches the search criteria
  * Provide the requested results (if any)

## Constructor ##

The first step is invoking the constructor method:
`container = new ContainerFlatFile(id, fields, rootFolder, result);`

Where:
  * String **id** is the identification name of this container
  * String[.md](.md) **fields** are the columns for our table
  * File **rootFolder** is the folder where we are storing the containers
  * LogMessage **result** is a log message object that contains details about this operation


### Initial checks ###

We first care to ensure that our **root folder** is valid and then we will check if we can create a sub folder with the same name as **id**.

If anything goes wrong, the **result** message will provide details and a specific value number under `result.getCode()` that you can use for debugging what has happened.

At this moment, there are no checks to verify if there exists enough disk space for storing data records in a reasonable manner (at least 200Mb available).



---



# Progress #

30th of June 2011
  * Initial implementation of ContainerFlatfile class
  * Introduced the class LogMessage object to allow testing the new class without tying it on a remedium component (allows testing exclusively the new class without interference from components)
1st of July 2011
  * Initial documentation and progress added on google code
2nd of July 2011
  * Introduced the class INIfile to handle index files that help managing the knowledge files