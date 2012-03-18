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
 * @since   2012-02-21
 */

import org.apache.bsf.*;
import org.rexxla.bsf.engines.rexx.*;

public class JavaRunRexxWith_RXFNC_Exit implements RexxExitHandler
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        RexxEngine rexxEngine=(RexxEngine) mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run
        String rexxCode= "call 'testRexxWith_RXFNC_Exit.rxj'        ;" +
                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        // Configure the RexxEngine
        RexxConfiguration rexxConf=rexxEngine.getRexxConfiguration();
        System.err.println("default rexxConf=["+rexxConf+"]\n");

            // add system exits
        rexxConf.addExitHandler(RexxExitHandler.RXFNC, new JavaRunRexxWith_RXFNC_Exit() );

        System.err.println("edited  rexxConf=["+rexxConf+"]\n===> Java - starting Rexx program ===>");

        // invoke the interpreter and run the Rexx program
        rexxEngine.apply ("JavaRunRexxWithSystemExits.rex", 0, 0, rexxCode, null, null);
        rexxEngine.terminate();
        System.err.println("<=== Java - after Rexx program has finished. <===");
    }



        // implementation of a _RXFNC exit handler
    public  int  counter=0;      // count # of invocations,

    public int handleExit(Object slot, int exitNumber, int subFunction, Object[] parmBlock)
    {
        counter++;
        System.err.print("(Java-side) [RXFNC_exit] exitNumber=["+exitNumber+"] subFunction=["+subFunction+"], counter: "+counter);

        if (subFunction==RexxExitHandler.RXFNCCAL)  // 1
        {
            System.err.println(", RXFNCCAL=1:");

            boolean flag[]=(boolean[]) parmBlock[0];    // fetch flag array

            System.err.println("\trxf ferr=["+flag[0]+"], rxf fnfnd=["+flag[1]+"], rxf fsub=["+flag[2]+"]");
            System.err.println("\tfuncName=["+parmBlock[1]+"], queueName=["+parmBlock[2]+"], args=["+parmBlock[3]+"]");
            if (parmBlock[3]!=null)
            {
                String args[]=(String[]) parmBlock[3];
                int size=args.length;
                for (int i=0;i<size;i++)
                {
                    System.err.println("\t\targ["+(i+1)+"/"+size+"]: ["+args[i]+"]");
                }
            }

            String handleVar=(String) RexxHandler.getContextVariable(slot, "handleVar");

            if ( handleVar.compareTo("1")==0 )   // handle exit
            {
                flag[0]=(((String) RexxHandler.getContextVariable(slot, "invalidCallToRoutineFlag")).compareTo("1")==0);
                flag[1]=(((String) RexxHandler.getContextVariable(slot, "functionNotFoundFlag")).compareTo("1")==0);
                parmBlock[4]="<value from Java, counter=["+counter+"]>";  // return value
                return RexxExitHandler.RXEXIT_HANDLED;
            }
        }

        System.err.println();
        return RexxExitHandler.RXEXIT_NOT_HANDLED;
    }
}

