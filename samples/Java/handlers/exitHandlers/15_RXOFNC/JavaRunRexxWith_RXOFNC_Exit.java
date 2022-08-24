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
 * @since   2012-02-21
 */

/*
    Changed: 2013-06-17, rgf, demonstrate that one should use BSFManager's terminate() method,
                            if that particular Rexx interpreter instance is not needed anymore,
                            such that its reserved system resources can be reclaimed
             2022-08-06, rgf, explicitly do System.exit(0);
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

public class JavaRunRexxWith_RXOFNC_Exit implements RexxExitHandler
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        RexxEngine rexxEngine=(RexxEngine) mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run
        String rexxCode= "call 'testRexxWith_RXOFNC_Exit.rxj'        ;" +
                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        // Configure the RexxEngine
        RexxConfiguration rexxConf=rexxEngine.getRexxConfiguration();
        System.err.println("default rexxConf=["+rexxConf+"]\n");

            // add system exits
        rexxConf.addExitHandler(RXOFNC, new JavaRunRexxWith_RXOFNC_Exit() );

        System.err.println("edited  rexxConf=["+rexxConf+"]\n===> Java - starting Rexx program ===>");

        // invoke the interpreter and run the Rexx program
        rexxEngine.apply ("JavaRunRexxWithSystemExits.rex", 0, 0, rexxCode, null, null);
        System.err.println("<=== Java - after Rexx program has finished. <===");

        mgr.terminate();    // make sure that the Rexx interpreter instance gets terminated!
        System.exit(0);     // exit Java
    }



        // implementation of a _RXOFNC exit handler
    public  int  counter=0;      // count # of invocations,

    public int handleExit(Object slot, int exitNumber, int subFunction, Object[] parmBlock)
    {
        counter++;
        System.err.print("(Java-side) [RXOFNC_exit] exitNumber=["+exitNumber+"] subFunction=["+subFunction+"], counter: "+counter);

        if (subFunction==RXOFNCCAL)  // 1
        {
            System.err.println(", RXOFNCCAL=1:");

            boolean flag[]=(boolean[]) parmBlock[0];    // fetch flag array

            System.err.println("\trxf ferr=["+flag[0]+"], rxf fnfnd=["+flag[1]+"], rxf fsub=["+flag[2]+"]");
            System.err.println("\tfuncName=["+parmBlock[1]+"], args=["+parmBlock[2]+"], # args=["+(parmBlock[2]==null?0:((Object[])parmBlock[2]).length)+"]");
            if (parmBlock[2]!=null)
            {
                Object args[]=(Object []) parmBlock[2];
                int size=args.length;
                for (int i=0;i<size;i++)
                {
                    System.err.println("\t\targ["+(i+1)+"/"+size+"]: ["+args[i]+"]");
                }
            }

            String handleVar=(String) getContextVariable(slot, "handleVar");

            if ( handleVar.compareTo("1")==0 )   // handle exit
            {
                flag[0]=(((String) getContextVariable(slot, "invalidCallToRoutineFlag")).compareTo("1")==0);
                flag[1]=(((String) getContextVariable(slot, "functionNotFoundFlag")).compareTo("1")==0);
                parmBlock[3]="<value from Java, counter="+counter+">";  // return value
                return RXEXIT_HANDLED;
            }
        }

        System.err.println();
        return RXEXIT_NOT_HANDLED;
    }
}

