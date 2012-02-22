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
 * purpose: demonstrate the possibilities of a Java command handler
 * needs:   testRexxWithCommandHandler.rxj
 *
 * @author  Rony G. Flatscher
 * @since   2012-02-21
 */

import org.apache.bsf.*;
import org.rexxla.bsf.engines.rexx.*;
import java.util.GregorianCalendar;


public class JavaRunRexxWithCommandHandler implements RexxCommandHandler
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        RexxEngine rexxEngine=(RexxEngine) mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run
        String rexxCode= "call 'testRexxWithCommandHandler.rxj'       ;" +
                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        // Configure the RexxEngine
        RexxConfiguration rexxconf=rexxEngine.getRexxConfiguration();
        System.err.println("default rexxconf=["+rexxconf+"]\n");
        rexxconf.addCommandHandler("EPOQUE", new JavaRunRexxWithCommandHandler());

        rexxconf.setInitialAddressEnvironment("EPOQUE");

            // add ",.pre" to default call extensions
        rexxconf.setExternalCallExtensions(rexxconf.getExternalCallExtensions()+",.pre");

        rexxconf.setExternalCallPath("..");

        System.err.println("edited  rexxconf=["+rexxconf+"]\n");

        // invoke the interpreter and run the Rexx program
        rexxEngine.apply ("JavaRunRexxWithCommandHandler.java", 0, 0, rexxCode, null, null);
    }


    int counter=0;
    public Object handleCommand(Object slot, String address, String command)
    {
        counter++;
        System.err.println("[JavaRunRexxWithCommandHandler] address=["+address+"]"+
                             "\tcommand=["+command+"], counter=["+counter+"]");
        switch (counter)
        {
            case 1:     return "123";
            case 2:     return new GregorianCalendar();
            case 3:     return null;
            default:    return ""+counter;      // turn primitive into Java String
        }
    }


}

