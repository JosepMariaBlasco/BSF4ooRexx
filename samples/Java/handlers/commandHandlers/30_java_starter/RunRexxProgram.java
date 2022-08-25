import org.apache.bsf.BSFManager;
import org.rexxla.bsf.engines.rexx.*;

public class RunRexxProgram
{
    public static void main (String args[])
    {
        if (args.length==0)
        {
            System.err.println("You must supply the name of a Rexx program to run.");
            System.exit(-1);
        }

        try
        {
            BSFManager mgr= new BSFManager();
            RexxEngine re = (RexxEngine) mgr.loadScriptingEngine("rexx");
            RexxConfiguration rexxconf=re.getRexxConfiguration();
            System.err.println("default rexxconf=["+rexxconf+"]\n");

            rexxconf.addCommandHandler("ONE"   ,         new OneCommandHandler());
            rexxconf.addCommandHandler("DEUX"  ,         new DeuxCommandHandler());
            rexxconf.setInitialAddressEnvironment("ONE");
            System.err.println("\nafter configuration:");
            System.err.println("edited  rexxconf=["+rexxconf+"]\n");

            System.err.println("now running the Rexx program you supplied in file \""+args[0]+"\":");
            // Rexx code to run
            String rexxCode= "call \""+args[0]+"\"      ;" +
                             "::requires BSF.CLS        ;" ;    // get ooRexx support (camouflage Java as ooRexx)
            // invoke the interpreter and call the Rexx program in the supplied file name
            re.apply (args[0], 0, 0, rexxCode, null, null);
            mgr.terminate();    // make sure that the Rexx interpreter instance gets terminated!
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            System.err.println("\n"+t);
        }
        System.exit(0);     // exit Java
    }


    public static class OneCommandHandler implements RexxCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[OneCommandHandler] address=["+address+"] "+"command=["+command+"]");

            // split at (and consume) whitespace
            String [] arrCommand = command.split("\\p{javaWhitespace}+");
            String cmd = "";
            if (arrCommand.length>0)
            {
                cmd=arrCommand[0].toUpperCase();    // get command, uppercase it
            }

            // excercise the default methods from RexxRedirectingCommandHandler or RexxHandler
            switch (cmd)
            {
                case "HELLO":   // return values set the RC variable in Rexx
                    return "Hello, ooRexx! This is your Java OneCommandHandler greeting you!";

                case "INFO":    // return information about this handler
                    return toString(slot,address);

                case "PING":    // return values set the RC variable in Rexx
                    return "Pong!";

                default:        // unknown command received create a failure
                    String [] strAdditional = new String[]{ "Unknown command ["+command+"]" };
                    //                                                      RC:
                    raiseCondition(slot, "Failure", command, strAdditional, "-1");
                    return null;    // uses fourth argument of raiseCondition as RC
            }
        }
    }


    public static class DeuxCommandHandler implements RexxRedirectingCommandHandler
    {
        public Object handleCommand(Object slot, String address, String command)
        {
            System.err.println("[DeuxCommandHandler] address=["+address+"] "+"command=["+command+"]");

            // split at (and consume) whitespace
            String [] arrCommand = command.split("\\p{javaWhitespace}+");
            String cmd = "";
            if (arrCommand.length>0)
            {
                cmd=arrCommand[0].toUpperCase();    // get command, uppercase it
            }

            // excercise the default methods from RexxRedirectingCommandHandler or RexxHandler
            switch (cmd)
            {
                case "HELLO":   // return values set the RC variable in Rexx
                    return "Hello, ooRexx! This is your Java DeuxCommandHandler greeting you!";

                case "INFO":    // return information about this handler
                    return toString(slot,address);

                case "PING":    // return values set the RC variable in Rexx
                    return "Pong!";

                default:        // unknown command received create a failure
                    String [] strAdditional = new String[]{ "Unknown command ["+command+"]" };
                    //                                                      RC:
                    raiseCondition(slot, "Failure", command, strAdditional, "-1");
                    return null;    // uses fourth argument of raiseCondition as RC
            }
        }
    }

}
