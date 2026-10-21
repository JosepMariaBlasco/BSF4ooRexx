package additions;

import org.rexxla.bsf.engines.rexx.RexxCommandHandler;

/** A Java command handler that raises a Rexx condition chosen by the
 *  command, through RexxHandler's default method raiseCondition():
 *  "user" raises USER MINE (rc 4), "notready" NOTREADY (rc 5), "error"
 *  ERROR (rc 6); any other command returns "0" and raises nothing.
 *  Used by CommandHandlerConditions.testGroup. */
public class ConditionRaisingCommandHandler implements RexxCommandHandler
{
    public Object handleCommand(Object slot, String address, String command)
    {
        String c = command.trim().toUpperCase();
        if (c.equals("USER"))     { raiseCondition(slot, "USER MINE", "from Java", null, "4"); return null; }
        if (c.equals("NOTREADY")) { raiseCondition(slot, "NOTREADY",  "from Java", null, "5"); return null; }
        if (c.equals("ERROR"))    { raiseCondition(slot, "ERROR",     "from Java", null, "6"); return null; }
        return "0";
    }
}
