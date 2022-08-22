package rgf.H_850;

import org.rexxla.bsf.engines.rexx.*;


public class DirectHandler1 implements RexxCommandHandler
{

    // entry to this command handler
    public Object handleCommand(Object slot, String address, String command)
    {
        return "address=["+address+"] command=["+command+"] version=["+this.toString(slot,address)+"]";
    }

}

