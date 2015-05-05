# Introduction #

Logging is often the achiles toe of any given application. One needs to write output for users and developers to understand what is happening. Most often than not, user are left with messages that are closer to cryptic than desirable.

Another issue is their ease of use, if a developer understands that a `System.out.println()`} is simpler to use when developing code, this system.out will remain as something that outputs text to the system but cannot be measured, printed to other media or stored for post-analyzis.

Last but not least: Translations. This step is often neglected until an application grows with a significant user base that will request translation of the application to their own language. Most often than not, developers will need to re-factor the code in order to meet this requirement.

# What is proposed #

The idea is to simply replace `System.out.println()` with an object that capable of enhancing our logging features. The advantage from not using `System.out.println()` is that we can also add some useful features that allow to solve the issues identified earlier with the `System.out.println()` method.

### Advantages ###

The `LogMessage()` class allows:
  * Specifying a number that details the type of message that is present
  * Provide translation support without need to re-factor classes
  * Allow output of log messages to other medias (database, web interface, etc)
  * Permit time tracking and measuring between messages
  * Filter displayed messages according to their type
  * Enact triggers to fired when a given log text is present

### Alternatives ###

Why not using an already available logging component?
  * A custom logging solution is custom fit to our needs
  * Licensing clashes could occur



### In resume ###

Using LogMessage it is now possible to control the flow of messages. On the case of Remedium, it allows developers to developers to write components and classes that do not require starting the whole remedium infrastruture to test their code and this is an handy feature to isolated the code under development from external interferences.

Over the next sections we will detail how this class works and how it can be used.