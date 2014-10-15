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
 * purpose: demonstrate the possibilities Java implemented system exit handlers allow for
 * needs:   testRexxWithSystemdExits.rxj
 *
 * @author  Rony G. Flatscher
 * @since   2012-02-20
 */

/*
    Changed: 2013-06-17, rgf, demonstrate that one should use BSFManager's terminate() method,
                            if that particular Rexx interpreter instance is not needed anymore,
                            such that its reserved system resources can be reclaimed
*/

import org.apache.bsf.*;
import org.rexxla.bsf.engines.rexx.*;


public class JavaRunRexxWith_RXSIO_Exit implements RexxExitHandler
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        RexxEngine rexxEngine=(RexxEngine) mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run
        String rexxCode= "call 'testRexxWith_RXSIO_Exit.rxj'        ;" +
                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        // Configure the RexxEngine
        RexxConfiguration rexxconf=rexxEngine.getRexxConfiguration();
        System.err.println("default rexxconf=["+rexxconf+"]\n");

            // add system exits
        rexxconf.addExitHandler(RexxExitHandler.RXSIO, new JavaRunRexxWith_RXSIO_Exit() );

        System.err.println("edited  rexxconf=["+rexxconf+"]\n===> Java - starting Rexx program ===>");

        // invoke the interpreter and run the Rexx program
        rexxEngine.apply ("JavaRunRexxWithSystemExits.rex", 0, 0, rexxCode, null, null);
        System.err.println("<=== Java - after Rexx program has finished. <===");

        mgr.terminate();    // make sure that the Rexx interpreter instance gets terminated!
    }



        // implementation of a RXSIO exit handler
    public  int     counter=0;      // count # of invocations

    public int handleExit(Object slot, int exitNumber, int subFunction, Object[] parmBlock)
    {
        counter++;
        System.err.print("(Java-side) [RXSIO_exit] exitNumber=["+exitNumber+"] subFunction=["+subFunction+"], counter: "+counter);
        if (subFunction==1)         // RXSIOSAY/1
        {
            System.err.print(", RXSIOSAY=1: ["+parmBlock[0]+"]");
        }
        else if (subFunction==2)    // RXSIOTRC/2
        {
            System.err.print(", RXSIOTRC=2: ["+parmBlock[0]+"]");
        }
        else if (subFunction==3)    // RXSIOTRD/3
        {
            parmBlock[0]="==> Java RXSIOTRD/3-supplied string <==";
            System.err.print(": ["+parmBlock[0]+"]");
        }
        else                        // RXSIODTR/4
        {
                // this needs to be a syntactically correct command
            parmBlock[0]="TRACE OFF -- line comment: RXSIODTR/4" ;  // turn off interactive tracing
                // parmBlock[0]="";  // empty string, meaning: "<enter>"
            System.err.print(": ["+parmBlock[0]+"]");
        }

        System.err.println();

        return RexxExitHandler.RXEXIT_HANDLED;
    }
}

