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
 * @since   2012-02-09
 */

import org.apache.bsf.*;
import org.rexxla.bsf.engines.rexx.*;
import java.util.GregorianCalendar;


public class JavaRunRexxWithSystemExits
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        RexxEngine rexxEngine=(RexxEngine) mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run
        String rexxCode= "call 'testRexxWithSystemExits.rxj'        ;" +
                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        // Configure the RexxEngine
        RexxConfiguration rexxconf=rexxEngine.getRexxConfiguration();
        System.err.println("default rexxconf=["+rexxconf+"]\n");

            // add system exits
        rexxconf.addExitHandler(RexxExitHandler.RXNOVAL, new RXNOVAL_exit() );

            // add ",.pre" to default call extensions
        rexxconf.setExternalCallExtensions(rexxconf.getExternalCallExtensions()+",.pre");

        rexxconf.setExternalCallPath("..");

        System.err.println("edited  rexxconf=["+rexxconf+"]\n");

        // invoke the interpreter and run the Rexx program
        rexxEngine.apply ("JavaRunRexxWithSystemExits.java", 0, 0, rexxCode, null, null);
    }



    public static class RXNOVAL_exit extends RexxExitHandler
    {
        private boolean bHandle=false;  // switch that causes handling every even invocation
        private int     counter=0;      // count # of invocations,

        public int handleExit(int exitNumber, int subFunction, Object[] parmBlock, Object[] slot)
        {
            counter++;
            System.err.print("(Java-side) [RXNOVAL_exit] exitNumber=["+exitNumber+"] subFunction=["+subFunction+"]"
                          + " variable_name=["+parmBlock[0]+"], invoked ["+counter+"] time");
            if (counter>1)  // add plural ?
            {
                System.err.print("s");
            }

            if (bHandle)
            {
                bHandle=false;  // next time do not handle exit
                parmBlock[1]="<Value supplied by Java system exit (RXNOVAL) handler>";   // assign new value
                System.err.println(": this time handling exit!");
                return RexxExitHandler.RXEXIT_HANDLED;
            }

            bHandle=true;       // next time handle the exit
            System.err.println();
            return RexxExitHandler.RXEXIT_NOT_HANDLED;
        }
    }

}

