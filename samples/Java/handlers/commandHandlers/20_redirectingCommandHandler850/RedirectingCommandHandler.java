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

public class RedirectingCommandHandler implements RexxRedirectingCommandHandler
{
    int counter=0;  // count number of invocations
    /** This direct command handler keeps a counter of invocations and does
     *  different things depending on the counter's value.
     */
    public Object handleCommand(Object slot, String address, String command)
    {
        counter++;
        System.err.println("[DirectCommandHandler] address=["+address+"]"+
                             "\tcommand=["+command+"], counter=["+counter+"]");

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

            case "PING":    // return values set the RC variable in Rexx
                return "Pong!";

                // cases working on data with potential redirections
            case "UPPERCASE":
            case "LOWERCASE":
            case "ROT13":
                boolean iOR=isOutputRedirected(slot);
                boolean iER=isErrorRedirected(slot);
System.err.println("iOR="+iOR+", iER="+iER);
                if (! iOR )
                {
System.err.println("! iOR");
                    if (! iER)
                    {
System.err.println("! iER");
                        strAdditional = new String[]{ "Neither \"output\" nor \"error\" redirected, cannot write any result" };
                        //                                                      RC:
                        raiseCondition(slot, "Failure", command, strAdditional, "-1");
                    }
                    else
                    {
System.err.println("! iOR");
                        strAdditional = new String[]{ "\"output\" not redirected, cannot write result" };
                        //                                                    RC:
                        raiseCondition(slot, "ERROR", command, strAdditional, "-10");
                    }
                    // ignored, because the condition's fourth argument will be used for the RC value
                    return null;
                }

                boolean iIR  =isInputRedirected(slot);

System.err.println("iIR="+iIR);
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
                        sData=readInputBuffer(slot);
                    }
                }
                else        // input not redirected, use remaining data after first blank after comand
                {
                    int pos=command.indexOf(' ');   // get first whitespace, use everything after it as data

System.err.println("command=["+command+"]: pos="+pos+", command.length()="+command.length());

                    if ( command.length()>=(pos+1))
                    {
                        sData=command.substring(pos+1);

System.err.println("command=["+command+"]: pos="+pos+", command.length()="+command.length()+", sData=["+sData+"]");
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

System.err.println("cmd="+cmd+", sData=["+sData+"], bData="+bData+" -> "+(bData==null? null : new String(bData))+" checkCondition()="+checkCondition(slot));

                // now process data
                if (cmd.equals("ROT13"))
                {
                    if (iOR)    // output is redirected, write to it
                    {
System.err.println("ROT13 - iOR");
                        writeOutput(slot,rot13(bData));
                        if (iER)    // if error redirected, reprocess (nullifying rot13) and write to error
                        {
System.err.println("ROT13 - iER");
                            writeError(slot,rot13(bData));
                        }
                    }
                    else        // write to redirected error
                    {
System.err.println("ROT13 - iER");
                        writeError(slot,rot13(bData));
                    }
System.err.println("ROT13 - return 0!");
                    return "0";    // indicate all o.k.
                }

                if (cmd.equals("UPPERCASE"))
                {
                    sData.toUpperCase();
                }
                else
                {
                    sData.toLowerCase();
                }
                if (iOR)
                {
                    writeOutput(slot,sData);
                }
                else
                {
                    writeError(slot,sData);
                }
                return 0;

            default: // show help text, return -2
                String usage="Error: ["+address+"] handler does not understand command: ["+command+"]"+
                    "\n\tPING"                    +
                    "\n\tHELLO"                   +
                    "\n\tROT13 [textToEncode]"    +
                    "\n\t... if \"textToEncode\" is omitted, then redirected \"input\" gets processed, "+
                    "\n\t    result is written to \"output\", if redirected, "+
                    "\n\t    reprocessed data is written to \"error\", if redirected.";
                System.err.println(usage);
                strAdditional = new String[]{ "O my God! An unknown command!!!" };
                //                                                      RC:
                raiseCondition(slot, "Failure", command, strAdditional, "-2");
                return 0;   // as condition got raised, condition's RC will get used
        }
    }

    /** Carry out rot13 on bytes.
     */
    static byte[] rot13 (byte [] bData)
    {
System.err.println("...rot13(), bData.length="+bData.length+" -> "+new String(bData));
        for (int idx=0;idx<bData.length;idx++)
        {
            byte b=bData[idx];
            if       (b >= 'a' && b <= 'm') b += 13;
            else if  (b >= 'A' && b <= 'M') b += 13;
            else if  (b >= 'n' && b <= 'z') b -= 13;
            else if  (b >= 'N' && b <= 'Z') b -= 13;
            bData[idx]=b;
        }
        return bData;
    }
}

