/*
 * Some handy routines to help on everyday tasks
 */

package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import remedium.Remedium;

/**
 *
 * @author Nuno Brito, 20th of March 2011 at Germany
 */
public class time {

    /**
     * Pauses the current thread for a while
     * @param time_to_wait in seconds
     */
    public static void wait(int time_to_wait){

    try { 
        
        Thread.sleep(time_to_wait * 1000);
    }
    catch (InterruptedException ex) {
            }

    }


    /** Holds execution until a speficic message appears on the log */
    public static void waitFor(Remedium instance,
            String who, String message){

        Boolean keepWaiting = true;
        while(keepWaiting){
            wait(1);
            keepWaiting = !instance.logContains(who, message);
        }
    }


//  public static String getElapsedText(long elapsedMillis) {
//    if(elapsedMillis < 60000) {
//      double unit = utils.math.round1(elapsedMillis / 1000.0);
//      return unit + (unit == 1 ? " second" : " seconds");
//    }
//    else if(elapsedMillis < 60000 * 60) {
//      double unit = utils.math.round1(elapsedMillis / 60000.0);
//      return unit + (unit == 1 ? " minute" : " minutes");
//    }
//    else if(elapsedMillis < 60000 * 60 * 24) {
//      double unit = utils.math.round1(elapsedMillis / (60000.0 * 60));
//      return unit + (unit == 1 ? " hour" : " hours");
//    }
//    else {
//      double unit = utils.math.round1(elapsedMillis / (60000.0 * 60 * 24));
//      return unit + (unit == 1 ? " day" : " days");
//    }
//  }


    public static String timeNumberToHumanReadable(long ms){

        String time;
        long x;
        long seconds;
        long minutes;
        long hours;
        long days;


        seconds = ms / 1000;
        minutes = seconds / 60;
        seconds %= 60;
        hours = minutes / 60;
        minutes %= 60;
        days = hours / 24;
        hours %= 24;

        time = "";

        if(days >0) time=days
                +(days == 1 ? " day" : " days")+", ";
        if(hours >0) time=time+hours
                +(hours == 1 ? " hour" : " hours")+", ";
        if(minutes >0) time=time+minutes
                +(minutes == 1 ? " minute" : " minutes")+" and ";
        time=time+seconds
                +(seconds == 1 ? " second" : " seconds");

        return time;
                }


    /** get the current time in a human readable manner */
   public static String getDateTime() {
       // code adapted from http://goo.gl/rZ716
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
        }
    /** get the current time in a human readable manner */

   public static String getTimeFromLong(long time) {
       // code adapted from http://goo.gl/rZ716
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //Date date = new Date();
        return dateFormat.format(time);
        }



   /** get the current time in a human readable manner */
   public static String getCurrentYear() {
       // code adapted from http://goo.gl/rZ716
        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        Date date = new Date();
        return dateFormat.format(date);
        }


}
