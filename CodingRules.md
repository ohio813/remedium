# Introduction #

This page gives provides some highlights about good practices when developing code. They are not intended to be strict nor intensively documented, but they are intended for developers to consider when contributing code.


---


### Size of Methods ###

Keep them short, up to 50 lines of code with comments included. If you create methods bigger than 50 lines then you'll notice that the code becomes very difficult for yourself and others to modify or debug.

To break the line count size, create smaller methods that are kept private inside the class and help to break down the bigger method onto a better state of understanding.


### Comments on source code ###

Remember to write comments, plenty of them.
You may understand perfectly what you are coding, remember that other people reading your code will not be so fortunate. For my own codings, I typically add one comment per statement.

At the beginning it might feel that you're just commenting about something that is "obvious", after a few days or weeks when you look again at the code then you will understand their value to help remember what each logical step is intended to do.

**Bottom line**: Write comments often and plenty

### Write test cases ###

For each working class there should exist a test case class, testing each method present on the working class.

This is important. It allows you to verify that your class or modifications are working as intended or not.

Better yet, test cases allow you to verify that new versions are keeping compatibility with the older versions of your code. This is really important to ensure that classes and people can continue to work together.

  * Don't create or modify code without ensuring that the test case works
  * If it doesn't work after your modification, don't change the test case. Debug what is happening and reflect about it

If a test case for a working class is missing, don't panic! You can create one yourself and we'll use it for future testings. When creating a new test case, follow the same pattern used by test cases already available.

Some will look more complete than others and that is normal since they will be changing over time and we also improve them over time, just remember to keep your new test case informative and documented.


### Keep it simple ###

A good developer always looks for the simplest solutions possible. And when this is not possible, he will implement complex solution to see it running and then work to re-write or re-think his approach until a simple solution is made possible.

It is a real bother when you write cryptic code that nobody else will understand. You might feel happy and get a sense of achievement for writing something complicated, for the rest of us that just means that your code will have to be replaced/rewritten/corrected sooner **than** later.

Keep it simple. We appreciate simple designs.


### Intuitive is intuitive ###

Yep. Keeping names intuitive will really go a long to keep them "intuitive":
  * Avoid at all costs create methods with numbers or single letters on the title (bad example: RequireFileQ - what the heck is a "Q")
  * Keep titles as simple as possible, meaningful under the context where they will be used
  * Avoid creating hundreds of similar methods with tiny differences. Where possible, remember that "less is more" and reduce the number of methods so that other developers know exactly where to find what they need. In the long term, remember that it is also "easier" to manage less than more



### Be happy ###

General advices and facts to preserve your overall **happiness**:

  * Everyone makes mistakes
  * Coding skills improve over time with enough practice
  * Nobody knows everything
  * Make up your own mind
  * Help others
  * We are only humans
  * Be nice to other developers


> 
---


> # Conclusion #

> This concludes our tips for coding. There exist no bad programmers as far as I'm concerned, what exists most times is the lack of information or awareness of how these small tips can really go a long way to keep software simple and acessible to many other programmers in the future.

> Don't code just for yourself, remember that many other people are looking at the same code. You also appreciate when the code looks neat, simple and organized. We'd surely appreciate that you help us keeping it that way.

