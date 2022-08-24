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
 * purpose: demonstrate some possibilities of definining Rexx command handlers
 *           in Java
 * @author  Rony G. Flatscher
 * @since   2022-08-24
 */

import org.rexxla.bsf.engines.rexx.*;
import java.util.GregorianCalendar;

public class DirectCommandHandler implements RexxCommandHandler
{
    int counter=0;
    /** This direct command handler keeps a counter of invocations and does
     *  different things depending on the counter's value.
     */
    public Object handleCommand(Object slot, String address, String command)
    {
        counter++;
        System.err.println("[DirectCommandHandler] address=["+address+"]"+
                             "\tcommand=["+command+"], counter=["+counter+"]");
        switch (counter)
        {
            case 1:     return "123";
            case 2:     return new GregorianCalendar();
            case 3:     return null;
            case 4:     return getNil(slot);                    // from RexxHandler super interface
            case 5:     long iv=getInterpreterVersion(slot);    // from RexxHandler super interface
                        return "InterpreterVersion: -> as hex string: ["+Long.toHexString(iv)+"] -> as long: ["+iv+"]";
            case 6:     long ll=getLanguageLevel(slot);         // from RexxHandler super interface
                        return "LanguageLevel:      -> as hex string: ["+Long.toHexString(ll)+"] -> as long: ["+ll+"]";
            case 7:     return getGlobalEnvironment(slot);      // from RexxHandler super interface
            case 8:     return getLocalEnvironment(slot);       // from RexxHandler super interface
            case 9:     return getCallerContext(slot);          // from RexxHandler super interface
            case 10:    return getAllContextVariables(slot);    // from RexxHandler super interface
            case 11:    return (checkCondition(slot) == true ? "1" : "0");  // from RexxHandler super interface
            case 12:    return getConditionInfo(slot);          // from RexxHandler super interface
            case 13:    return (checkCondition(slot) == true ? "1" : "0");  // from RexxHandler super interface
            case 14:    return "This is \"case 14\", hence returning this interesting String... ;)";

            default:    return ""+counter;      // turn primitive into Java String
        }
    }
}

