package rgf.H_850;
import org.rexxla.bsf.engines.rexx.*;

// purpose allow access tests to RexxHandler default methods (now available via inheritance)


public class RedirectingHandler2 implements RexxRedirectingCommandHandler
{
    // entry to this command handler
    public Object handleCommand(Object slot, String address, String command)
    {

// System.err.println("address=["+address+"] command=["+command+"] version=["+this.toString(slot,address)+"]");

        // split at (and consume) whitespace
        String [] arrCommand = command.split("\\p{javaWhitespace}+");

        String cmd = arrCommand[0].toUpperCase();   // split on whitespace

        if (arrCommand.length==0 || cmd.length()==0)    // command omitted, show help
        {
            cmd="?";
        }

        switch (cmd)
        {
            case "DROPCONTEXTVARIABLE":
                if (arrCommand.length<2)
                {
                    // api/oorexxerrors.h:392:#define Rexx_Error_Incorrect_call_user_defined  40900
                    String [] msg=new String[]{"Environment handler for \""+address+"\": command ["+command+"] does not contain variable name"};
                    raiseException(slot, 40900, msg);
                    return null;
                }
                dropContextVariable(slot, arrCommand[1]);
                return "0";

            case "GETCONTEXTVARIABLE":
                if (arrCommand.length<2)
                {
                    // api/oorexxerrors.h:392:#define Rexx_Error_Incorrect_call_user_defined  40900
                    String [] msg=new String[]{"Environment handler for \""+address+"\": command ["+command+"] does not contain variable name"};
                    raiseException(slot, 40900, msg);
                    return null;
                }
                return getContextVariable(slot, arrCommand[1]);

            case "SETCONTEXTVARIABLETONIL":
                if (arrCommand.length<2)
                {
                    // api/oorexxerrors.h:392:#define Rexx_Error_Incorrect_call_user_defined  40900
                    String [] msg=new String[]{"Environment handler for \""+address+"\": command ["+command+"] does not contain variable name"};
                    raiseException(slot, 40900, msg);
                    return null;
                }
                setContextVariableToNil(slot, arrCommand[1]);
                return "0";

            case "SETCONTEXTVARIABLE":
                if (arrCommand.length<3)
                {
                    // api/oorexxerrors.h:392:#define Rexx_Error_Incorrect_call_user_defined  40900
                    String [] msg=new String[]{"Environment handler for \""+address+"\": command ["+command+"] does not contain either variable name or new value"};
                    raiseException(slot, 40900, msg);
                    return null;
                }
                setContextVariable(slot, arrCommand[1], arrCommand[2]);
                return "0";


            case "GETINTERPRETERVERSION": return getInterpreterVersion(slot); // <code>0x00050000</code> for version 5.0.0.&quot;

            case "GETLANGUAGELEVEL": return getInterpreterVersion(slot);      // <code>0x00000603</code> for language level 6.03.&quot;

            case "GETLOCALENVIRONMENT":   return getLocalEnvironment(slot);

            case "GETGLOBALENVIRONMENT":  return getGlobalEnvironment(slot);

            case "GETNIL": return getNil(slot);

            case "SECRET":   // test reading input as processed string or unprocessed byteArray
                    //
                  char c=arrCommand[1].charAt(0);
                  switch (c)
                  {
                      case '0':     // readInput
                          return readInput(slot);

                      case '1':     // readInputBuffer
                          return readInputBuffer(slot);

                      case '2':     // readInputAsByteArray with data
                          if (arrCommand.length<3)
                          {
                              return readInputAsByteArray(slot);
                          }
                          // read as long as an item gets returned as byte array
                          // turn each array into its String value and append it to StringBuffer
                          // finally return the String (without any .endOfLine injected)
                          byte [] arrTmp=null;
                          StringBuffer sb=new StringBuffer();
                          for (arrTmp=readInputAsByteArray(slot);arrTmp!=null;arrTmp=readInputAsByteArray(slot))
                          {
                              sb.append(new String(arrTmp));
                          }
                          return sb.toString();


                      default:      // readInputAsByteArray with no data
                          return readInputAsByteArray(slot);
                  }


            case "GETVERSION":
                 return "address=["+address+"] command=["+command+"] version=["+this.toString(slot,address)+"]";

            case "?":
            case "HELP":


// do not change order nor content: !!
                String info =
                            "dropContextVariable " +
                            "getContextVariable " +
                            "setContextVariable " +
                            "setContextVariableToNil " +
                            "getInterpreterVersion " +
                            "getLanguageLevel " +
                            "getLocalEnvironment " +
                            "getGlobalEnvironment " +
                            "getNil " +
                            "raiseException " +
                            "getVersion "
                              ;

                String helpInfo="command handler for ["+address+"] has the following available commands:"+
                                "\n\tdropContextVariable varName" +
                                "\n\tgetContextVariable varName" +
                                "\n\tsetContextVariable varName (... set to this handler instance)" +
                                "\n\tsetContextVariableToNil varName" +
                                "\n\tgetInterpreterVersion " +
                                "\n\tgetLanguageLevel " +
                                "\n\tgetLocalEnvironment " +
                                "\n\tgetGlobalEnvironment " +
                                "\n\tgetNil " +
                                "\n\traiseException " +
                                "\n\tgetVersion " +
                                "\n\thelp "+
                                "\n\t? "
                                ;

                // using output or error, if input is redirected
                if (isInputRedirected(slot))    // if redirected, tells us what output to use
                {
                    String input = readInput(slot);
// System.err.println("\t*** --> --> --> readInput(slot): input=["+input+"] <-- <-- <-- ***");

                    if (isOutputRedirected(slot))
                    {
                        if (input.charAt(0)=='0')
                        {
                            writeOutput(slot,helpInfo);
                        }
                        else if (input.charAt(0)=='1')
                        {
                            writeOutputBuffer(slot,helpInfo);
                        }
                        else if (input.charAt(0)=='2')      // use byte array
                        {
                            writeOutput(slot,helpInfo.getBytes());
                        }
                        else    // for testing purposes show output if input is not redirected
                        {
                            System.out.println("*** BRRR ***: isInputRedirected(), but no valid value ('0'|'1'|'2')! *** BRRR ***");
                        }
                    }
                    else if (isErrorRedirected(slot))   // if error redirected, write string to it
                    {
                        if (input.charAt(0)=='0')
                        {
                            writeError(slot,helpInfo);
                        }
                        else if (input.charAt(0)=='1')
                        {
                            writeErrorBuffer(slot,helpInfo);
                        }
                        else if (input.charAt(0)=='2')      // use byte array
                        {
                            writeError(slot,helpInfo.getBytes());
                        }
                        else    // for testing purposes show output if input is not redirected
                        {
                            System.out.println("*** BRRR ***: isInputRedirected(), but no valid value ('0'|'1'|'2')! *** BRRR ***");
                        }
                    }
                    else
                    {
                        System.out.println("*** BRRR ***: isInputRedirected(), but output/error redirection set!! *** BRRR ***");
                    }
                }
                return info;


            case "RAISEEXCEPTION":
            default:
// System.err.println("default/RAISEEXCEPTION: cmd=["+cmd+"], arrCommand.length=["+arrCommand.length+"]");

                // api/oorexxerrors.h:392:#define Rexx_Error_Incorrect_call_user_defined  40900
                String [] msg=new String[]{"Environment handler for \""+address+"\": received unknown or raiseException command ["+command+"]"};
                raiseException(slot, 40900, msg);
                return null;
        }

    }
}

