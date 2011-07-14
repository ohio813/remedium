/*
 * Provide an alternative to the System.currentTimeMillis() method that
 * makes the CPU go crazy.
 *
 * The trick is to create a single thread that is only updated with a margin
 * of a few seconds. This way, instead of having hundreds of calls per second
 * to the currentTimeMillis methods, we provide a static value.
 *
 * This results in speedier processing and reduced CPU overhead, good to save
 * battery and CPU noise on laptops.
 */

package utils;

/**
 *
 * @author Nuno Brito, 17th of May 2011 in Pittsburgh, USA.
 */
public class TimeTracker {

    private TimeThread watchDog = new TimeThread(this);

    /** Public constructor */
    public TimeTracker(){
        watchDog.start();
    }

    protected long
            currentTime,
            interval = 1; // number of seconds between updates
    
    /** Provides the current time in milliseconds */
    public long getTime(){
        return currentTime;
    }


    

}


class TimeThread extends Thread{

    private TimeTracker track;

    /** Constructor for this method */
    TimeThread(TimeTracker assignedTrack){
        track = assignedTrack;
    }

    @Override
    public void run() {
        //System.out.println("Starting the watchdog");
        while(true){
            try {
                track.currentTime = System.currentTimeMillis();

                sleep(track.interval * 1000);

            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
