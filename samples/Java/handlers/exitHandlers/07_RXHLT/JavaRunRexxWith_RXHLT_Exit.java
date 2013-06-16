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


public class JavaRunRexxWith_RXHLT_Exit implements RexxExitHandler
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        RexxEngine rexxEngine=(RexxEngine) mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run
        String rexxCode= "call 'testRexxWith_RXHLT_Exit.rxj'        ;" +
                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        // Configure the RexxEngine
        RexxConfiguration rexxConf=rexxEngine.getRexxConfiguration();
        System.err.println("default rexxConf=["+rexxConf+"]\n");

            // add system exits
        rexxConf.addExitHandler(RexxExitHandler.RXHLT, new JavaRunRexxWith_RXHLT_Exit() );

        System.err.println("edited  rexxConf=["+rexxConf+"]\n===> Java - starting Rexx program ===>");

        // invoke the interpreter and run the Rexx program
        try
        {
            rexxEngine.apply ("JavaRunRexxWithSystemExits.rex", 0, 0, rexxCode, null, null);
            System.err.println("<=== Java - after Rexx program has finished. <===");
        }
        finally
        {
            mgr.terminate();    // make sure that the Rexx interpreter instance gets terminated!
            System.err.println("<=== finally: terminated Rexx interpreter instance with the help of the manager.");
        }
    }



        // implementation of a RXHLT exit handler
    public  int  counter=0;      // count # of invocations,
    public  int  clrCounter=0;

    public int handleExit(Object slot, int exitNumber, int subFunction, Object[] parmBlock)
    {
        counter++;
        boolean flag[]=(boolean[]) parmBlock[0];    // fetch flag array
        System.err.print("(Java-side) [RXHLT_exit] exitNumber=["+exitNumber+"] subFunction=["+subFunction+"], counter: "+counter);

        System.err.print(" - 'haltVar'=["+RexxHandler.getContextVariable(slot, "haltVar")+"]"+
                           ", clearVar=["+RexxHandler.getContextVariable(slot, "clearVar")+"]");

        if (subFunction==1)
        {
            System.err.print(", RXHLTCLR=1: flag[0]=["+flag[0]+"]");
            clrCounter++;
            String clearVar=(String) RexxHandler.getContextVariable(slot, "clearVar");
            if ( clearVar.compareTo("1")==0 )
            {
                flag[0]=true;
                System.err.println(" -> now set to=["+flag[0]+"]: \"Clears the HALT condition?\"");
                return RexxExitHandler.RXEXIT_HANDLED;
            }
        }

        else if (subFunction==2)
        {
            System.err.print(", RXHLTTST=2: flag[0]=["+flag[0]+"]");
            String haltVar=(String) RexxHandler.getContextVariable(slot, "haltVar");
            if ( haltVar.compareTo("1")==0 )
            {
                flag[0]=true;
                System.err.println(" -> now set to=["+flag[0]+"]: \"Raises the HALT condition?\"");
                RexxHandler.dropContextVariable(slot, "haltVar");

                return RexxExitHandler.RXEXIT_HANDLED;
            }
        }
        System.err.println();

        return RexxExitHandler.RXEXIT_NOT_HANDLED;
    }
}

