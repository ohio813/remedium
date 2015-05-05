"remedium" is a cooperative virus defense system that aggregates information from users about files on their disks. This information is processed to expose malicious events activities.

To get an idea of its usefulness, you should soon be able of asking the system about the reliability of a given file, and remedium will provide back a score of trust on that file.

There exist online systems providing a trust functionality such as http://virustotal.com - the main difference is the fact that remedium intends to build trust over files that are benign rather than malicious and it is not centralized.

Every LAN or group can operate their own remedium without need to depend on vendors or third-party providers.





In the meanwhile, it is possible to run two demonstrations of the sentinel application inside remedium:
  * Index all files inside your computer
  * Immunize USB flash drives when inserted in the computer

Below is a screenshot of sentinel indexing files.
![http://img855.imageshack.us/img855/9083/remediumsentinel110622j.png](http://img855.imageshack.us/img855/9083/remediumsentinel110622j.png)

Remedium works across Windows, Linux (tested in Ubuntu) and MacOSX. I am including the .exe file that can be run directly from explorer. For other operative systems you should launch the executable from command line using:
`java -jar remedium.exe`

When launching from command line you get access to the log messages, please use the command line when testing remedium.


On this test you should be able of completing the index process. If some problem is output on the log, please do let us know at http://reboot.pro/14801/

The reboot.pro forum is the place to talk about remedium.


---


The indexing of files allows to create a database of files that are found on your machine. In the future, it is intended that this information can be merged with the information from other workstations on a given network. The idea is to assign a score on files that are considered of trust or not.

After enough information is gathered, we can run metrics on the collected information. For example, if a kernel32.dll file is modified by a malicious process, we should be able of detecting that no similar file from Microsoft existed before and that this be treated as a suspicious event (more details on this algorithm will be explained later).

Thank you for helping!