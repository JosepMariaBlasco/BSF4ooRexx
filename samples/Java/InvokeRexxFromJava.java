import org.apache.bsf.*;   // BSF support
import java.io.*;       // exception handling

/** A Java demonstration program to show, how easy it is to invoke a Rexx program, supply
 *  arguments to it and retrieve a return value.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2001-2009 Rony G. Flatscher
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
 * ----------------------------------------------------------------------------- </pre>
 *
 * Compile the Java program:     javac InvokeRexxFromJava.java
 *                               ... will create a file named "InvokeRexxFromJava.class"
 *
 * Run the compiled program:     java InvokeRexxFromJava
 *                               ... note: file extension ".class" must not be given!
 *
 *
 * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
 * @version 1.2, 2008-06-14, 2009-10-17
 */
public class InvokeRexxFromJava {

  /** Running an in-line defined Rexx script.
   */
  public static void main (String[] args) throws IOException {
    try {
      BSFManager mgr        = new BSFManager ();                // get an instance of BSFManager
      BSFEngine  rexxViaBSF = mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // create Rexx code right here in Java (could be read from file as well!): in this case
        // we are using a String in Java and can use the usual Java string escape characters, if
        // we wish (like "\n" for newline); each Rexx statement is either ended with a semi-colon
        // or implicitly by a newline ("\n") character

        // it will show the number of arguments received from Java and each arguments value in Rexx,
        // finally it will return a string to Java
      String     rexxCode =
                 " parse version v; say v; parse source s; say s; say '---'; say                \n" +
                 " say '  This is Rexx code called from Java!'; say                             \n" +
                 " say '  received [' || arg() || '] arguments:'; say                           ; " +

                 " do  i=1 to arg()                                                             ; " +
                 "    say '  arg #' pp(i)': exists?' pp(arg(i, 'E'))', value:' pp(arg(i))       ; " +
                 " end                                                                          ; " +
                 " return 'Hi Java, this is Rexx saying hello...'  /* to be returned to Java */ ; " +

                 " pp: procedure    /* \"pretty print \": enclose argument in square brackets */\n" +
                 "     return '[' || arg(1) || ']'                                                "
                 ;

        // define arbitrary arguments

      Object     a = new Object();      // a pure Java object
      String  [] b = {"eins", "zwei" }; // an array of String

      java.util.Vector vArgs = new java.util.Vector();  // Vector for arguments to be passed to Rexx

      for (int i=0; i<args.length; i++)   // add argument to vector
      {
          vArgs.addElement(args[i]);
      }

        // changed add() to addElement() for JRE 1.1.8, ---rgf, 2001-05-23
      vArgs.addElement( new String("an argument of type String"));     // arg # 1
      vArgs.addElement( new Integer(17) );                             // arg # 2
      vArgs.addElement( new Float(17.71f));                            // arg # 3
      vArgs.addElement( Float.class);                                  // arg # 4
      vArgs.addElement( a );                                           // arg # 5
            // create the String representation 'manually'
      vArgs.addElement( "'a': "+a.getClass().getName()+'@'+
                        Integer.toHexString(a.hashCode()));            // arg # 6

      vArgs.addElement(null);   // indicate: no argument (= omitted)   // arg # 7

      vArgs.addElement(b);                                             // arg # 8
         // create the String representation 'manually'
      vArgs.addElement( "'b': "+b.getClass().getName()+'@'+
                        Integer.toHexString(b.hashCode()));            // arg # 9

        // invoke Rexx and retrieve its return value
      // Object obj = rexxViaBSF.apply (null, 0, 0, rexxCode, null, vArgs);
      Object obj = rexxViaBSF.apply ("InvokeFromJava.java", 0, 0, rexxCode, null, vArgs);

      System.err.println("\t---\nThis is what Rexx returned: [" + obj + "]");
    }
    catch (BSFException e)
    {
      e.printStackTrace();
    }
    System.exit(0);             // stop Java
  }
}

