/* ------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2022 Rony G. Flatscher
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
 * @since   2022-08-25
 */

import org.rexxla.bsf.engines.rexx.*;
import java.util.GregorianCalendar;

public class RedirectingCommandHandler implements RexxRedirectingCommandHandler
{
    int counter=0;  // count number of invocations
    boolean bDebug=true; // false; // true
    /** This direct command handler keeps a counter of invocations and does
     *  different things depending on the counter's value.
     */
    public Object handleCommand(Object slot, String address, String command)
    {
        counter++;
if (bDebug)
{
    System.err.println("[RedirectingCommandHandler].handleCommand(slot, address=["+address+"], "+
                         "command=["+command+"]), counter=["+counter+"]");
}

        // split at (and consume) whitespace
        String [] arrCommand = command.split("\\p{javaWhitespace}+");
        String cmd = "";
        if (arrCommand.length>0)
        {
            cmd=arrCommand[0].toUpperCase();    // get command, uppercase it
        }

        // excercise the default methods from RexxRedirectingCommandHandler or RexxHandler
        String [] strAdditional=null;   // if conditions get raised use it for message text

        switch (cmd)
        {
            case "HELLO":   // return values set the RC variable in Rexx
                return "Hello, ooRexx! This is your Java RedirectingCommandHandler working for you!";

            case "INFO":    // return information about this handler
                return toString(slot,address);

            case "PING":    // return values set the RC variable in Rexx
                return "Pong!";

                // cases working on data with potential redirections
            case "UPPERCASE":
            case "LOWERCASE":
            case "ROT13":
                boolean iOR=isOutputRedirected(slot);
                boolean iER=isErrorRedirected(slot);
                if (! iOR )     // output not redirected
                {
                    if (! iER)  // error also not redirected
                    {
                        strAdditional = new String[]{ "Neither \"output\" nor \"error\" redirected, cannot write any result" };
                        //                                                      RC:
                        raiseCondition(slot, "Failure", command, strAdditional, "-1");
                        return null;    // uses fourth argument of raiseCondition as RC
                    }
                }

                boolean iIR  =isInputRedirected(slot);

                byte [] bData=null;
                String  sData=null;

                if (iIR)    // input is redirected
                {
                    if (cmd.equals("ROT13"))    // read into a byte array
                    {
                        bData=readInputAsByteArray(slot);
                    }                           // read into a String
                    else
                    {
                        sData=readInput(slot);
                    }
                }
                else        // input not redirected, use remaining data after first blank after comand
                {
                    int pos=command.indexOf(' ');   // get first whitespace, use everything after it as data
                    if ( pos>1 && command.length()>=(pos+2))
                    {
                        sData=command.substring(pos+1);
                        if (cmd.equals("ROT13"))
                        {
                            bData=sData.getBytes();
                        }
                    }
                    else
                    {
                        strAdditional = new String[]{ "neither \"input\" redirected, nor data supplied after command" };
                        //                                                    RC:
                        raiseCondition(slot, "ERROR", command, strAdditional, "-11");
                        return null;
                    }
                }

                // now process data
                if (cmd.equals("ROT13"))
                {
                    if (iOR)    // output is redirected, write to it
                    {
                        writeOutput(slot,rot13(bData));
                        if (iER)    // if error redirected, reprocess (nullifying rot13) and write to error
                        {
                            writeError(slot,rot13(bData));
                        }
                    }
                    else        // write to redirected error
                    {
                        writeError(slot,rot13(bData));
                    }
                    return "0";    // indicate all o.k.
                }

                if (cmd.equals("UPPERCASE"))
                {
                    sData=sData.toUpperCase();
                }
                else    // only command left
                {
                    sData=sData.toLowerCase();
                }

                if (iOR)    // output redirected, then use it
                {
                    writeOutput(slot,sData);
                }
                else        // use redirected error for output
                {
                    writeError(slot,sData);
                }
                return "0";

            default: // show help text, return -2
                String usage="Error: ["+address+"] handler does not understand command: ["+command+"]"+
                    "\n\tHELLO"                   +
                    "\n\tINFO"                    +
                    "\n\tLOWERCASE [text]"        +
                    "\n\t... if \"text\" is omitted, then redirected \"input\" gets processed, "+
                    "\n\t    result is written to \"output\", if redirected, "+
                    "\n\t    else to redirected \"error\"" +
                    "\n\tPING"                    +
                    "\n\tROT13 [text]"    +
                    "\n\t... if \"text\" is omitted, then redirected \"input\" gets processed, "+
                    "\n\t    result is written to \"output\", if redirected, "+
                    "\n\t    reprocessed data is written to \"error\", if redirected" +
                    "\n\tUPPERCASE [text]"        +
                    "\n\t... if \"text\" is omitted, then redirected \"input\" gets processed, "+
                    "\n\t    result is written to \"output\", if redirected, "+
                    "\n\t    else to redirected \"error\"" ;

                System.err.println(usage);
                // strAdditional = new String[]{ "O my God! An unknown command!!!" };
                strAdditional = usage.split("\n");
                //                                                      RC:
                raiseCondition(slot, "Error", command, strAdditional, "-2");
                return null;   // as condition got raised, condition's RC will get used
        }
    }

    /** Carry out rot13 on bytes.
     */
    static byte[] rot13 (byte [] bData)
    {
        for (int idx=0;idx<bData.length;idx++)
        {
            byte b=bData[idx];
            if       ((b >= 'a' && b <= 'm') || (b >= 'A' && b <= 'M')) b += 13;
            else if  ((b >= 'n' && b <= 'z') || (b >= 'N' && b <= 'Z')) b -= 13;
            bData[idx]=b;
        }
        return bData;
    }
}

