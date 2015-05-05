# Introduction #

This class allows to handle INI files. It is tailored to fit our platform with the inclusion of specific techniques to either save time or RAM memory.

It is also not fully tested and stabilized, over the next years all reported defects should eventually be addressed. Developers making use of this class should adopt the conventional structure of INI files since it is also the guideline adopted for this implementation.


---



# Structure #

The next sections of this chapter define how INIfile was structured and how it can be used.

## Constructor ##

Before using a INIfile object, it needs to be constructed (initialized). The construction of this object can be invoked using this example:
```
		ini = new INIfile(file, log);
```

On this example:
  * INIfile `ini` - is the INI object that we can use to write and read keys/values
  * File `file` - is the file object that we will open for our use
  * LogMessage `log` - is where all log messages will be stored during our operations

## Write Key ##

To write a key, one must invoke the method write() of this class. When calling this method it is required to include the `section`, `key` and `value`.

**Conditions**
  * If a `section` does not exist when calling this method, then it is created.
  * If a `key` does not exist when calling this method then it is created, otherwise, the old value is overwriten.

**Example:**
```
		ini.write("test", "Hello", "World");
```

Where:
  * "test" is the section
  * "Hello" is the key
  * "World" is the value



---


# Handling log messages #

During each invoked operation, the `LogMessage` that you provided during the construction of the INIfile object will be used to store messages.

After invoking any operations, the most recent message should **always** output a value equal to `msg.COMPLETED` if it has completed as intended. Typically, operations will output a value of `msg.ERROR` when an error occurs. Using the `LogMessage` that was provided on the constructor, you can backtrack all previous messages that occurred during the execution of an INI operation to see what might have happened wrong.

Here is an example of how you can detect such errors:
```
           ini.write("test", "Hello", "World");
           if(log.getResult()==msg.ERROR){
               System.out.println(log.getRecent());
               return;
           }
```

Calling `log.getResult()` after invoking your INI operation will give us the status result from the last operation. If this result is of type `msg.ERROR`, then we know that something went wrong. We can get the backtrack of messages calling `log.getRecent()` to see what happened wrong.


---


# INI style conventions #

This chapter details the behavior of our INIfile class when interpreting the INI style conventions.

### Duplicate key titles on same section ###

Following the INI style convention, a title given for a `key` can only exist once on each `section`. If more than one `key` with the same title exists, the first `key` will be used for reading and writing values.

### Differences of letter cases ###

When calling operations that read values from the INI file, it is not necessary to match the Upper/Lower cases for **section** and **key** titles.

We only apply strict upper/lower casing as specific by the developer on the **values** that are written.


### Rules about sections ###

  * Don't use `=` inside the section titles
  * When adding an empty section to the end of the file, ensure that it is **always** followed with a blank line. We only recognize the end of section title looking for the combination of `]\n`. If you don't add a blank link on this particular situation then we won't recognize the section and this causes troubles


---


# Progress #

2nd of July 2011
  * Initial implementation and documentation