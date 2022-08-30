package org.rexxla.bsf.engines.rexx;

/* Java 8 introduced default methods for interface classes but the implementation
*  would not allow java.lang.Proxy objects that declare to implement interface
*  classes to use default methods. Defining an abstract class declaring to implement
*  such an interface but leave the non-default methods abstract will allow to
*  invoke default methods without runtime error.
*
*  There is a problem using Java Proxy for default methods (as of 2022-08-23):
*  <https://stackoverflow.com/questions/26206614/java8-dynamic-proxy-and-default-methods>.
*  (For allowing Rexx direct and redirecting command handlers to be implementable in Rexx
*  with BSF4ooRexx the solution will be to define abstract classes for each kind to be used
*  instead of the respecitve interface classes.)
*/

/** Make it easy for Rexx programs to implement direct command handlers in Rexx. :)
 *
 * @since 850 (2022-08-30)
*/
public abstract class AbstractExitHandler implements RexxExitHandler
{
    public static void main (String args[])
    {
        System.out.println("hello, world, this is the \"AbstractExitHandler\" class");
    }
        // this method needs to be implemented by Rexx
    abstract public int handleExit(Object slot, int exitNumber, int subFunction, Object[] parmBlock);
}
