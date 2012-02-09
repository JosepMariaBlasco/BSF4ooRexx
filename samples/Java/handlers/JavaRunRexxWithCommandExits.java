/* ------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2012 Rony G. Flatscher
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
 * purpose: demonstrate the possibilities Java command handlers allows for
 * needs:   testRexxWithCommandExits.rxj
 *
 * @author  Rony G. Flatscher
 * @since   2012-02-09
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

        rexxconf.addCommandHandler("TESTCONDITION",  new JavaTestConditionCommandHandler());

        rexxconf.addCommandHandler("TESTEXCEPTION",  new JavaTestExceptionCommandHandler());
        rexxconf.addCommandHandler("TESTEXCEPTION0", new JavaTestException0CommandHandler());
        rexxconf.addCommandHandler("TESTEXCEPTION1", new JavaTestException1CommandHandler());
        rexxconf.addCommandHandler("TESTEXCEPTION2", new JavaTestException2CommandHandler());

        rexxconf.setInitialAddressEnvironment("ALICE");

            // add ";*.pre" to default call extensions
        rexxconf.setExternalCallExtensions(rexxconf.getExternalCallExtensions()+",.pre");

        rexxconf.setExternalCallPath("..");

        System.err.println("edited  rexxconf=["+rexxconf+"]\n");

        // invoke the interpreter and run the Rexx program
        rexxEngine.apply ("JavaRunRexxWithCommandExits.java", 0, 0, rexxCode, null, null);
    }


    public static class JavaAliceCommandHandler extends RexxCommandHandler
    {
        public Object handleCommand(String address, String command, Object[] slot)
        {
            System.err.println("[JavaAliceCommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]");
            return "118";
        }
    }

    public static class JavaDanielCommandHandler extends RexxCommandHandler
    {
        public Object handleCommand(String address, String command, Object[] slot)
        {
            System.err.println("[JavaDanielCommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]");
            return "521";
        }
    }

    public static class JavaMichaelCommandHandler extends RexxCommandHandler
    {
        public Object handleCommand(String address, String command, Object[] slot)
        {
            System.err.println("[JavaMichaelCommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]");
            return "922";
        }
    }

    public static class JavaRonyCommandHandler extends RexxCommandHandler
    {
        public Object handleCommand(String address, String command, Object[] slot)
        {
            System.err.println("[JavaRonyCommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]");
            return "520";
        }
    }


    public static class JavaTestConditionCommandHandler extends RexxCommandHandler
    {
        public Object handleCommand(String address, String command, Object[] slot)
        {
            System.err.println("[JavaTestConditionCommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]"+
                                 "\nraising a 'Failure' condition before returning to Rexx");

            GregorianCalendar gregCal=new GregorianCalendar();
            Object additional[]={"additional #1", "additional #2", gregCal, "additional #4"};
            raiseCondition(slot, "Failure", "description: test", additional, "43 -nixi, noxi");
            return null;
        }
    }


    public static class JavaTestExceptionCommandHandler extends RexxCommandHandler
    {
        public Object handleCommand(String address, String command, Object[] slot)
        {
            System.err.println("[JavaTestExceptionCommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]"+
                                 "\nraising a syntax exception (# 98.945) before returning to Rexx");

            GregorianCalendar gregCal=new GregorianCalendar();
            Object substitutions[]={"substitution #1", "substitution #2", gregCal, "substitution #4"};
            raiseException(slot, 98945, substitutions); // some dummy Rexx error number ("98.945")
            return null;
        }
    }

    public static class JavaTestException0CommandHandler extends RexxCommandHandler
    {
        public Object handleCommand(String address, String command, Object[] slot)
        {
            System.err.println("[JavaTestException0CommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]"+
                                 "\nraising a syntax exception (# 98.945) before returning to Rexx");

            raiseException0(slot, 98945);   // some dummy Rexx error number ("98.945")
            return null;
        }
    }

    public static class JavaTestException1CommandHandler extends RexxCommandHandler
    {
        public Object handleCommand(String address, String command, Object[] slot)
        {
            System.err.println("[JavaTestException1CommandHandler] address=["+address+"]"+
                                 "\tcommand=["+command+"]"+
                                 "\nraising a syntax exception (# 98.945) before returning to Rexx");

            raiseException1(slot, 98945, "substitution #1");   // some dummy Rexx error number ("98.945")
            return null;
        }
    }

    public static class JavaTestException2CommandHandler extends RexxCommandHandler
    {
        public Object handleCommand(String address, String command, Object[] slot)
        {
            System.err.println("[JavaTestException2CommandHandler] address=["+address+"]"+
                                     "\tcommand=["+command+"]");

            System.err.println("\t(JAVA-side) fetching Rexx context variable 'TEST', which should NOT be set: ["
                               + getContextVariable(slot, "TEST")
                               + "]");

            setContextVariable(slot, "TEST", "This is a value from the Java command handler!");

            System.err.println("\t(JAVA-side) fetching Rexx context variable 'TEST', which now SHOULD be set: ["
                               + getContextVariable(slot, "TEST")
                               + "]");

            System.err.println("\nraising a syntax exception (# 98.945) before returning to Rexx");

            raiseException2(slot, 98945, "substitution #1", "substitution #2");   // some dummy Rexx error number ("98.945")
            return null;
        }
    }

}

