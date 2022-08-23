package rgf.H_850;

import org.rexxla.bsf.engines.rexx.*;

public class DirectHandler1 implements RexxCommandHandler
{
    // entry to this command handler
    public Object handleCommand(Object slot, String address, String command)
    {
        // toString(slot,address) method is a default method of RexxCommandHandler interface
        return "address=["+address+"] command=["+command+"] version=["+toString(slot,address)+"]";
    }
}

