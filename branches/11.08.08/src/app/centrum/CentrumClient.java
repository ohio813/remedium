/*
 * Hold all the details of a centrum client that connects to this instance.
 */

package app.centrum;

import java.util.Properties;

/**
 *
 * @author Nuno Brito, 17th of May 2011 in Pittsburgh, USA.
 */
public class CentrumClient {

    private String
            url; // where does it come from?
            
    private long
            updated, // when did it last contacted us?
            disk,
            bandwidth,
            uptime,
            cpu,

            // Options
            interval = 10000; // interval to consider a client alive


      /** Export client to a properties object */
    public String doExport(){
        Properties message = new Properties();
        // fill all the fields of a property object with our data
        message.setProperty("url", url);
        message.setProperty("updated", "" + updated);
        message.setProperty("disk", "" + disk);
        message.setProperty("bandwidth", "" + bandwidth);
        message.setProperty("uptime", "" + uptime);
        message.setProperty("cpu", "" + cpu);
        message.setProperty("interval", "" + interval);
        // convert our properties object to a plain string and return as result
        return system.net.protocols.propertiesToString(message);
    }

      /** Export client to a properties object */
    public void doImport(Properties message){
        url = message.getProperty("url", url);
        updated = Long.parseLong(message.getProperty("updated", "0"));
        disk = Long.parseLong(message.getProperty("disk", "0"));
        bandwidth = Long.parseLong(message.getProperty("bandwidth", "0"));
        uptime = Long.parseLong(message.getProperty("uptime", "0"));
        cpu = Long.parseLong(message.getProperty("cpu", "0"));
        interval = Long.parseLong(message.getProperty("interval", ""+interval));
    }


    public long getInterval() {
        return interval;
    }


    /**
     * Is this client still considered well and alive?
     */
    public boolean isAlive() {
        // the sum of the last time that the client data was updated
        // plus the accepted interval between alive call must be
        // bigger than the current time on the system
        return ((updated + interval) > System.currentTimeMillis());
    }

    public long getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(long bandwidth) {
        this.bandwidth = bandwidth;
    }

    public long getCpu() {
        return cpu;
    }

    public void setCpu(long cpu) {
        this.cpu = cpu;
    }

    public long getDisk() {
        return disk;
    }

    public void setDisk(long disk) {
        this.disk = disk;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

           
}
