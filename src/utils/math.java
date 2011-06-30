package utils;

import java.lang.reflect.Array;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author nuno
 */
public class math {



    public static double round1(double value) {
    return Math.round(value * 10.0) / 10.0;
  }

    public static int RandomInteger(int aStart, int aEnd){
    Random aRandom = new Random();
    if ( aStart > aEnd ) {
      throw new IllegalArgumentException("Start cannot exceed End.");
    }
    //get the range, casting to long to avoid overflow problems
    long range = (long)aEnd - (long)aStart + 1;
    // compute a fraction of the range, 0 <= frac < range
    long fraction = (long)(range * aRandom.nextDouble());
    int randomNumber =  (int)(fraction + aStart);
    return randomNumber;
  }



    


     /**
     * Gets a string value from laptop characteristics based on a given pattern.
     * A Matcher object is used internally.
     *
     * @param source string containing the text to be parsed
     * @param reg regular expression pattern to use
     * @param group index of one of the groups found by the pattern
     * @return String containing the found pattern, or null otherwise
     */
    public static String findRegEx(String source, String reg, int group) {
        String out = null;

        Pattern p = Pattern.compile(reg); // Prepare the search pattern.
        Matcher matcher = p.matcher(source); // Retrieve our items.

        if (matcher.find()) {
            try {
                out = matcher.group(group);
            } catch (Exception e) {}
        }

        return out;
    }


 /** Convert an array of strings to one string.
  *  Put the 'separator' string between each element.
  */
public static String arrayToString(String[] a, String separator) {
    StringBuilder result = new StringBuilder();
    if (a.length > 0) {
        result.append(a[0]);
        for (int i=1; i<a.length; i++) {
            result.append(separator);
            result.append(a[i]);
        }
    }
    return result.toString();
}

public static <T> T[] arrayMerge(T[]... arrays)
{
    // Determine required size of new array

    int count = 0;
    for (T[] array : arrays)
    {
        count += array.length;
    }

    // create new array of required class

    T[] mergedArray = (T[]) Array.newInstance(
       arrays[0][0].getClass(),count);

    // Merge each array into new array

    int start = 0;
    for (T[] array : arrays)
    {
        System.arraycopy(array, 0,
           mergedArray, start, array.length);
        start += array.length;
    }
    return (T[]) mergedArray;
}

}
