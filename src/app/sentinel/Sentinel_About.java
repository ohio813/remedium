/*
 * Provide the text for the About page of the Sentinel component
 */

package app.sentinel;

/**
 *
 * @author Nuno Brito, 24th of April 2011 in Darmstadt in Germany.
 */
public class Sentinel_About {

    /** Displays the "About" text for the Sentinel role.
     * This page is available under ./docs/html_pages*/
    public static String getAboutText(){
           return
"<h1>What is a Sentinel?</h1>"
+"This role is "
+"responsible for enforcing the local security of the machine where it "
+"runs, inside the context of a castrum or clan and will "
+"interact with a machine in the role of triumvir that has been assigned "
+"to bridge his contact with the other elements in the network. <br>"
+"<br>"
+"The Sentinel is also equipped with recovery mechanisms that allow him to "
+"reconnect to the network in case of this triumvir connection fails.<br>"
+"<br>"
+"<h2>Sentinel functioning</h2>"
+"This section details how a cliens application in the role of Sentinel is "
+"intended to behave and interact with other cliens applications on his "
+"network.<br>"
+"<h3>Responsibilities:</h3>"
+"<ul>"
+"  <li>Verify the reliability of trusted files on disk against the "
+"knowledge on database</li>"
+"  <li>Flag potentially dangerous files:</li>"
+"  <ul>"
+"    <li>User of machine can manually report a file as dangerous</li>"
+"  </ul>"
+"  <ul>"
+"    <li>Knowledge database can expose a given file as dangerous</li>"
+"  </ul>"
+"  <li>Monitor the introduction of new files on the machine (USB, "
+"Internet, shared folders and critical folders)</li>"
+"  <li>Contribute with knowledge to the network about the files on "
+"his disk</li>"
+"  <li>React to files detected as malicious</li>"
+"  <li>Apply quarantine</li>"
+"  <li>Replace files with trusted versions from other cliens if "
+"possible</li>"
+"  <li>Prevents machine from being used until administrator takes "
+"action</li>"
+"  <li>Assume the role of triumvir au pair with the Sentinel role "
+"when necessary</li>"
+"</ul>"
+"<br>"
+"Each of these topics are detailed on the sub-sections below.<br>"
+"<br>"
+"<h3>Verify the reliability of trusted files on disk against the "
+"knowledge on database</h3>"
+"One of the most important tasks for our system is the detection of "
+"malicious activity affecting the files on disk. We do not compete "
+"against anti virus products already installed on a given machine; our "
+"verification is performed with focus on files that we verify against a "
+"knowledge database to check if they match or not. For this task to "
+"succeed, there is the need to catalogue all files that are found inside "
+"the machine and use a database to store information about each of them:<br>"
+"<br>"
+"<ul>"
+"  <li>Compute SHA1, SHA-2, CRC, MD5 checksums</li>"
+"  <li>Store the file name</li>"
+"  <li>Store the file version (only at the moment for win32 "
+"executables: exe, ocx, dll, sys, drv, scr)</li>"
+"  <li>File size (in bytes)</li>"
+"  <li>File creation date (if applicable)</li>"
+"  <li>Security classification: trusted, malicious, suspicious, "
+"cautious or ignored;</li>"
+"  <li>Additional comments made by users of the system</li>"
+"  <li>Date added to database</li>"
+"  <li>User name and clan or castrum of who added the file;</li>"
+"</ul>"
+"<br>"
+"The database is kept on the same machine where the cliens application "
+"is running; the knowledge is updated from a triumvir and also built "
+"from the profiling of file activity of disk activity inside the "
+"machine. Please look on the Triumvir \"About\" page to read more about "
+"this role.<br>"
+"<br>"
+"<h3>Flag potentially dangerous files</h3>"
+"<span style=\"font-weight: bold;\">User of machine can "
+"manually report a file as suspicious or malicious</span><br>"
+"If a user suspects that a given file on his disk is potentially "
+"dangerous (email attachment, a file that appears on a public share and "
+"so forth), it is possible for users to flag these files and pass along "
+"this information to the rest of the network.<br>"
+"<br>"
+"<span style=\"font-weight: bold;\">Knowledge database can"
+"expose a given file as malicious</span><br>"
+"Even though the primary objective of this system is to verify the "
+"trustworthiness of files on disk, it is also possible to keep in the "
+"database a list of files that are identified as malicious. The details "
+"of this detection are specified over the following paragraph.<br>"
+"&nbsp;<br>"
+"The logical process for indexing new files on the database does not "
+"include the task of detecting anomalies since it is part of the "
+"Quaestor tasks, but we provide here a resumed explanation of this "
+"specific action:<br>"
+"<br>"
+"We start with a simple loop through all the files found inside a given "
+"folder. If the file extension is recognized as an executable file (we "
+"are only covering a few selected extensions at the moment), then we "
+"proceed to verify if it is on the group of files that has a PE "
+"(Portable Executable) header from where we can read the file version "
+"(.exe .dll .scr .ocx .drv).<br>"
+"<br>"
+"There are cases where a file with PE header does not bear a version. On "
+"these cases, we treat them as plain files. The next step is obtaining "
+"the checksum. If the checksum is not recognized, it is added on the DB, "
+"otherwise we first check if it is on the list of files to ignore along "
+"with the version information (if available).<br>"
+"<br>"
+"In case the file is not on the “ignore” list and the checksum does not "
+"match our records, an anomaly has been detected and the alarm sounds. <br>"
+"<br>"
+"<br>"
+"<h3>Monitor the introduction of new files on the machine </h3>"
+"<br>"
+"<span style=\"font-weight: bold;\">USB</span><br>"
+"A Sentinel can detect whenever a removable drive is inserted on the system "
+"and proceed with scanning and cleaning procedures as necessary. We "
+"include not only USB drives but also any drive that may be introduced "
+"on the system while running such as memory cards and firewire drives.<br>"
+"<br>"
+"<span style=\"font-weight: bold;\">Internet</span><br>"
+"Specific folders inside the system may be monitored constantly for the "
+"appearance of new files arriving from the Internet, folders such as the "
+"Desktop and Downloads are default locations where most people and "
+"browsers and users place their downloaded files.<br>"
+"<br>"
+"<span style=\"font-weight: bold;\">Shared/Critical folders</span><br>"
+"When working at an intranet environment, it is common to share a given "
+"folder with other coworkers in the same team or department. Often is "
+"the case when an infected machine will place a new binary with a "
+"suggestive name at shared folders, eluding other users to infect their "
+"machines when running the binary.<br>"
+"<br>"
+"Certain critical folders is OS such as the Desktop, Documents and "
+"Download folders in MS Windows should also be monitored for changes as "
+"they are often the preferred location for malware to host their "
+"malicious code.<br>"
+"<br>"
+"<span style=\"font-weight: bold;\">Contribute with knowledge "
+"to the network about the files on his disk</span><br>"
+"Just as important as monitoring the files present on disk is to gather "
+"additional knowledge than can be shared with the network. For example, "
+"identifying an attack to a given file on disk will trigger additional "
+"attention on other cliens applications about the file was affected.<br>"
+"<br>"
+"If more cliens are reported as being under attack, the network "
+"administrator will be better informed about this threat and have an "
+"opportunity to react accordingly.<br>"
+"<br>"
+"&nbsp;<br>"
+"<h3>React to files detected as malicious</h3>"
+"<span style=\"font-weight: bold;\">Apply quarantine</span><br>"
+"When a file is flagged as prejudicial to the machine, it won’t be "
+"erased from disk but rather placed in quarantine where it may be "
+"submitted in a later time to third party anti-virus that may assess if "
+"it is a threat or not. This feature might also contribute to improve "
+"the third party antivirus knowledge database about malicious files.<br>"
+"&nbsp;<br>"
+"<span style=\"font-weight: bold;\">Replace files with "
+"trusted versions from other cliens if possible</span><br>"
+"The network administrator might also enable the cliens application to "
+"replace the malicious file with a version that is considered correct "
+"from another cliens application. <br>"
+"<br>"
+"<span style=\"font-weight: bold;\">Prevents machine from "
+"being used until administrator takes action</span><br>"
+"Under an extreme scenario of security, whenever anomalies are detected "
+"- the cliens application can prevent any further participation of the "
+"machine in the network either by disabling the network interfaces or "
+"forcing the machine shutdown until an administrator reverts this "
+"condition.<br>"
+"<br>"
+"This feature disrupts the normal operation of users at the affected "
+"machine but protects the network of attacks originating from this "
+"location. <br>"
+"<br>"
+"<span style=\"font-weight: bold;\">Assume the role of "
+"triumvir au pair with the Sentinel role when necessary</span><br>"
+"Each cliens application is enabled to perform the role of triumvir when "
+"elected from a pool of active Sentinel inside the network.<br>"
+"Since each machine will differ in terms of physical characteristics to "
+"perform this role, a score is computed by the cliens application and "
+"also assigned a score according to its presence on the network. This "
+"score is detailed at the “Cliens score” section.<br>"
+"<br>"
+"<br>"
+"<h2>How do we detect anomalies?</h2>"
+"On a given set of machines, knowledge will be gathered about the files "
+"that are found on the disk of each cliens. This knowledge is passed "
+"onto the respective triumvir to where the cliens is connected and this "
+"knowledge is eventually propagated across the other triumvirs.<br> "
+"<br>"
+"At some point, we will have gathered a significative group of data from "
+"all the members of our network and this knowledge is also built over "
+"time. This means that we can not only base our assumptions of anomaly "
+"on a statistical number of data but also analyze this data when "
+"compared to historical values.<br>"
+"<br>"
+"The Sentinel is not directly responsible for flagging anomalies, this is a "
+"task delivered to Quaestor. On the \"About\" page of Quaestor, exists a "
+"more detailed description of this activity.<br>"
+"<br>"
+"<br>";
    }

}
