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
             2022-08-06, rgf, explicitly do System.exit(0);
*/

import org.apache.bsf.*;
import org.rexxla.bsf.engines.rexx.*;


public class JavaRunRexxWith_RXMSQ_Exit implements RexxExitHandler
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        RexxEngine rexxEngine=(RexxEngine) mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run
        String rexxCode= "call 'testRexxWith_RXMSQ_Exit.rxj'        ;" +
                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        // Configure the RexxEngine
        RexxConfiguration rexxConf=rexxEngine.getRexxConfiguration();
        System.err.println("default rexxConf=["+rexxConf+"]\n");

            // add system exits
        rexxConf.addExitHandler(RexxExitHandler.RXMSQ, new JavaRunRexxWith_RXMSQ_Exit() );

        System.err.println("edited  rexxConf=["+rexxConf+"]\n===> Java - starting Rexx program ===>");

        // invoke the interpreter and run the Rexx program
        rexxEngine.apply ("JavaRunRexxWithSystemExits.rex", 0, 0, rexxCode, null, null);
        System.err.println("<=== Java - after Rexx program has finished. <===");

        mgr.terminate();    // make sure that the Rexx interpreter instance gets terminated!
        System.exit(0);     // exit Java
    }



        // implementation of a _RXMSQ exit handler
    public  int  counter=0;      // count # of invocations,

    public int handleExit(Object slot, int exitNumber, int subFunction, Object[] parmBlock)
    {
        counter++;
        System.err.print("(Java-side) [RXMSQ_exit] exitNumber=["+exitNumber+"] subFunction=["+subFunction+"], counter: "+counter);

        if (subFunction==RexxExitHandler.RXMSQPLL)          // 1 - pull string from external queue
        {
            parmBlock[0]="<value from Java, pretend this was pulled from an external queue, exit invocation counter=["+counter+"]>";
            System.err.println(", RXMSQPLL returns: ["+parmBlock[0]+"]");
            return RexxExitHandler.RXEXIT_HANDLED;
        }

        else if (subFunction==RexxExitHandler.RXMSQPSH)     // 2 - push string to external queue
        {
            boolean flag[]=(boolean[]) parmBlock[0];    // fetch flag array
            System.err.println(", RXMSQPSH received: ["+parmBlock[1]+"], LIFO=["+flag[0]+"]");
            return RexxExitHandler.RXEXIT_HANDLED;
        }

        else if (subFunction==RexxExitHandler.RXMSQSIZ)     // 3 - return number of items in external queue
        {
            parmBlock[0]="123";     // return
            System.err.println(", RXMSQSIZ returns: ["+parmBlock[0]+"]");
            return RexxExitHandler.RXEXIT_HANDLED;
        }

        else if (subFunction==RexxExitHandler.RXMSQNAM)     // 4 - set new external queue name
        {
            System.err.println(", RXMSQNAM received: ["+parmBlock[0]+"]");
            return RexxExitHandler.RXEXIT_HANDLED;
        }

            // future safe should there ever be a new subfunction added after 2012-02-20
        return RexxExitHandler.RXEXIT_NOT_HANDLED;
    }
}

