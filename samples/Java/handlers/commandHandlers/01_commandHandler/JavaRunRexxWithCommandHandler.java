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
 * purpose: demonstrate the possibilities of a Java command handler
 *
 * Changed:    2013-06-13, demonstrate that one should use BSFManager's terminate() method,
 *                         if that particular Rexx interpreter instance is not needed anymore,
 *                         such that its reserved system resources can be reclaimed
               2022-08-06, explicitly do System.exit(0);
               2022-08-24, adjust for having changed RexxHandler to a superclass interface
                           (serving as the root interface class for all Rexx handler types)
                           in 850 and the 461 static methods to interface default methods;
                           this way invoking all default methods becomes simpler (no need
                           to refer explicitly to the class RexxHandler, normal inheritance
                           rules will take place)
 *
 * needs:   testRexxWithCommandHandler.rxj
 *
 * @author  Rony G. Flatscher
 * @since   2012-02-25
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
            // configure command handler
        rexxconf.addCommandHandler("EPOQUE", new JavaRunRexxWithCommandHandler());
        rexxconf.setInitialAddressEnvironment("EPOQUE");

            // add "rxmath" library, distributed with ooRexx
        rexxconf.addRequiredLibrary("rxmath");

            // add "./anotherpath" to lookup
        String fs=System.getProperty("file.separator");     // "\" or "/"
        String ps=System.getProperty("path.separator");     // ";" or ":"
        String ud=System.getProperty("user.dir");
        String dirName="anotherpath";
        rexxconf.setExternalCallPath("."+fs+dirName+ps+ud+fs+dirName);

            // add ",.pre" to default call extensions
        rexxconf.setExternalCallExtensions(rexxconf.getExternalCallExtensions()+",.pre");

        System.err.println("edited  rexxconf=["+rexxconf+"]\n");

        // invoke the interpreter and run the Rexx program
        rexxEngine.apply ("JavaRunRexxWithCommandHandler.rex", 0, 0, rexxCode, null, null);
        mgr.terminate();    // make sure that the Rexx interpreter instance gets terminated!
        System.exit(0);     // exit Java
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
            case 4:     return getNil(slot);
            case 5:     long iv=getInterpreterVersion(slot);
                        return "InterpreterVersion: -> as hex string: ["+Long.toHexString(iv)+"] -> as long: ["+iv+"]";
            case 6:     long ll=getLanguageLevel(slot);
                        return "LanguageLevel:      -> as hex string: ["+Long.toHexString(ll)+"] -> as long: ["+ll+"]";
            case 7:     return getGlobalEnvironment(slot);
            case 8:     return getLocalEnvironment(slot);
            case 9:     return getCallerContext(slot);
            case 10:    return getAllContextVariables(slot);
            case 11:    return (checkCondition(slot) == true ? "1" : "0");
            case 12:    return getConditionInfo(slot);
            case 13:    return (checkCondition(slot) == true ? "1" : "0");
            case 14:    return "This is \"case 14\", hence returning this interesting String... ;)";

            default:    return ""+counter;      // turn primitive into Java String
        }
    }


}

