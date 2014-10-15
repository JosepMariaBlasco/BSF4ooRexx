/* ------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2012-2013 Rony G. Flatscher
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
 * purpose: demonstrate the possibilities Java command handlers allows for
 *
 * Changed:    2013-06-13, demonstrate that one should use BSFManager's terminate() method,
 *                         if that particular Rexx interpreter instance is not needed anymore,
 *                         such that its reserved system resources can be reclaimed
 *
 * needs:   testRexxWithCommandExits.rxj
 * @author  Rony G. Flatscher
 * @since   2012-02-21
 */

import org.apache.bsf.*;
import org.rexxla.bsf.engines.rexx.*;
import java.util.GregorianCalendar;


public class JavaRunRexxWithCommandExits
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        RexxEngine rexxEngine=(RexxEngine) mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run
        String rexxCode= "call 'testRexxWithCommandExits.rxj'       ;" +
                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        // Configure the RexxEngine
        RexxConfiguration rexxconf=rexxEngine.getRexxConfiguration();
        System.err.println("default rexxconf=["+rexxconf+"]\n");
        rexxconf.addCommandHandler("ALICE",          new JavaAliceCommandHandler());
        rexxconf.addCommandHandler("DANIEL",         new JavaDanielCommandHandler());
        rexxconf.addCommandHandler("MICHAEL",        new JavaMichaelCommandHandler());
        rexxconf.addCommandHandler("RONY",           new JavaRonyCommandHandler());

        rexxconf.addCommandHandler("GREGORIAN",      new JavaReturnGregorianCalendarCommandHandler());



        rexxconf.addCommandHandler("TESTCONDITION",  new JavaTestConditionCommandHandler());

        rexxconf.addCommandHandler("TESTEXCEPTION",  new JavaTestExceptionCommandHandler());
        rexxconf.addCommandHandler("TESTEXCEPTION0", new JavaTestException0CommandHandler());
        rexxconf.addCommandHandler("TESTEXCEPTION1", new JavaTestException1CommandHandler());
        rexxconf.addCommandHandler("TESTEXCEPTION2", new JavaTestException2CommandHandler());

        rexxconf.setInitialAddressEnvironment("ALICE");

        System.err.println("edited  rexxconf=["+rexxconf+"]\n");

        // invoke the interpreter and run the Rexx program
        rexxEngine.apply ("JavaRunRexxWithCommandExits.rex", 0, 0, rexxCode, null, null);
        mgr.terminate();    // make sure that the Rexx interpreter instance gets terminated!
    }


    public static class JavaAliceCommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[JavaAliceCommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]");
            return "118";
        }
    }

    public static class JavaDanielCommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[JavaDanielCommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]");
            return "521";
        }
    }

    public static class JavaMichaelCommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[JavaMichaelCommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]");
            return "922";
        }
    }

    public static class JavaRonyCommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[JavaRonyCommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]");
            return "520";
        }
    }

    public static class JavaReturnGregorianCalendarCommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[JavaReturnGregorianCalendarCommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]");
            return new java.util.GregorianCalendar();
        }
    }


    public static class JavaTestConditionCommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[JavaTestConditionCommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]"+
                                 "\nraising a 'Failure' condition before returning to Rexx");

            GregorianCalendar gregCal=new GregorianCalendar();
            Object additional[]={"additional #1", "additional #2", gregCal, "additional #4"};
            RexxHandler.raiseCondition(slot, "Failure", "description: test", additional, "43 -nixi, noxi");
            return null;
        }
    }


    public static class JavaTestExceptionCommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[JavaTestExceptionCommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]"+
                                 "\nraising a syntax exception (# 98.945) before returning to Rexx");

            GregorianCalendar gregCal=new GregorianCalendar();
            Object substitutions[]={"substitution #1", "substitution #2", gregCal, "substitution #4"};
            RexxHandler.raiseException(slot, 98945, substitutions); // some dummy Rexx error number ("98.945")
            return null;
        }
    }

    public static class JavaTestException0CommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[JavaTestException0CommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]"+
                                 "\nraising a syntax exception (# 98.945) before returning to Rexx");

            RexxHandler.raiseException0(slot, 98945);   // some dummy Rexx error number ("98.945")
            return null;
        }
    }

    public static class JavaTestException1CommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[JavaTestException1CommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]"+
                                 "\nraising a syntax exception (# 98.945) before returning to Rexx");

            RexxHandler.raiseException1(slot, 98945, "substitution #1");   // some dummy Rexx error number ("98.945")
            return null;
        }
    }

    public static class JavaTestException2CommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[JavaTestException2CommandHandler] address=["+address+"]"+
                                     "\tcommand=["+command+"]");

            System.err.println("\t(JAVA-side) fetching Rexx context variable 'TEST', which should NOT be set: ["
                               + RexxHandler.getContextVariable(slot, "TEST")
                               + "]");

            RexxHandler.setContextVariable(slot, "TEST", "This is a value from the Java command handler!");

            System.err.println("\t(JAVA-side) fetching Rexx context variable 'TEST', which now SHOULD be set: ["
                               + RexxHandler.getContextVariable(slot, "TEST")
                               + "]");

            System.err.println("\nraising a syntax exception (# 98.945) before returning to Rexx");

            RexxHandler.raiseException2(slot, 98945, "substitution #1", "substitution #2");   // some dummy Rexx error number ("98.945")
            return null;
        }
    }

}

