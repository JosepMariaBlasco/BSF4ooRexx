package rgf.H_850;
import org.rexxla.bsf.engines.rexx.*;

// purpose allow access tests to RexxHandler default methods (now available via inheritance)


public class RedirectingHandler2 implements RexxRedirectingCommandHandler
{
    // entry to this command handler
    public Object handleCommand(Object slot, String address, String command)
    {

System.err.println("address=["+address+"] command=["+command+"] version=["+this.toString(slot,address)+"]");

        // split at (and consume) whitespace
        String [] arrCommand = command.split("\\p{javaWhitespace}+");
System.err.println("\t*** arrCommand.length=["+arrCommand.length+"], Arrays.deepToString(arrCommand)=["+java.util.Arrays.deepToString(arrCommand)+"]");

        String cmd = arrCommand[0].toUpperCase();   // split on whitespace

        if (arrCommand.length==0 || cmd.length()==0)    // command omitted, show help
        {
            cmd="?";
        }

System.err.println("cmd=["+cmd+"], arrCommand.length=["+arrCommand.length+"], before switch statement!");

        switch (cmd)
        {
            case "DROPCONTEXTVARIABLE":
System.err.println("cmd=["+cmd+"], arrCommand.length=["+arrCommand.length+"]");
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
System.err.println("cmd=["+cmd+"], arrCommand.length=["+arrCommand.length+"]");
                if (arrCommand.length<2)
                {
                    // api/oorexxerrors.h:392:#define Rexx_Error_Incorrect_call_user_defined  40900
                    String [] msg=new String[]{"Environment handler for \""+address+"\": command ["+command+"] does not contain variable name"};
                    raiseException(slot, 40900, msg);
                    return null;
                }
System.err.println("cmd=["+cmd+"], arrCommand[1]=["+arrCommand[1]+"]");
                return getContextVariable(slot, arrCommand[1]);

            case "SETCONTEXTVARIABLETONIL":
System.err.println("cmd=["+cmd+"], arrCommand.length=["+arrCommand.length+"]");
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
                return arrCommand[2];


            case "GETINTERPRETERVERSION": return getInterpreterVersion(slot); // <code>0x00050000</code> for version 5.0.0.&quot;

            case "GETLANGUAGELEVEL": return getInterpreterVersion(slot);      // <code>0x00000603</code> for language level 6.03.&quot;

            case "GETLOCALENVIRONMENT":   return getLocalEnvironment(slot);

            case "GETGLOBALENVIRONMENT":  return getGlobalEnvironment(slot);

            case "GETNIL": return getNil(slot);

            case "GETVERSION":
System.err.println("cmd=["+cmd+"], arrCommand.length=["+arrCommand.length+"]");
                 return "address=["+address+"] command=["+command+"] version=["+this.toString(slot,address)+"]";

            case "?":
            case "HELP":
System.err.println("HELP/? - cmd=["+cmd+"], arrCommand.length=["+arrCommand.length+"]");
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

                System.out.println("command handler for ["+address+"] has the following available commands:"+
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
                                   );
                return info;

            case "RAISEEXCEPTION":
            default:
System.err.println("default/RAISEEXCEPTION: cmd=["+cmd+"], arrCommand.length=["+arrCommand.length+"]");

                // api/oorexxerrors.h:392:#define Rexx_Error_Incorrect_call_user_defined  40900
                String [] msg=new String[]{"Environment handler for \""+address+"\": received unknown or raiseException command ["+command+"]"};
                raiseException(slot, 40900, msg);
                return null;
        }

    }
}

