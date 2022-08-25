/* ------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2022 Rony G. Flatscher
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * -----------------------------------------------------------------------------
 *
 *
 * purpose: demonstrate some possibilities of definining mulitple Rexx command handlers
            in Java (eleven different ones!), some could be used as redirecting
            Rexx command handlers, though this capability is not demonstrated in here

 * @author  Rony G. Flatscher
 * @since   2022-08-24
 */

import org.apache.bsf.*;
import org.rexxla.bsf.engines.rexx.*;
import java.util.GregorianCalendar;


public class DirectCommandHandlers
{
    /** A command handler that always returns the string &quot;118&quot;. */
    public static class AliceCommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[AliceCommandHandler] address=["+address+"] "+
                                 "command=["+command+"]\n\t -> info=["+toString(slot,address)+"]");
            return "118";
        }
    }

    /** A command handler that always returns the string &quot;521&quot;. */
    public static class DanielCommandHandler implements RexxRedirectingCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[DanielCommandHandler] address=["+address+"] "+
                                 "command=["+command+"]\n\t -> info=["+toString(slot,address)+"]");
            return "521";
        }
    }

    /** A command handler that always returns the string &quot;922&quot;. */
    public static class MichaelCommandHandler implements RexxRedirectingCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[MichaelCommandHandler] address=["+address+"] "+
                                 "command=["+command+"]\n\t -> info=["+toString(slot,address)+"]");
            return "922";
        }
    }

    /** A command handler that always returns the string &quot;520&quot;. */
    public static class RonyCommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[RonyCommandHandler] address=["+address+"] "+
                                 "command=["+command+"]\n\t -> info=["+toString(slot,address)+"]");
            return "520";
        }
    }

    /** A command handler that always creates and returns a Java <code>GregorianCalendar</code>
     *  instance.
    */
    public static class ReturnGregorianCalendarCommandHandler implements RexxRedirectingCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[ReturnGregorianCalendarCommandHandler] address=["+address+"] "+
                                 "command=["+command+"]\n\t -> info=["+toString(slot,address)+"]");
            return new java.util.GregorianCalendar();
        }
    }


    /** A command handler that creates a Java <code>GregorianCalendar</code> but then
     *  raises a Rexx <code>failure</code> condition making
     *  the created <code>GregorianCalendar</code> object available in the
     *  <code>additional</code> array entry of the Rexx condition object.
     *  Note: according to <code>rxapi.pdf</code> (as of 2022-08-24 cf.
     *        &quot;Example 1.56. API — Command handler interface&quot;)
     *        a 'FAILURE' condition is serious indicating that a serious
     *        failure in the command handler was diagnosed.
    */
    public static class TestFailureConditionCommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[TestFailureConditionCommandHandler] address=["+address+"] "+
                                 "command=["+command+"]\n\t -> info=["+toString(slot,address)+"]"+
                                 "\n--> raising intentionally a 'Failure' condition before returning to Rexx ...\n");

            GregorianCalendar gregCal=new GregorianCalendar();
            Object additional[]={"additional #1", "additional #2", gregCal, "additional #4"};
                            //   name:      cause (command):  additional:   rc-value:
            raiseCondition(slot, "Failure", command,          additional,   "43 -nixi, noxi");
            return null;
        }
    }

    /** A command handler that creates a Java <code>GregorianCalendar</code> but then
     *  raises a Rexx <code>failure</code> condition making
     *  the created <code>GregorianCalendar</code> object available in the
     *  <code>additional</code> array entry of the Rexx condition object.
     *  Note: according to <code>rxapi.pdf</code> (as of 2022-08-24 cf.
     *        &quot;Example 1.56. API — Command handler interface&quot;)
     *        an 'ERROR' condition is indicating that a not so serious
     *        condition in the command handler was diagnosed.
    */
    public static class TestErrorConditionCommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[TestErrorConditionCommandHandler] address=["+address+"] "+
                                 "command=["+command+"]\n\t -> info=["+toString(slot,address)+"]"+
                                 "\n--> raising intentionally an 'Error' condition before returning to Rexx ...\n");

                            //   name:      cause (command):  additional:   rc-value:
            raiseCondition(slot, "Error"  , command,          null,         "-43");
            return null;
        }
    }


    /** A command handler that creates a Java <code>GregorianCalendar</code> but then
     *  raises a Rexx <code>exception</code> (some &quot;execution error 98.945&quot;) making
     *  the created <code>GregorianCalendar</code> object available in the
     *  <code>additional</code> array entry of the Rexx condition object.
    */
    public static class TestExceptionCommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[TestExceptionCommandHandler] address=["+address+"] "+
                                 "command=["+command+"]\n\t -> info=["+toString(slot,address)+"]"+
                                 "\n--> raising intentionally a syntax exception (# 98.945) before returning to Rexx ...\n");

            GregorianCalendar gregCal=new GregorianCalendar();
            Object substitutions[]={"substitution #1", "substitution #2", gregCal, "substitution #4"};
            raiseException(slot, 98945, substitutions); // some dummy Rexx error number ("98.945")
            return null;
        }
    }

    /** A command handler that raises a Rexx <code>exception</code> (some &quot;execution
     *  error 98.945&quot;) without any arguments for the error condition object.
    */
    public static class TestException0CommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[TestException0CommandHandler] address=["+address+"] "+
                                 "command=["+command+"]\n\t -> info=["+toString(slot,address)+"]"+
                                 "\n--> raising intentionally a syntax exception (# 98.945) before returning to Rexx ...\n");

            raiseException0(slot, 98945);   // some dummy Rexx error number ("98.945")
            return null;
        }
    }

    /** A command handler that raises a Rexx <code>exception</code> (some &quot;execution
     *  error 98.945&quot;) with a single argument for the error condition object.
    */
    public static class TestException1CommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[TestException1CommandHandler] address=["+address+"] "+
                                 "command=["+command+"]\n\t -> info=["+toString(slot,address)+"]"+
                                 "\n--> raising intentionally a syntax exception (# 98.945) before returning to Rexx ...\n");

            raiseException1(slot, 98945, "substitution #1");   // some dummy Rexx error number ("98.945")
            return null;
        }
    }

    /** A command handler that raises a Rexx <code>exception</code> (some &quot;execution
     *  error 98.945&quot;) with two arguments for the error condition object.
    */
    public static class TestException2CommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[TestException2CommandHandler] address=["+address+"] "+
                                 "command=["+command+"]\n\t -> info=["+toString(slot,address)+"]");

            System.err.println("\t(JAVA-side) fetching Rexx context variable 'TEST', which should NOT be set: ["
                               + getContextVariable(slot, "TEST")
                               + "]");

            setContextVariable(slot, "TEST", "This is a value from the Java command handler!");

            System.err.println("\t(JAVA-side) fetching Rexx context variable 'TEST', which now SHOULD be set: ["
                               + getContextVariable(slot, "TEST")
                               + "]");

            System.err.println("\n--> raising intentionally a syntax exception (# 98.945) before returning to Rexx ...\n");

            raiseException2(slot, 98945, "substitution #1", "substitution #2");   // some dummy Rexx error number ("98.945")
            return null;
        }
    }

}

