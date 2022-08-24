/* ------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2012-2022 Rony G. Flatscher
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
             2022-08-05, rgf, use System.exit(0); to end Java program
             2022-08-24, - adjust for having changed RexxHandler to a superclass interface
                           (serving as the root interface class for all Rexx handler types)
                           in 850 and the 461 static methods to interface default methods;
                           this way invoking all default methods becomes simpler (no need
                           to refer explicitly to the class RexxHandler, normal inheritance
                           rules will take place)
                         - all Rexx exit constants (e.g. RXEXIT_HANDLED, RXEXIT_NOT_HANDLED, ...)
                           from RexxHandlerExit are now available via inheritance, hence no
                           need to explicitly refer to the RexxExitHandler class anymore
*/

import org.apache.bsf.*;
import org.rexxla.bsf.engines.rexx.*;


public class JavaRunRexxWith_RXCMD_Exit implements RexxExitHandler
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        RexxEngine rexxEngine=(RexxEngine) mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run
        String rexxCode= "call 'testRexxWith_RXCMD_Exit.rxj'        ;" +
                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        // Configure the RexxEngine
        RexxConfiguration rexxConf=rexxEngine.getRexxConfiguration();
        System.err.println("default rexxConf=["+rexxConf+"]\n");

            // add system exits
        rexxConf.addExitHandler(RXCMD, new JavaRunRexxWith_RXCMD_Exit() );

        System.err.println("edited  rexxConf=["+rexxConf+"]\n===> Java - starting Rexx program ===>");

        // invoke the interpreter and run the Rexx program
        rexxEngine.apply ("JavaRunRexxWithSystemExits.rex", 0, 0, rexxCode, null, null);
        System.err.println("<=== Java - after Rexx program has finished. <===");

        mgr.terminate();    // make sure that the Rexx interpreter instance gets terminated!
        System.exit(0);
    }



        // implementation of a _RXCMD exit handler
    public  int  counter=0;      // count # of invocations,

    public int handleExit(Object slot, int exitNumber, int subFunction, Object[] parmBlock)
    {
        counter++;
        boolean flag[]=(boolean[]) parmBlock[0];    // fetch flag array
        System.err.print("(Java-side) [RXCMD_exit] exitNumber=["+exitNumber+"] subFunction=["+subFunction+"], counter: "+counter);

        String address=(String) parmBlock[1];
        String dllname=(String) parmBlock[2];
        String command=(String) parmBlock[3];

        System.err.print(", address=["+address+"], dllname=["+dllname+"], command=["+command+"]");

        if (subFunction==1)     // RXCMDHST
        {
            System.err.println(", RXCMDHST=1: flag rxfcfail=["+flag[0]+"], flag rxfcerr=["+flag[0]+"]");
            if (((String) getContextVariable(slot,"RGF_JAVA")).compareTo("0")==0) // o.k. addressed to us
            {
                if (((String) getContextVariable(slot,"RGF_FAILURE")).compareTo("1")==0)
                {
                    flag[0]=true;       // set rxfcfail
                }
                else if (((String) getContextVariable(slot,"RGF_ERROR")).compareTo("1")==0)
                {
                    flag[1]=true;       // set rxfcerr
                }
                else
                {
                    parmBlock[4]="<dealt by Java exit handler for address=["+address+"], received command=["+command+"], handler invoked ["+counter+"] time"+(counter==1?"":"s")+" so far.>";
                }
                return RXEXIT_HANDLED;
            }
        }
        System.err.println();

        return RXEXIT_NOT_HANDLED;
    }
}

