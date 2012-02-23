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
        rexxConf.addExitHandler(RexxExitHandler.RXCMD, new JavaRunRexxWith_RXCMD_Exit() );

        System.err.println("edited  rexxConf=["+rexxConf+"]\n===> Java - starting Rexx program ===>");

        // invoke the interpreter and run the Rexx program
        rexxEngine.apply ("JavaRunRexxWithSystemExits.java", 0, 0, rexxCode, null, null);
        System.err.println("<=== Java - after Rexx program has finished. <===");
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
            if (((String) RexxHandler.getContextVariable(slot,"RGF_JAVA")).compareTo("0")==0) // o.k. addressed to us
            {
                if (((String) RexxHandler.getContextVariable(slot,"RGF_FAILURE")).compareTo("1")==0)
                {
                    flag[0]=true;       // set rxfcfail
                }
                else if (((String) RexxHandler.getContextVariable(slot,"RGF_ERROR")).compareTo("1")==0)
                {
                    flag[1]=true;       // set rxfcerr
                }
                else
                {
                    parmBlock[4]="<dealt by Java exit handler for address=["+address+"], received command=["+command+"], handler invoked ["+counter+"] time"+(counter==1?"":"s")+" so far.>";
                }
                return RexxExitHandler.RXEXIT_HANDLED;
            }
        }
        System.err.println();

        return RexxExitHandler.RXEXIT_NOT_HANDLED;
    }
}

