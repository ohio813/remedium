/*
 * This class is used to track the performance of certain operations.
 * Developers only need to feed this class with new data and we will do the
 * math to calculate the average performance.
 */

package utils;

/**
 *
 * @author Nuno Brito, 8th of June 2011 in Darmstadt, Germany.
 */
public class AverageTracker {

    private String
            values = ""; // where we'll store our data

    private long
            limit = 10; // how many entries we can store

    /** Public constructor */
    public AverageTracker(){
    }

    /** Add new data onto our machine*/
    public void add(long newValue){

        // remove the oldest value after the limit
        if(values.split(";").length >= limit)
             values = values.substring(values.indexOf(";")+1);

        // add the new string to the bottom of our data sampling
        values = values.concat(newValue+";");
        }

    /** Returns the average value from our sampling */
    public long average(){

        // transform our sampling into an average of all available values
        String[] samples = this.values.split(";");

        long sum = 0;

        for(String sample : samples){
            sum += Long.parseLong(sample);
        }

        long result = 0;

        try{
            result = sum / samples.length;
        }
        catch (Exception e){}
        finally {}
        
        // return the sum of all data divided by the number of elements
        return result;
    }


    
    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getLimit() {
        return limit;
    }

    public String getValues() {
        return values;
    }



}
