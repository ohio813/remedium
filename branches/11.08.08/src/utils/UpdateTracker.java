/*
 * This class helps to track time elapsed between recurring events.
 */

package utils;

/**
 *
 * @author Nuno Brito, 5th of May 2011 in Darmstadt, Germany.
 */
public class UpdateTracker {

    private long
            secondsBetweenAction = 60, // periodicity to grab updates
            lastUpdated = 0; // when were we last updated?

    private final TimeTracker
            timeTracker;




    /** public constructor */
    public UpdateTracker(TimeTracker assignedTimeTracker){
        // pre flight checks
        if(assignedTimeTracker == null)
            // create a new one
            timeTracker = new TimeTracker();
        else
        // get the designated component
        timeTracker = assignedTimeTracker;
    }


    public long getSecondsBetweenAction() {
        return secondsBetweenAction;
    }

    public void setSecondsBetweenAction(long secondsBetweenAction) {
        this.secondsBetweenAction = secondsBetweenAction;
    }

    /** Are we allowed to proceed? */
    public boolean isAllowed(){

        if( // restrict the update request to a given amount of seconds
            (
              (lastUpdated + (secondsBetweenAction * 1000))
              > timeTracker.getTime()
              )
                 &&
                (lastUpdated > 0)
                 )// not time yet, just quit here
            return false;

        lastUpdated = timeTracker.getTime();
        return true;
    }
}
