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


public class JavaRunRexxWith_RXVALUE_Exit implements RexxExitHandler
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        RexxEngine rexxEngine=(RexxEngine) mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run
        String rexxCode= "call 'testRexxWith_RXVALUE_Exit.rxj'       ;" +
                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        // Configure the RexxEngine
        RexxConfiguration rexxconf=rexxEngine.getRexxConfiguration();
        System.err.println("default rexxconf=["+rexxconf+"]\n");

            // add system exits
        rexxconf.addExitHandler(RexxExitHandler.RXVALUE, new JavaRunRexxWith_RXVALUE_Exit() );

        System.err.println("edited  rexxconf=["+rexxconf+"]\n===> Java - starting Rexx program ===>");

        // invoke the interpreter and run the Rexx program
        rexxEngine.apply ("JavaRunRexxWithSystemExits.java", 0, 0, rexxCode, null, null);
        System.err.println("<=== Java - after Rexx program has finished. <===");
    }



        // implementation of a RXVALUE exit handler
    public  int     counter=0;      // count # of invocations,

    public int handleExit(Object slot, int exitNumber, int subFunction, Object[] parmBlock)
    {
        counter++;
        System.err.print("(Java-side) [RXVALUE_exit] exitNumber=["+exitNumber+"] subFunction=["+subFunction+"], counter: "+counter);
        if (subFunction==1)
        {
            System.err.println(", RXVALUECALL=1: selector=["+parmBlock[0]+"]"
                                      +", variable_name=["+parmBlock[1]+"]"
                                      +", value=["+parmBlock[2]+"]"
                                      );
        }
        String selector     =(String) parmBlock[0];
        String variable_name=(String) parmBlock[1];
        Object value        =parmBlock[2];

        System.err.print("(Java-side) [RXVALUE_exit] selector=["+selector+"], variable_name=["+variable_name+"]");
        if ( selector.compareToIgnoreCase("RXVALUE_BY_JAVA")==0 )
        {
            if (value==null)    // query value
            {
                String str="<some value ->"+counter+"<- supplied by the Java RXVALUE exit handler>";
                System.err.println(", returning value ["+str+"]");
                parmBlock[2]=str;
            }
            else    // set to new value
            {
                System.err.println(", returning supplied value as new value ---> ["+value+"] <---");
                RexxHandler.setContextVariable(slot,variable_name,value);
                // parmBlock[3]=obj;       // return value
            }
            System.err.println();
            return RexxExitHandler.RXEXIT_HANDLED;
        }

        System.err.println(": unknown selector, not handling this exit");
        return RexxExitHandler.RXEXIT_NOT_HANDLED;
    }
}

